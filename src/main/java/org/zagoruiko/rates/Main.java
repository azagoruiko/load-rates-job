package org.zagoruiko.rates;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.PropertySource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.zagoruiko.rates.client.BinanceRatesClient;
import org.zagoruiko.rates.service.StorageService;
import org.zagoruiko.rates.util.Binance;

import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.text.Format;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

@Component
@PropertySource(value = "classpath:application.properties")
public class Main {

    private static Format format = new SimpleDateFormat("yyyy-MM-dd");
    private static Calendar calendar = Calendar.getInstance();
    private BinanceRatesClient binanceRatesClient;

    private StorageService storageService;

    @Autowired
    public void setBinanceRatesClient(BinanceRatesClient binanceRatesClient) {
        this.binanceRatesClient = binanceRatesClient;
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

        Date today = new Date();
        for (String table : new String[]{"binance"}) {
            for (String[] pair : new String[][]{
                    new String[]{"BTC", "USDT"},
                    new String[]{"SOL", "USDT"},
                    new String[]{"SOL", "BTC"},
                    new String[]{"SOL", "ETH"},
                    new String[]{"NEAR", "USDT"},
                    new String[]{"NEAR", "BTC"},
                    new String[]{"MATIC", "USDT"},
                    new String[]{"MATIC", "BTC"},
                    new String[]{"USDT", "UAH"},
                    new String[]{"ETH", "USDT"},
                    new String[]{"EUR", "USDT"},
                    new String[]{"ETH", "BTC"}
            }) {
                List<List<Object>> data = null;
                this.storageService.createPartition("currency", table, pair[0], pair[1]);
                Date currentMaxDate = startDate;

                System.out.format("!!!! %s - %s", currentMaxDate, startDate);
                calendar.setTime(new Date(Math.max(
                        startDate.getTime(),
                        currentMaxDate.getTime()
                )));
                Date maxDate = calendar.getTime();
                Map<String, List<String>> output = new HashMap<>();
                do {
                    Logger.getAnonymousLogger().log(Level.INFO, String.format("Querying %s %s-%s for %s",
                            table, pair[0], pair[1], maxDate));
                    data = this.binanceRatesClient.loadContents(pair[0], pair[1], maxDate, 1000);
                    output = Binance.klines2CSVMap(data, output);

                    Logger.getAnonymousLogger().log(Level.INFO, String.format("Got %s for %s",
                            data.size(), format.format(maxDate)));

                    if (data.size() > 0) {
                        Date lastDate = new Date((Long) data.stream().max(
                                (o1, o2) -> (Long) o1.get(6) >= (Long) o2.get(6) ? 1 : -1).get().get(6)
                        );
                        calendar.setTime(lastDate);

                        calendar.add(Calendar.DATE, 1);
                        maxDate = calendar.getTime();
                        Logger.getAnonymousLogger().log(Level.INFO, String.format("Last date: %s, New start date: %s",
                                format.format(lastDate), format.format(maxDate)));
                    }
                } while (data.size() > 0);
                this.storageService.storeAsCsvFile("currency", table, pair[0], pair[1], output);
            }
        }
    }
}