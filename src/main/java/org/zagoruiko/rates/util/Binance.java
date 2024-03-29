package org.zagoruiko.rates.util;

import com.amazonaws.services.s3.AmazonS3;

import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

public class Binance {
    private static Format format = new SimpleDateFormat("yyyy-MM-dd");

    private static String convertTime(long time){
        Date date = new Date(time);
        return format.format(date);
    }
    public static Map<String, Map<String, String>> klines2CSVMap(List<List<Object>> data,
                                                          Map<String, Map<String, String>> output) {
        List<String> rawList = data.stream().map(v ->
                        String.format("%s,%s,%s,%s,%s",
                                convertTime((Long)v.get(0)),
                                v.get(2), v.get(3), v.get(1), v.get(4)))
                .collect(Collectors.toList());

        for (String line : rawList) {
            output.putIfAbsent(line.substring(0,7), new HashMap<>());
            output.get(line.substring(0,7)).put(line.substring(0,10), line);
        }
        return output;
    }
}
