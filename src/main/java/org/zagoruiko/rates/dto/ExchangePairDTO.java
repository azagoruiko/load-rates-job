package org.zagoruiko.rates.dto;

public class ExchangePairDTO {
    private String exchange;
    private String asset;
    private String quote;

    public ExchangePairDTO(String exchange, String asset, String quote) {
        this.exchange = exchange;
        this.asset = asset;
        this.quote = quote;
    }

    public String getExchange() {
        return exchange;
    }

    public String getAsset() {
        return asset;
    }

    public String getQuote() {
        return quote;
    }
}
