package org.zagoruiko.rates.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Service;
import org.zagoruiko.rates.dto.ExchangePairDTO;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

@Service
public class PortfolioServiceImpl implements PortfolioService {
    private JdbcTemplate jdbcTemplate;

    public PortfolioServiceImpl(@Autowired JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public List<ExchangePairDTO> getAllPairs() {
        return jdbcTemplate.query("SELECT DISTINCT exchange, \"baseAsset\", \"quoteAsset\" FROM trades.trade_history",
                (rs, rowNum) -> new ExchangePairDTO(rs.getString(1), rs.getString(2), rs.getString(3)));
    }
}
