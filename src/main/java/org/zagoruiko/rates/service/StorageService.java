package org.zagoruiko.rates.service;

import java.io.IOException;
import java.util.List;
import java.util.Map;

public interface StorageService {
    void storeAsCsvFile(String bucket, String table, String asset, String quote,
                        Map<String, Map<String, String>> output) throws IOException;

    void createPartition(String bucket, String table, String asset, String quote) throws IOException;

    void prepareTableFolder(String bucket, String table) throws IOException;
}
