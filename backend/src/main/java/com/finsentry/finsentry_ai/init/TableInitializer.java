package com.finsentry.finsentry_ai.init;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
@Order(1)
public class TableInitializer implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(TableInitializer.class);
    private static final String CREATE_TABLE_SQL = """
        CREATE TABLE IF NOT EXISTS transactions (
            id                 BIGSERIAL PRIMARY KEY,
            step               INTEGER         NOT NULL,
            type               VARCHAR(20)     NOT NULL,
            amount             NUMERIC(18,2)   NOT NULL,
            name_orig          VARCHAR(30)     NOT NULL,
            oldbalance_org     NUMERIC(18,2)   NOT NULL,
            newbalance_orig    NUMERIC(18,2)   NOT NULL,
            name_dest          VARCHAR(30)     NOT NULL,
            oldbalance_dest    NUMERIC(18,2)   NOT NULL,
            newbalance_dest    NUMERIC(18,2)   NOT NULL,
            is_fraud           BOOLEAN         NOT NULL,
            is_flagged_fraud   BOOLEAN         NOT NULL
        )
        """;

    private static final String CREATE_INDEXES_SQL = """
        CREATE INDEX IF NOT EXISTS idx_transactions_type ON transactions (type);
        CREATE INDEX IF NOT EXISTS idx_transactions_name_orig ON transactions (name_orig);
        CREATE INDEX IF NOT EXISTS idx_transactions_is_fraud ON transactions (is_fraud);
        """;

    private final JdbcTemplate jdbcTemplate;

    public TableInitializer(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void run(String... args) throws Exception {
        log.info("Ensuring 'transactions' table exists...");
        jdbcTemplate.execute(CREATE_TABLE_SQL);
        jdbcTemplate.execute(CREATE_INDEXES_SQL);
        log.info("Table check complete.");

    }
}
