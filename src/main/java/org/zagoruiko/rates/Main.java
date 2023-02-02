package org.zagoruiko.rates;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.PropertySource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.zagoruiko.rates.client.BinanceRatesClient;
import org.zagoruiko.rates.client.RatesClient;
import org.zagoruiko.rates.dto.ExchangePairDTO;
import org.zagoruiko.rates.service.PortfolioService;
import org.zagoruiko.rates.service.StorageService;
import org.zagoruiko.rates.util.AssetPair;
import org.zagoruiko.rates.util.Binance;
import org.zagoruiko.rates.util.Exmo;

import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.text.Format;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.function.Function;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

@Component
@PropertySource(value = "classpath:application.properties")
public class Main {

    private static Format format = new SimpleDateFormat("yyyy-MM-dd");
    private static Calendar calendar = Calendar.getInstance();
    private RatesClient binanceRatesClient;
    private RatesClient exmoRatesClient;

    private StorageService storageService;

    private PortfolioService portfolioService;

    @Autowired
    @Qualifier("binanceRatesClient")
    public void setBinanceRatesClient(RatesClient binanceRatesClient) {
        this.binanceRatesClient = binanceRatesClient;
    }

    @Autowired
    @Qualifier("exmoRatesClient")
    public void setExmoRatesClient(RatesClient exmoRatesClient) {
        this.exmoRatesClient = exmoRatesClient;
    }

    @Autowired
    public void setPortfolioService(PortfolioService portfolioService) {
        this.portfolioService = portfolioService;
    }

    @Autowired
    public void setStorageService(StorageService storageService) {
        this.storageService = storageService;
    }

    public static void main(String[] args) throws IOException, ParseException {
        AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext();
        context.scan(Main.class.getPackage().getName());
        context.refresh();
        context.getBean(Main.class).run(args);
    }

    public void run(String[] args) throws IOException, ParseException {

        Date startDate = (Date) format.parseObject("2015-01-01");
        storageService.prepareTableFolder("currency", "binance");
        storageService.prepareTableFolder("investing.com.rates", "main");

        List<ExchangePairDTO> pairs = portfolioService.getAllPairs();
        Set<AssetPair> pairsSet = pairs.stream()
                .map(p -> new AssetPair(p.getAsset(), p.getQuote()))
                .collect(Collectors.toSet());

        Set<AssetPair> pairsSetToAdd = new HashSet<>();
        pairsSet.forEach(p -> {
            AssetPair btcPair = new AssetPair(p.getAsset(), "BTC");
            if (!pairsSet.contains(btcPair) && !btcPair.getAsset().equals(btcPair.getQuote())) {
                pairsSetToAdd.add(btcPair);
            }
            AssetPair usdtPair = new AssetPair(p.getAsset(), "USDT");
            if (!pairsSet.contains(usdtPair) && !usdtPair.getAsset().equals(usdtPair.getQuote())) {
                pairsSetToAdd.add(usdtPair);
            }
        });

        pairsSet.addAll(pairsSetToAdd);

        for (String table : new String[]{"binance"}) {
            for (AssetPair pair : pairsSet) {
                List<List<Object>> data = null;
                this.storageService.createPartition("currency", table, pair.getAsset(), pair.getQuote());
                Date currentMaxDate = startDate;

                System.out.format("!!!! %s - %s", currentMaxDate, startDate);
                calendar.setTime(new Date(Math.max(
                        startDate.getTime(),
                        currentMaxDate.getTime()
                )));
                Date maxDate = calendar.getTime();
                Map<String, Map<String, String>> output = new HashMap<>();
                String exchange = table;
                do {
                    Logger.getAnonymousLogger().log(Level.INFO, String.format("Querying %s %s-%s for %s",
                            table, pair.getAsset(), pair.getQuote(), maxDate));
                    try {
                        exchange = table;
                        data = this.binanceRatesClient.loadContents(pair.getAsset(), pair.getQuote(), maxDate, 1000);
                    } catch (Exception e) {
                        Logger.getAnonymousLogger().log(Level.SEVERE, String.format("%s - %s pair does not exist here, trying EXMO", pair.getAsset(), pair.getQuote()));
                        try {
                            exchange = "exmo";
                            data = this.exmoRatesClient.loadContents(pair.getAsset(), pair.getQuote(), maxDate, 1000);
                        } catch (Exception e2) {
                            Logger.getAnonymousLogger().log(Level.SEVERE, String.format("%s - %s pair does not exist here, try another exchange", pair.getAsset(), pair.getQuote()));
                            data = new ArrayList<>();
                            continue;
                        }
                    }
                    output = Binance.klines2CSVMap(data, output);

                    Logger.getAnonymousLogger().log(Level.INFO, String.format("Got %s for %s",
                            data.size(), format.format(maxDate)));

                    if (data.size() > 0) {
                        Date lastDate = new Date((Long) data.stream().max(
                                (o1, o2) -> (Long) o1.get(6) >= (Long) o2.get(6) ? 1 : -1).get().get(6)
                        );
                        calendar.setTime(lastDate);
                        calendar.add(Calendar.DATE, 1);

                        Logger.getAnonymousLogger().log(Level.INFO, String.format("Last date: %s, New start date: %s",
                                format.format(lastDate), format.format(maxDate)));
                    } else {
                        calendar.add(Calendar.DATE, 1000);
                    }
                    maxDate = calendar.getTime();
                } while (data.size() > 0 || maxDate.getTime() < (new Date()).getTime());
                this.storageService.storeAsCsvFile("currency", exchange, pair.getAsset(), pair.getQuote(), output);
            }
        }
    }
}