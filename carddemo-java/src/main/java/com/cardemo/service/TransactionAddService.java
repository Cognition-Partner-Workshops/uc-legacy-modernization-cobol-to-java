package com.cardemo.service;

import com.cardemo.model.CardXref;
import com.cardemo.model.Transaction;
import com.cardemo.repository.CardXrefRepository;
import com.cardemo.repository.TransactionRepository;
import com.cardemo.util.DateValidationUtil;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Java equivalent of COBOL program COTRN02C.
 * Validates and adds new transactions to the TRANSACT file.
 */
public class TransactionAddService {

    private final CardXrefRepository cardXrefRepository;
    private final TransactionRepository transactionRepository;
    private final DateValidationUtil dateValidationUtil;

    public TransactionAddService(CardXrefRepository cardXrefRepository,
                                  TransactionRepository transactionRepository,
                                  DateValidationUtil dateValidationUtil) {
        this.cardXrefRepository = cardXrefRepository;
        this.transactionRepository = transactionRepository;
        this.dateValidationUtil = dateValidationUtil;
    }

    /**
     * Validates the key fields of a transaction input.
     * Equivalent of COBOL VALIDATE-INPUT-KEY-FIELDS.
     *
     * @param accountId  the account ID (may be null)
     * @param cardNumber the card number (may be null)
     * @return list of validation error messages (empty if valid)
     */
    public List<String> validateKeyFields(String accountId, String cardNumber) {
        List<String> errors = new ArrayList<>();

        boolean hasAccountId = accountId != null && !accountId.isBlank();
        boolean hasCardNumber = cardNumber != null && !cardNumber.isBlank();

        if (!hasAccountId && !hasCardNumber) {
            errors.add("Account or Card Number must be entered...");
            return errors;
        }

        if (hasAccountId) {
            if (!isNumeric(accountId.trim())) {
                errors.add("Account ID must be Numeric...");
                return errors;
            }
        }

        if (hasCardNumber) {
            if (!isNumeric(cardNumber.trim())) {
                errors.add("Card Number must be Numeric...");
                return errors;
            }
        }

        return errors;
    }

    /**
     * Validates the data fields of a transaction input.
     * Equivalent of COBOL VALIDATE-INPUT-DATA-FIELDS.
     */
    public List<String> validateDataFields(String typeCode, String categoryCode,
                                            String source, String description,
                                            String amount, String originDate,
                                            String processDate, String merchantId,
                                            String merchantName, String merchantCity,
                                            String merchantZip) {
        List<String> errors = new ArrayList<>();

        // Required field checks
        validateRequired(errors, typeCode, "Type CD");
        validateRequired(errors, categoryCode, "Category CD");
        validateRequired(errors, source, "Source");
        validateRequired(errors, description, "Description");
        validateRequired(errors, amount, "Amount");
        validateRequired(errors, originDate, "Orig Date");
        validateRequired(errors, processDate, "Proc Date");
        validateRequired(errors, merchantId, "Merchant ID");
        validateRequired(errors, merchantName, "Merchant Name");
        validateRequired(errors, merchantCity, "Merchant City");
        validateRequired(errors, merchantZip, "Merchant Zip");

        if (!errors.isEmpty()) {
            return errors;
        }

        // Numeric validation for type and category codes
        if (!isNumeric(typeCode.trim())) {
            errors.add("Type CD must be Numeric...");
        }
        if (!isNumeric(categoryCode.trim())) {
            errors.add("Category CD must be Numeric...");
        }

        // Amount format validation: +/-99999999.99
        if (!isValidAmount(amount.trim())) {
            errors.add("Amount should be in format -99999999.99");
        }

        // Date format validation: YYYY-MM-DD
        if (!isValidDateFormat(originDate.trim())) {
            errors.add("Orig Date should be in format YYYY-MM-DD");
        }
        if (!isValidDateFormat(processDate.trim())) {
            errors.add("Proc Date should be in format YYYY-MM-DD");
        }

        return errors;
    }

    /**
     * Looks up the card number for an account ID via cross-reference.
     */
    public Optional<String> lookupCardNumber(String accountId) {
        if (accountId == null || accountId.isBlank()) {
            return Optional.empty();
        }
        try {
            long acctId = Long.parseLong(accountId.trim());
            return cardXrefRepository.findByAccountId(acctId)
                    .map(CardXref::getCardNumber);
        } catch (NumberFormatException e) {
            return Optional.empty();
        }
    }

    /**
     * Looks up the account ID for a card number via cross-reference.
     */
    public Optional<Long> lookupAccountId(String cardNumber) {
        if (cardNumber == null || cardNumber.isBlank()) {
            return Optional.empty();
        }
        return cardXrefRepository.findByCardNumber(cardNumber.trim())
                .map(CardXref::getAccountId);
    }

    /**
     * Adds a new transaction after validation.
     *
     * @param transaction the transaction to add
     * @param confirmed   whether the user has confirmed ('Y' or 'N')
     * @return error message, or null if successful
     */
    public String addTransaction(Transaction transaction, String confirmed) {
        if (confirmed == null || confirmed.isBlank()) {
            return "Confirm to add this transaction...";
        }

        String upperConfirmed = confirmed.toUpperCase().trim();
        if ("Y".equals(upperConfirmed)) {
            transactionRepository.save(transaction);
            return null;
        } else if ("N".equals(upperConfirmed)) {
            return "Confirm to add this transaction...";
        } else {
            return "Invalid value. Valid values are (Y/N)...";
        }
    }

    /**
     * Validates the amount format: sign + 8 digits + . + 2 digits
     * Matches COBOL: TRNAMTI(1:1) NOT EQUAL '-' AND '+'
     *                TRNAMTI(2:8) NOT NUMERIC, etc.
     */
    boolean isValidAmount(String amount) {
        if (amount == null || amount.length() < 4) {
            return false;
        }

        char sign = amount.charAt(0);
        if (sign != '+' && sign != '-') {
            return false;
        }

        int dotIndex = amount.indexOf('.');
        if (dotIndex < 0) {
            return false;
        }

        String integerPart = amount.substring(1, dotIndex);
        if (integerPart.isEmpty() || !isNumeric(integerPart)) {
            return false;
        }

        String decimalPart = amount.substring(dotIndex + 1);
        if (decimalPart.length() != 2 || !isNumeric(decimalPart)) {
            return false;
        }

        return true;
    }

    /**
     * Validates date format: YYYY-MM-DD.
     */
    boolean isValidDateFormat(String date) {
        if (date == null || date.length() != 10) {
            return false;
        }

        // Check structure: 4digits-2digits-2digits
        if (date.charAt(4) != '-' || date.charAt(7) != '-') {
            return false;
        }

        String year = date.substring(0, 4);
        String month = date.substring(5, 7);
        String day = date.substring(8, 10);

        if (!isNumeric(year) || !isNumeric(month) || !isNumeric(day)) {
            return false;
        }

        // Delegate to the date validation utility for deeper validation
        DateValidationUtil.ValidationResult result =
                dateValidationUtil.validateDate(date, "YYYY-MM-DD");
        return result.isValid();
    }

    private void validateRequired(List<String> errors, String value, String fieldName) {
        if (value == null || value.isBlank()) {
            errors.add(fieldName + " can NOT be empty...");
        }
    }

    private boolean isNumeric(String str) {
        if (str == null || str.isEmpty()) {
            return false;
        }
        for (char c : str.toCharArray()) {
            if (!Character.isDigit(c)) {
                return false;
            }
        }
        return true;
    }
}
