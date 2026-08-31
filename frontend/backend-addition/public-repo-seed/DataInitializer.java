package com.finsentry.finsentry_ai.init;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Generates a small, self-contained PaySim-shaped transaction dataset
 * directly in Java — no CSV file required. This means anyone who clones
 * the repo can run the app immediately with zero data setup.
 *
 * Fixed random seed (42) so the generated dataset is identical across
 * every machine that runs it — reproducible demos, reproducible screenshots.
 *
 * Fraud rate is deliberately ~4% (vs. real PaySim's ~0.13%) so a small
 * dataset still gives meaningful demo/evaluation coverage. Fraud
 * transactions are always TRANSFER or CASH_OUT and fully drain the origin
 * balance — matching real PaySim's actual fraud pattern.
 */
@Component
@Order(2)
public class DataInitializer implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);

    private static final String TABLE = "transactions";
    private static final int TRANSACTION_COUNT = 5000;
    private static final double FRAUD_RATE = 0.04;
    private static final long RANDOM_SEED = 42L;
    private static final int BATCH_SIZE = 500;

    private static final String[] LEGIT_TYPES = {"PAYMENT", "TRANSFER", "CASH_OUT", "CASH_IN", "DEBIT"};
    private static final String[] FRAUD_TYPES = {"TRANSFER", "CASH_OUT"};

    private static final String INSERT_SQL = """
        INSERT INTO transactions
            (step, type, amount, name_orig, oldbalance_org, newbalance_orig,
             name_dest, oldbalance_dest, newbalance_dest, is_fraud, is_flagged_fraud)
        VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
        """;

    private final JdbcTemplate jdbcTemplate;

    public DataInitializer(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void run(String... args) {
        if (isTableEmpty()) {
            log.info("Table '{}' is empty. Generating {} synthetic transactions...", TABLE, TRANSACTION_COUNT);
            int inserted = generateTransactions();
            log.info("Generated {} transactions ({} fraud) into '{}'.", inserted, countFraud(), TABLE);
        } else {
            log.info("Table '{}' already has data. Skipping generation.", TABLE);
        }
    }

    private boolean isTableEmpty() {
        Boolean exists = jdbcTemplate.queryForObject(
                "SELECT EXISTS (SELECT 1 FROM " + TABLE + " LIMIT 1)", Boolean.class);
        return exists == null || !exists;
    }

    private int generateTransactions() {
        Random random = new Random(RANDOM_SEED);
        List<Object[]> batch = new ArrayList<>(BATCH_SIZE);
        int totalInserted = 0;

        for (int n = 1; n <= TRANSACTION_COUNT; n++) {
            boolean isFraud = random.nextDouble() < FRAUD_RATE;

            int step = random.nextInt(744) + 1;
            String type = isFraud
                    ? FRAUD_TYPES[random.nextInt(FRAUD_TYPES.length)]
                    : LEGIT_TYPES[random.nextInt(LEGIT_TYPES.length)];

            String nameOrig = "C" + (100_000_000 + n);

            BigDecimal amount = isFraud
                    ? randomAmount(random, 50_000, 4_600_000)
                    : randomAmount(random, 100, 50_000);

            // Guarantee oldbalance_org always covers the amount, so the
            // balance-drain pattern (newbalance_orig = 0 for fraud) is
            // always coherent — no negative balances, ever.
            BigDecimal extra = isFraud
                    ? randomAmount(random, 0, 50_000)
                    : randomAmount(random, 1_000, 100_000);
            BigDecimal oldBalanceOrg = amount.add(extra);
            BigDecimal newBalanceOrig = oldBalanceOrg.subtract(amount);

            boolean toMerchant = random.nextDouble() < 0.35;
            String nameDest = toMerchant
                    ? "M" + (200_000_000 + n)
                    : "C" + (900_000_000 - n);

            BigDecimal oldBalanceDest = randomAmount(random, 0, 500_000);
            BigDecimal newBalanceDest = toMerchant ? BigDecimal.ZERO : oldBalanceDest.add(amount);

            boolean isFlaggedFraud = isFraud && amount.compareTo(new BigDecimal("200000")) > 0;

            batch.add(new Object[]{
                    step, type, amount, nameOrig, oldBalanceOrg, newBalanceOrig,
                    nameDest, oldBalanceDest, newBalanceDest, isFraud, isFlaggedFraud
            });

            if (batch.size() == BATCH_SIZE) {
                jdbcTemplate.batchUpdate(INSERT_SQL, batch);
                totalInserted += batch.size();
                batch.clear();
            }
        }

        if (!batch.isEmpty()) {
            jdbcTemplate.batchUpdate(INSERT_SQL, batch);
            totalInserted += batch.size();
        }

        return totalInserted;
    }

    private BigDecimal randomAmount(Random random, long min, long max) {
        double value = min + random.nextDouble() * (max - min);
        return BigDecimal.valueOf(value).setScale(2, RoundingMode.HALF_UP);
    }

    private Integer countFraud() {
        return jdbcTemplate.queryForObject("SELECT COUNT(*) FROM " + TABLE + " WHERE is_fraud = TRUE", Integer.class);
    }
}
