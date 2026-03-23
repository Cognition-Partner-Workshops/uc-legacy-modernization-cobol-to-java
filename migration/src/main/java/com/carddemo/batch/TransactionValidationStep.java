package com.carddemo.batch;

import com.carddemo.entity.Transaction;
import com.carddemo.service.TransactionPostingService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * Replaces CBTRN01C.cbl validation logic.
 * Provides reusable transaction validation for batch processing.
 */
@Component
public class TransactionValidationStep {

    private static final Logger log = LoggerFactory.getLogger(TransactionValidationStep.class);

    private final TransactionPostingService transactionPostingService;

    public TransactionValidationStep(TransactionPostingService transactionPostingService) {
        this.transactionPostingService = transactionPostingService;
    }

    /**
     * Validate a list of transactions and separate into valid and rejected lists.
     */
    public ValidationOutput validate(List<Transaction> transactions) {
        List<Transaction> valid = new ArrayList<>();
        List<TransactionPostingService.RejectedTransaction> rejected = new ArrayList<>();

        for (Transaction tran : transactions) {
            TransactionPostingService.ValidationResult result =
                    transactionPostingService.validateTransaction(tran);

            if (result.reasonCode() == 0) {
                valid.add(tran);
            } else {
                rejected.add(new TransactionPostingService.RejectedTransaction(
                        tran, result.reasonCode(), result.reasonDescription()));
            }
        }

        log.info("Validation complete: {} valid, {} rejected", valid.size(), rejected.size());
        return new ValidationOutput(valid, rejected);
    }

    public record ValidationOutput(
            List<Transaction> validTransactions,
            List<TransactionPostingService.RejectedTransaction> rejectedTransactions) {}
}
