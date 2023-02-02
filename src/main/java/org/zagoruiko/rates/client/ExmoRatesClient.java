package org.zagoruiko.rates.client;

import org.codehaus.jackson.map.ObjectMapper;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.zagoruiko.rates.util.Exmo;

import java.io.IOException;
import java.util.*;

@Component("exmoRatesClient")
public class ExmoRatesClient implements RatesClient {

    private RestTemplate restTemplate;

    public ExmoRatesClient(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    @Override
    public List<List<Object>> loadContents(String asset, String quote, Date from, int limit) throws IOException {
        //https://api.exmo.com/v1.1/candles_history?symbol=BTC_USD&resolution=D&from=1585556979&to=1685557979
        Date to = new Date(from.getTime() + (limit) * 24 * 60 * 60 * 1000l);
        String url = String.format("https://api.exmo.com/v1.1/candles_history?symbol=%s_%s&resolution=D&from=%s&to=%s",
                asset, quote, from.getTime() / 1000, to.getTime() / 1000);
        System.out.println(String.format("EXMO queried %s_%s from %s (%s) to %s (%s)", asset, quote, from.getTime() / 1000, from, to.getTime() / 1000, to));
        Map<Object, Object> res = new ObjectMapper().readValue(restTemplate.getForObject(url, String.class), Map.class);
        if (!res.containsKey("candles")) {
            return new ArrayList<>();
        }
        return Exmo.exmoDataToBinanceData((ArrayList) new ObjectMapper().readValue(restTemplate.getForObject(url, String.class), Map.class).get("candles"));
    }
}
