package org.zagoruiko.rates.service;

import org.zagoruiko.rates.dto.ExchangePairDTO;

import java.util.List;

public interface PortfolioService {
    List<ExchangePairDTO> getAllPairs();
}
