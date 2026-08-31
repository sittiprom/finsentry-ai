package com.finsentry.finsentry_ai.init;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

/**
 * Creates investigation_cases and investigation_reports if they don't
 * already exist. These were previously only ever created manually via
 * pgAdmin — meaning a fresh clone of this repo (or a wiped local DB)
 * would boot cleanly but fail the moment anyone ran an investigation.
 * This closes that gap, matching the same DDL-on-startup pattern as
 * TableInitializer.
 *
 * Runs first (@Order(1), same as TableInitializer) since transactions
 * must exist before the FK on investigation_cases can be created —
 * Spring runs same-@Order CommandLineRunners in an unspecified but
 * consistent order per JVM, so if you hit an FK error on first boot,
 * bump this to @Order(0) or move transactions table creation ahead of it.
 */
@Component
@Order(1)
public class CaseSchemaInitializer implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(CaseSchemaInitializer.class);

    private final JdbcTemplate jdbcTemplate;

    public CaseSchemaInitializer(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void run(String... args) {
        log.info("Ensuring investigation_cases and investigation_reports tables exist...");

        jdbcTemplate.execute("""
            CREATE TABLE IF NOT EXISTS investigation_cases (
                id              BIGSERIAL PRIMARY KEY,
                transaction_id  BIGINT NOT NULL,
                status          VARCHAR(20) NOT NULL DEFAULT 'PENDING',
                created_at      TIMESTAMP NOT NULL DEFAULT NOW(),
                completed_at    TIMESTAMP
            )
            """);

        jdbcTemplate.execute("""
            CREATE TABLE IF NOT EXISTS investigation_reports (
                id                 BIGSERIAL PRIMARY KEY,
                case_id            BIGINT NOT NULL UNIQUE REFERENCES investigation_cases(id),
                risk_level         VARCHAR(10) NOT NULL,
                risk_score         INTEGER NOT NULL,
                summary            TEXT NOT NULL,
                findings           JSONB NOT NULL,
                policy_matches     JSONB NOT NULL,
                recommendation     VARCHAR(30) NOT NULL,
                model_used         VARCHAR(50),
                execution_time_ms  INTEGER,
                created_at         TIMESTAMP NOT NULL DEFAULT NOW()
            )
            """);

        jdbcTemplate.execute("CREATE INDEX IF NOT EXISTS idx_cases_transaction ON investigation_cases (transaction_id)");
        jdbcTemplate.execute("CREATE INDEX IF NOT EXISTS idx_reports_case ON investigation_reports (case_id)");

        log.info("investigation_cases / investigation_reports ready.");
    }
}
