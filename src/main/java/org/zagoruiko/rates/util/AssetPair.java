package org.zagoruiko.rates.util;

import java.util.Objects;

public class AssetPair {
    private final String asset;
    private final String quote;

    public AssetPair(String baseAsset, String quoteAsset) {
        this.asset = baseAsset;
        this.quote = quoteAsset;
    }

    public String getAsset() {
        return asset;
    }

    public String getQuote() {
        return quote;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AssetPair assetPair = (AssetPair) o;
        return asset.equals(assetPair.asset) && quote.equals(assetPair.quote);
    }

    @Override
    public int hashCode() {
        return Objects.hash(asset, quote);
    }

    @Override
    public String toString() {
        return "AssetPair{" +
                "asset='" + asset + '\'' +
                ", quote='" + quote + '\'' +
                '}';
    }
}
