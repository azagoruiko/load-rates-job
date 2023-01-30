package org.zagoruiko.rates.service;

import java.io.IOException;
import java.util.List;
import java.util.Map;

public interface StorageService {
    void storeAsCsvFile(String bucket, String exchange, String asset, String quote,
                        Map<String, Map<String, String>> output) throws IOException;

    void createPartition(String bucket, String exchange, String asset, String quote) throws IOException;

    void prepareTableFolder(String bucket, String exchange) throws IOException;
}
