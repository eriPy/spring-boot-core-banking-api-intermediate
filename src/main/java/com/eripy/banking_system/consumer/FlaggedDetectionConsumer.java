package com.eripy.banking_system.consumer;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import com.eripy.banking_system.dto.transactions.FlaggedDetail;
import com.eripy.banking_system.dto.transactions.TransactionDetail;
import com.eripy.banking_system.entities.transactions.DepositTransaction;
import com.eripy.banking_system.entities.transactions.TransferTransaction;
import com.eripy.banking_system.event.UserBehaviorAnalyzedEvent;
import com.eripy.banking_system.repositories.transactions.DepositTransactionRepository;
import com.eripy.banking_system.repositories.transactions.TransferTransactionRepository;

@Component 
public class FlaggedDetectionConsumer {
    private final DepositTransactionRepository depositTransactionRepository;
    private final TransferTransactionRepository transferTransactionRepository;
    private static final Logger logger = LoggerFactory.getLogger(FlaggedDetectionConsumer.class);
    private static final BigDecimal THREE = new BigDecimal("3");
    private static final BigDecimal FOUR = new BigDecimal("4");

    public FlaggedDetectionConsumer(
        DepositTransactionRepository depositTransactionRepository,
        TransferTransactionRepository transferTransactionRepository
    ) {
        this.depositTransactionRepository = depositTransactionRepository;
        this.transferTransactionRepository = transferTransactionRepository;
    }

    @Transactional 
    @KafkaListener(topics = "user-analized", groupId = "flagged")
    public void analizingFlagged(UserBehaviorAnalyzedEvent event) {
        if (event.account().getAccountType().getAccountType().equals("fixed_deposit_account")) return;
        logger.info("Evaluating the last transactions from the last 3 months");
        List<DepositTransaction> deposits = depositTransactionRepository
            .findLastPeriod(
                LocalDateTime.now().minusMonths(3), 
                event.account()
            );
        List<TransferTransaction> transfers = transferTransactionRepository.findLastPeriod(
            event.account(), 
            LocalDateTime.now().minusMonths(3)
        );
        logger.info("Evaluating last earnings");
        FlaggedDetail depositDetail = getDepositDetail(
            new BigDecimal("0"), 
            new BigDecimal("0"), 
            new BigDecimal("0"), 
            deposits, 
            LocalDateTime.now().minusMonths(1)
        );
        FlaggedDetail transferDetail = getTransferDetail(
            new BigDecimal("0"), 
            new BigDecimal("0"), 
            new BigDecimal("0"), 
            transfers, 
            LocalDateTime.now().minusMonths(1)
        );
        BigDecimal earnings = event.account().getMonthlyEarnings();
        BigDecimal max = depositDetail.max().add(transferDetail.max());
        BigDecimal monthly = depositDetail.monthly().add(transferDetail.monthly());
        BigDecimal period = depositDetail.period().add(transferDetail.period());
        BigDecimal maxWarning = earnings.multiply(THREE);
        BigDecimal periodWarning = earnings.multiply(THREE).multiply(THREE);
        BigDecimal maxBan = earnings.multiply(FOUR);
        BigDecimal periodBan = earnings.multiply(FOUR).multiply(FOUR);
        logger.info("Evaluating whether the user will receive a warning or a ban");
        boolean warning = max.compareTo(maxWarning) >= 0 || 
            monthly.compareTo(maxWarning) >= 0 ||
            period.compareTo(periodWarning) >= 0;
        boolean ban = max.compareTo(maxBan) >= 0 || 
            monthly.compareTo(maxBan) >= 0 ||
            period.compareTo(periodBan) >= 0;
        if (ban) {
            return;
        }
        if (warning) {
            return;
        }
        logger.info("The user will not be penalized.");
    }

    private <T extends TransactionDetail> FlaggedDetail getFormat(
        BigDecimal max, 
        BigDecimal monthly, 
        BigDecimal period, 
        List<T> transactions, 
        LocalDateTime thisMonth
    ) {
        for (T transaction: transactions) {
            BigDecimal current = transaction.getTransaction().getAmount();
            LocalDateTime date = transaction.getTransaction().getTransactionDate();
            if (max.compareTo(current) < 0) max = current;
            if (date.isAfter(thisMonth)) monthly = monthly.add(current);
            period = period.add(current);
        }
        return new FlaggedDetail(max, monthly, period);
    }

    private FlaggedDetail getDepositDetail(
        BigDecimal max, 
        BigDecimal monthly, 
        BigDecimal period, 
        List<DepositTransaction> deposits, 
        LocalDateTime thisMonth
    ) {
        return getFormat(max, monthly, period, deposits, thisMonth);
    }

    private FlaggedDetail getTransferDetail(
        BigDecimal max, 
        BigDecimal monthly, 
        BigDecimal period, 
        List<TransferTransaction> deposits, 
        LocalDateTime thisMonth
    ) {
        return getFormat(max, monthly, period, deposits, thisMonth);
    }
}
