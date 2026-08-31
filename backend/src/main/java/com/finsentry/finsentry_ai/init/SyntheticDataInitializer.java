package com.finsentry.finsentry_ai.init;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

/**
 * Generates customers, devices, merchants, and login_history — one row of
 * "case history" per transaction — entirely in Java on top of whatever
 * DataInitializer produced. Same fixed seed (42), so the whole dataset is
 * reproducible across every machine that runs this repo.
 *
 * Fraud transactions get a brand-new, untrusted device and an unusual
 * country; legitimate transactions reuse a known trusted device and the
 * customer's home country — this is what gives RiskService's newDevice /
 * unusualCountry checks (and RAG's policy citations) something real to
 * detect, rather than just echoing is_fraud.
 */
@Component
@Order(3)
public class SyntheticDataInitializer implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(SyntheticDataInitializer.class);
    private static final long RANDOM_SEED = 42L;

    private static final String[] COUNTRIES = {"US", "GB", "CA", "DE", "FR", "AU", "SG", "JP"};
    private static final String[] RISK_LEVELS = {"LOW", "MEDIUM", "HIGH"};
    private static final String[] DEVICE_TYPES = {"MOBILE", "DESKTOP", "TABLET"};
    private static final String[] MERCHANT_CATEGORIES = {"RETAIL", "ONLINE", "TRAVEL", "GAMBLING", "CRYPTO", "UTILITIES"};
    private static final String[] MERCHANT_COUNTRIES = {"US", "GB", "CA", "DE", "FR", "AU", "SG", "JP", "RU", "NG"};
    private static final String[] FRAUD_COUNTRIES = {"RU", "NG", "CN", "BR", "VN"};

    private final JdbcTemplate jdbcTemplate;
    private final Random random = new Random(RANDOM_SEED);

    public SyntheticDataInitializer(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void run(String... args) {
        createSchema();

        if (isPopulated()) {
            log.info("customers/devices/merchants/login_history already populated — skipping.");
            return;
        }

        log.info("Generating customers, devices, merchants, and login history...");

        Map<String, LocalDateTime> customerCreatedAt = generateCustomers();
        Map<String, List<Long>> trustedDevicesByCustomer = generateDevices(customerCreatedAt);
        int merchantCount = generateMerchants();
        int loginCount = generateLoginHistory(trustedDevicesByCustomer);

        log.info("Generated {} customers, merchants: {}, login history rows: {}.",
                customerCreatedAt.size(), merchantCount, loginCount);
    }

    private void createSchema() {
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

    private boolean isPopulated() {
        Boolean exists = jdbcTemplate.queryForObject(
                "SELECT EXISTS (SELECT 1 FROM customers LIMIT 1)", Boolean.class);
        return exists != null && exists;
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
            int deviceCount = 1 + random.nextInt(2); // 1 to 2 trusted devices
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

        // Build customerId -> [trusted device ids] for login generation below
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
        record TxRow(long id, String nameOrig, int step, boolean isFraud, String homeCountry) {}

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

            if (deviceId == -1) continue; // shouldn't happen, but skip defensively

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
}
