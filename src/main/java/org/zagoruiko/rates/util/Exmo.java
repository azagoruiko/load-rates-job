package org.zagoruiko.rates.util;

import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

public class Exmo {
    private static Format format = new SimpleDateFormat("yyyy-MM-dd");

    private static String convertTime(long time){
        Date date = new Date(time);
        return format.format(date);
    }

    public static List<List<Object>> exmoDataToBinanceData(List<Map<String, Object>> data) {
        return data.stream().map(x -> {
            List<Object> list = new ArrayList<>();
            list.add(x.get("t"));
            list.add(x.get("o"));
            list.add(x.get("h"));
            list.add(x.get("l"));
            list.add(x.get("c"));
            list.add(x.get("v"));
            list.add(x.get("t"));
            return list;
        }).collect(Collectors.toList());
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
