package com.finsentry.finsentry_ai.init;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.*;

/**
 * Prepares the entire database on first startup — schema, the transaction
 * dataset, and every table derived from it. One class, one run() method,
 * top-to-bottom in the order each step actually needs. No @Order juggling
 * between classes, and no ambiguity about what runs before what.
 *
 * Every step independently checks "already done?" before doing work, so
 * this is safe to run against a partially-seeded database too, not just
 * a fully empty one.
 *
 * Fixed random seed (42) — the generated dataset is identical on every
 * machine that runs this repo. ~5,000 synthetic transactions, ~4% fraud
 * (deliberately higher than real PaySim's ~0.13%, so a small dataset still
 * gives meaningful demo/evaluation coverage).
 *
 * NOTE: PolicyDocumentLoader (rag/) stays separate — different concern
 * (PDF ingestion for RAG), not part of the relational schema this class
 * owns.
 */
@Component
public class DatabaseInitializer implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DatabaseInitializer.class);

    private static final long RANDOM_SEED = 42L;
    private static final int TRANSACTION_COUNT = 5000;
    private static final double FRAUD_RATE = 0.04;

    private static final String[] LEGIT_TYPES = {"PAYMENT", "TRANSFER", "CASH_OUT", "CASH_IN", "DEBIT"};
    private static final String[] FRAUD_TYPES = {"TRANSFER", "CASH_OUT"};
    private static final String[] COUNTRIES = {"US", "GB", "CA", "DE", "FR", "AU", "SG", "JP"};
    private static final String[] RISK_LEVELS = {"LOW", "MEDIUM", "HIGH"};
    private static final String[] DEVICE_TYPES = {"MOBILE", "DESKTOP", "TABLET"};
    private static final String[] MERCHANT_CATEGORIES = {"RETAIL", "ONLINE", "TRAVEL", "GAMBLING", "CRYPTO", "UTILITIES"};
    private static final String[] MERCHANT_COUNTRIES = {"US", "GB", "CA", "DE", "FR", "AU", "SG", "JP", "RU", "NG"};
    private static final String[] FRAUD_COUNTRIES = {"RU", "NG", "CN", "BR", "VN"};

    private final JdbcTemplate jdbcTemplate;
    private final Random random = new Random(RANDOM_SEED);

    public DatabaseInitializer(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void run(String... args) {
        log.info("Preparing database...");

        createTransactionsTable();
        createCaseTables();
        createTransactionView();
        generateTransactionsIfEmpty();
        generateSyntheticDataIfEmpty();

        log.info("Database ready.");
    }

    // ---------------------------------------------------------------
    // 1. Schema
    // ---------------------------------------------------------------

    private void createTransactionsTable() {
        jdbcTemplate.execute("""
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
                """);
        jdbcTemplate.execute("CREATE INDEX IF NOT EXISTS idx_transactions_type ON transactions (type)");
        jdbcTemplate.execute("CREATE INDEX IF NOT EXISTS idx_transactions_name_orig ON transactions (name_orig)");
        jdbcTemplate.execute("CREATE INDEX IF NOT EXISTS idx_transactions_is_fraud ON transactions (is_fraud)");
    }

    private void createCaseTables() {
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
    }

    private void createTransactionView() {
        // CREATE OR REPLACE is naturally idempotent — no guard needed.
        // Excludes is_fraud/is_flagged_fraud: every AI-facing tool/service
        // reads from this view, never the raw transactions table.
        jdbcTemplate.execute("""
                CREATE OR REPLACE VIEW transactions_investigation_view AS
                SELECT
                    id, step, type, amount,
                    name_orig, oldbalance_org, newbalance_orig,
                    name_dest, oldbalance_dest, newbalance_dest
                FROM transactions
                """);
    }

    private void createSyntheticDataTables() {
        jdbcTemplate.execute("""
                CREATE TABLE IF NOT EXISTS customers (
                    customer_id                   VARCHAR(30) PRIMARY KEY,
                    name                           VARCHAR(100) NOT NULL,
                    home_country                   VARCHAR(2)   NOT NULL,
                    account_created_at             TIMESTAMP    NOT NULL,
                    customer_risk_level            VARCHAR(10)  NOT NULL,
                    average_monthly_transactions   INTEGER      NOT NULL
                )
                """);

        jdbcTemplate.execute("""
                CREATE TABLE IF NOT EXISTS devices (
                    device_id       BIGSERIAL PRIMARY KEY,
                    customer_id     VARCHAR(30) NOT NULL REFERENCES customers(customer_id),
                    device_type     VARCHAR(20) NOT NULL,
                    first_seen_at   TIMESTAMP   NOT NULL,
                    trusted         BOOLEAN     NOT NULL DEFAULT TRUE
                )
                """);

        jdbcTemplate.execute("""
                CREATE TABLE IF NOT EXISTS login_history (
                    login_id          BIGSERIAL PRIMARY KEY,
                    customer_id        VARCHAR(30) NOT NULL REFERENCES customers(customer_id),
                    device_id          BIGINT      NOT NULL REFERENCES devices(device_id),
                    transaction_id      BIGINT      REFERENCES transactions(id),
                    login_timestamp     TIMESTAMP   NOT NULL,
                    country              VARCHAR(2)  NOT NULL,
                    ip_address            VARCHAR(45) NOT NULL,
                    successful            BOOLEAN     NOT NULL DEFAULT TRUE
                )
                """);

        jdbcTemplate.execute("""
                CREATE TABLE IF NOT EXISTS merchants (
                    merchant_id         VARCHAR(30) PRIMARY KEY,
                    merchant_name       VARCHAR(100) NOT NULL,
                    merchant_category   VARCHAR(50)  NOT NULL,
                    country              VARCHAR(2)   NOT NULL,
                    risk_category         VARCHAR(10)  NOT NULL
                )
                """);

        jdbcTemplate.execute("CREATE INDEX IF NOT EXISTS idx_devices_customer ON devices (customer_id)");
        jdbcTemplate.execute("CREATE INDEX IF NOT EXISTS idx_login_customer ON login_history (customer_id)");
        jdbcTemplate.execute("CREATE INDEX IF NOT EXISTS idx_login_transaction ON login_history (transaction_id)");
    }

    // ---------------------------------------------------------------
    // 2. Transaction data
    // ---------------------------------------------------------------

    private void generateTransactionsIfEmpty() {
        if (!isEmpty("transactions")) {
            log.info("transactions already has data — skipping.");
            return;
        }

        log.info("Generating {} synthetic transactions...", TRANSACTION_COUNT);
        List<Object[]> batch = new ArrayList<>(500);
        int inserted = 0;

        String insertSql = """
                INSERT INTO transactions
                    (step, type, amount, name_orig, oldbalance_org, newbalance_orig,
                     name_dest, oldbalance_dest, newbalance_dest, is_fraud, is_flagged_fraud)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                """;

        for (int n = 1; n <= TRANSACTION_COUNT; n++) {
            boolean isFraud = random.nextDouble() < FRAUD_RATE;
            int step = random.nextInt(744) + 1;
            String type = isFraud
                    ? FRAUD_TYPES[random.nextInt(FRAUD_TYPES.length)]
                    : LEGIT_TYPES[random.nextInt(LEGIT_TYPES.length)];
            String nameOrig = "C" + (100_000_000 + n);

            BigDecimal amount = isFraud
                    ? randomAmount(50_000, 4_600_000)
                    : randomAmount(100, 50_000);

            BigDecimal extra = isFraud ? randomAmount(0, 50_000) : randomAmount(1_000, 100_000);
            BigDecimal oldBalanceOrg = amount.add(extra);
            BigDecimal newBalanceOrig = oldBalanceOrg.subtract(amount);

            boolean toMerchant = random.nextDouble() < 0.35;
            String nameDest = toMerchant ? "M" + (200_000_000 + n) : "C" + (900_000_000 - n);

            BigDecimal oldBalanceDest = randomAmount(0, 500_000);
            BigDecimal newBalanceDest = toMerchant ? BigDecimal.ZERO : oldBalanceDest.add(amount);

            boolean isFlaggedFraud = isFraud && amount.compareTo(new BigDecimal("200000")) > 0;

            batch.add(new Object[]{
                    step, type, amount, nameOrig, oldBalanceOrg, newBalanceOrig,
                    nameDest, oldBalanceDest, newBalanceDest, isFraud, isFlaggedFraud
            });

            if (batch.size() == 500) {
                jdbcTemplate.batchUpdate(insertSql, batch);
                inserted += batch.size();
                batch.clear();
            }
        }
        if (!batch.isEmpty()) {
            jdbcTemplate.batchUpdate(insertSql, batch);
            inserted += batch.size();
        }

        Integer fraudCount = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM transactions WHERE is_fraud = TRUE", Integer.class);
        log.info("Generated {} transactions ({} fraud).", inserted, fraudCount);
    }

    // ---------------------------------------------------------------
    // 3. Customers, devices, merchants, login history
    // ---------------------------------------------------------------

    private void generateSyntheticDataIfEmpty() {
        createSyntheticDataTables();

        if (!isEmpty("customers")) {
            log.info("customers/devices/merchants/login_history already populated — skipping.");
            return;
        }

        log.info("Generating customers, devices, merchants, and login history...");

        Map<String, LocalDateTime> customerCreatedAt = generateCustomers();
        Map<String, List<Long>> trustedDevicesByCustomer = generateDevices(customerCreatedAt);
        int merchantCount = generateMerchants();
        int loginCount = generateLoginHistory(trustedDevicesByCustomer);

        log.info("Generated {} customers, {} merchants, {} login history rows.",
                customerCreatedAt.size(), merchantCount, loginCount);
    }

    private Map<String, LocalDateTime> generateCustomers() {
        List<String> nameOrigs = jdbcTemplate.queryForList(
                "SELECT DISTINCT name_orig FROM transactions", String.class);

        Map<String, LocalDateTime> createdAtByCustomer = new LinkedHashMap<>();
        List<Object[]> batch = new ArrayList<>();

        for (String customerId : nameOrigs) {
            LocalDateTime createdAt = LocalDateTime.now().minusDays(random.nextInt(1825));
            createdAtByCustomer.put(customerId, createdAt);
            batch.add(new Object[]{
                    customerId,
                    "Customer " + customerId.substring(1, Math.min(9, customerId.length())),
                    COUNTRIES[random.nextInt(COUNTRIES.length)],
                    createdAt,
                    RISK_LEVELS[random.nextInt(RISK_LEVELS.length)],
                    random.nextInt(30) + 1
            });
        }

        jdbcTemplate.batchUpdate("""
                INSERT INTO customers (customer_id, name, home_country, account_created_at, customer_risk_level, average_monthly_transactions)
                VALUES (?, ?, ?, ?, ?, ?)
                """, batch);

        return createdAtByCustomer;
    }

    private Map<String, List<Long>> generateDevices(Map<String, LocalDateTime> customerCreatedAt) {
        List<Object[]> batch = new ArrayList<>();

        for (var entry : customerCreatedAt.entrySet()) {
            int deviceCount = 1 + random.nextInt(2);
            for (int i = 0; i < deviceCount; i++) {
                LocalDateTime firstSeenAt = entry.getValue().plusDays(random.nextInt(30));
                batch.add(new Object[]{
                        entry.getKey(),
                        DEVICE_TYPES[random.nextInt(DEVICE_TYPES.length)],
                        firstSeenAt,
                        true
                });
            }
        }

        jdbcTemplate.batchUpdate("""
                INSERT INTO devices (customer_id, device_type, first_seen_at, trusted)
                VALUES (?, ?, ?, ?)
                """, batch);

        Map<String, List<Long>> trustedDevices = new HashMap<>();
        jdbcTemplate.query("SELECT customer_id, device_id FROM devices WHERE trusted = TRUE", rs -> {
            String customerId = rs.getString("customer_id");
            long deviceId = rs.getLong("device_id");
            trustedDevices.computeIfAbsent(customerId, k -> new ArrayList<>()).add(deviceId);
        });
        return trustedDevices;
    }

    private int generateMerchants() {
        List<String> merchantIds = jdbcTemplate.queryForList(
                "SELECT DISTINCT name_dest FROM transactions WHERE name_dest LIKE 'M%'", String.class);

        List<Object[]> batch = new ArrayList<>();
        for (String merchantId : merchantIds) {
            batch.add(new Object[]{
                    merchantId,
                    "Merchant " + merchantId.substring(1, Math.min(9, merchantId.length())),
                    MERCHANT_CATEGORIES[random.nextInt(MERCHANT_CATEGORIES.length)],
                    MERCHANT_COUNTRIES[random.nextInt(MERCHANT_COUNTRIES.length)],
                    RISK_LEVELS[random.nextInt(RISK_LEVELS.length)]
            });
        }

        jdbcTemplate.batchUpdate("""
                INSERT INTO merchants (merchant_id, merchant_name, merchant_category, country, risk_category)
                VALUES (?, ?, ?, ?, ?)
                """, batch);

        return batch.size();
    }

    private int generateLoginHistory(Map<String, List<Long>> trustedDevicesByCustomer) {
        record TxRow(long id, String nameOrig, int step, boolean isFraud, String homeCountry) {
        }

        List<TxRow> transactions = jdbcTemplate.query("""
                SELECT t.id, t.name_orig, t.step, t.is_fraud, c.home_country
                FROM transactions t
                JOIN customers c ON c.customer_id = t.name_orig
                """, (rs, rowNum) -> new TxRow(
                rs.getLong("id"), rs.getString("name_orig"), rs.getInt("step"),
                rs.getBoolean("is_fraud"), rs.getString("home_country")
        ));

        int count = 0;
        for (TxRow tx : transactions) {
            LocalDateTime loginTime = LocalDateTime.now()
                    .minusHours(744 - tx.step())
                    .minusMinutes(random.nextInt(10));

            long deviceId;
            String country;

            if (tx.isFraud()) {
                deviceId = jdbcTemplate.queryForObject("""
                        INSERT INTO devices (customer_id, device_type, first_seen_at, trusted)
                        VALUES (?, ?, ?, FALSE)
                        RETURNING device_id
                        """, Long.class, tx.nameOrig(), DEVICE_TYPES[random.nextInt(DEVICE_TYPES.length)], loginTime);
                country = FRAUD_COUNTRIES[random.nextInt(FRAUD_COUNTRIES.length)];
            } else {
                List<Long> trusted = trustedDevicesByCustomer.getOrDefault(tx.nameOrig(), List.of());
                deviceId = trusted.isEmpty() ? -1 : trusted.get(random.nextInt(trusted.size()));
                country = tx.homeCountry();
            }

            if (deviceId == -1) continue;

            String ipAddress = (random.nextInt(223) + 1) + "." + random.nextInt(255) + "."
                    + random.nextInt(255) + "." + (random.nextInt(253) + 1);

            jdbcTemplate.update("""
                    INSERT INTO login_history (customer_id, device_id, transaction_id, login_timestamp, country, ip_address, successful)
                    VALUES (?, ?, ?, ?, ?, ?, TRUE)
                    """, tx.nameOrig(), deviceId, tx.id(), loginTime, country, ipAddress);

            count++;
        }

        return count;
    }

    // ---------------------------------------------------------------
    // Shared helpers
    // ---------------------------------------------------------------

    private boolean isEmpty(String table) {
        Boolean exists = jdbcTemplate.queryForObject(
                "SELECT EXISTS (SELECT 1 FROM " + table + " LIMIT 1)", Boolean.class);
        return exists == null || !exists;
    }

    private BigDecimal randomAmount(long min, long max) {
        double value = min + random.nextDouble() * (max - min);
        return BigDecimal.valueOf(value).setScale(2, RoundingMode.HALF_UP);
    }
}