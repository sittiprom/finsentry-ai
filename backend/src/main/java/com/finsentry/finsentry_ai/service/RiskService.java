package com.finsentry.finsentry_ai.service;

import com.finsentry.finsentry_ai.api.RiskIndicators;
import com.finsentry.finsentry_ai.api.RiskIndicators.RiskLevel;
import com.finsentry.finsentry_ai.entity.*;
import com.finsentry.finsentry_ai.repository.*;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

@Service
public class RiskService {

    // Thresholds — mirror the fraud policy document (Section 2 & 3)
    private static final BigDecimal AMOUNT_ANOMALY_THRESHOLD = new BigDecimal("5");   // POLICY-TRANSACTION-002
    private static final BigDecimal AMOUNT_SEVERE_THRESHOLD = new BigDecimal("10");   // POLICY-TRANSACTION-003
    private static final BigDecimal BALANCE_DRAIN_THRESHOLD = new BigDecimal("0.9");  // POLICY-TRANSACTION-004
    private static final int RAPID_TRANSACTION_COUNT_THRESHOLD = 3;                    // POLICY-TRANSACTION-005

    private final TransactionInvestigationViewRepository transactionRepository;
    private final LoginHistoryRepository loginHistoryRepository;
    private final DeviceRepository deviceRepository;

    public RiskService(TransactionInvestigationViewRepository transactionRepository,
                       LoginHistoryRepository loginHistoryRepository,
                       DeviceRepository deviceRepository) {
        this.transactionRepository = transactionRepository;
        this.loginHistoryRepository = loginHistoryRepository;
        this.deviceRepository = deviceRepository;
    }

    /** How many times larger this transaction is than the customer's average — POLICY-TRANSACTION-002/003 */
    public BigDecimal calculateAmountAnomaly(TransactionInvestigationView transaction) {
        List<TransactionInvestigationView> history =
        transactionRepository.findByNameOrigOrderByStepDesc(transaction.getNameOrig());

        List<BigDecimal> priorAmounts = history.stream()
                .filter(t -> !t.getId().equals(transaction.getId()))
                .map(TransactionInvestigationView::getAmount)
                .toList();

        if (priorAmounts.isEmpty()) {
            return BigDecimal.ZERO; // no history to compare against — can't judge anomaly
        }

        BigDecimal average = priorAmounts.stream()
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .divide(BigDecimal.valueOf(priorAmounts.size()), 2, RoundingMode.HALF_UP);

        if (average.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }

        return transaction.getAmount().divide(average, 2, RoundingMode.HALF_UP);
    }

    /** Was this transaction preceded by a login from an untrusted/unrecognized device? — POLICY-DEVICE-001 */
    public boolean checkNewDevice(TransactionInvestigationView transaction) {
        return loginHistoryRepository.findByTransactionId(transaction.getId())
                .map(login -> deviceRepository.findById(login.getDeviceId())
                        .map(device -> !device.isTrusted())
                        .orElse(true)) // no device record at all — treat as unknown/risky
                .orElse(false);   // no login on record — nothing to flag
    }

    /** Was the login country different from the customer's home country? — POLICY-DEVICE-002 */
    public boolean checkUnusualCountry(TransactionInvestigationView transaction, Customer customer) {
        return loginHistoryRepository.findByTransactionId(transaction.getId())
                .map(login -> !login.getCountry().equalsIgnoreCase(customer.getHomeCountry()))
                .orElse(false);
    }

    /** Three or more transactions from this customer within the same time step — POLICY-TRANSACTION-005 */
    public boolean checkRapidTransactions(TransactionInvestigationView transaction) {
        long countInSameStep = transactionRepository
                .findByNameOrigOrderByStepDesc(transaction.getNameOrig())
                .stream()
                .filter(t -> t.getStep().equals(transaction.getStep()))
                .count();
        return countInSameStep >= RAPID_TRANSACTION_COUNT_THRESHOLD;
    }

    /** Did this transaction drain more than 90% of the origin balance? — POLICY-TRANSACTION-004 */
    public boolean checkBalanceDrain(TransactionInvestigationView transaction) {
        BigDecimal oldBalance = transaction.getOldbalanceOrg();
        if (oldBalance.compareTo(BigDecimal.ZERO) <= 0) {
            return false; // nothing to drain
        }
        BigDecimal drop = oldBalance.subtract(transaction.getNewbalanceOrig());
        BigDecimal dropRatio = drop.divide(oldBalance, 4, RoundingMode.HALF_UP);
        return dropRatio.compareTo(BALANCE_DRAIN_THRESHOLD) >= 0;
    }

    /** Deterministic weighted score, 0-100. The LLM never computes this — Java does (spec Section 21). */
    public int calculateRiskScore(BigDecimal amountAnomaly, boolean newDevice, boolean unusualCountry,
                                  boolean rapidTransactions, boolean balanceDrained) {
        int score = 0;

        if (amountAnomaly.compareTo(AMOUNT_SEVERE_THRESHOLD) >= 0) {
            score += 40;
        } else if (amountAnomaly.compareTo(AMOUNT_ANOMALY_THRESHOLD) >= 0) {
            score += 25;
        }

        if (newDevice) score += 25;
        if (unusualCountry) score += 25;
        if (rapidTransactions) score += 25;
        if (balanceDrained) score += 30;

        return Math.min(score, 100);
    }

    /** Maps score to LOW/MEDIUM/HIGH per the policy doc's risk-level table (Section 5) */
    public RiskLevel determineRiskLevel(int riskScore) {
        if (riskScore >= 70) return RiskLevel.HIGH;
        if (riskScore >= 35) return RiskLevel.MEDIUM;
        return RiskLevel.LOW;
    }

    /** Runs everything above and assembles the final RiskIndicators — this is what RiskTools calls */
    public RiskIndicators calculateRiskIndicators(TransactionInvestigationView transaction, Customer customer) {
        BigDecimal amountAnomaly = calculateAmountAnomaly(transaction);
        boolean newDevice = checkNewDevice(transaction);
        boolean unusualCountry = checkUnusualCountry(transaction, customer);
        boolean rapidTransactions = checkRapidTransactions(transaction);
        boolean balanceDrained = checkBalanceDrain(transaction);

        int score = calculateRiskScore(amountAnomaly, newDevice, unusualCountry, rapidTransactions, balanceDrained);
        RiskLevel level = determineRiskLevel(score);

        return new RiskIndicators(
                amountAnomaly, newDevice, unusualCountry, rapidTransactions, balanceDrained, score, level
        );
    }
}