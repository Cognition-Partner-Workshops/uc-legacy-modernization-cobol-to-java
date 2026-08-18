package com.carddemo.statement;

import java.util.List;

/**
 * Port of the {@code WS-TRNX-TABLE} grouping built by {@code 8100-TRNXFILE-OPEN} /
 * {@code 8500-READTRNX-READ}: the sorted transaction file is read sequentially and
 * grouped per card number, 51 cards x 10 transactions, with a per-card count.
 */
public final class TransactionTable {

    public static final int MAX_CARDS = 51;
    public static final int MAX_TRANSACTIONS_PER_CARD = 10;

    /** 1-based, as in COBOL: index 0 is unused. */
    private final String[] cardNumbers = new String[MAX_CARDS + 1];
    private final String[][] transactionIds = new String[MAX_CARDS + 1][MAX_TRANSACTIONS_PER_CARD + 1];
    private final String[][] transactionRest = new String[MAX_CARDS + 1][MAX_TRANSACTIONS_PER_CARD + 1];
    private final int[] transactionCounts = new int[MAX_CARDS + 1];
    private int cardCount;

    public static TransactionTable build(List<TransactionRecord> sortedTransactions) {
        TransactionTable table = new TransactionTable();
        table.load(sortedTransactions);
        return table;
    }

    private void load(List<TransactionRecord> sortedTransactions) {
        if (sortedTransactions.isEmpty()) {
            return;
        }
        String saveCard = sortedTransactions.get(0).cardNumber();
        cardCount = 1;
        int tranCount = 0;
        for (TransactionRecord transaction : sortedTransactions) {
            if (saveCard.equals(transaction.cardNumber())) {
                tranCount++;
            } else {
                transactionCounts[cardCount] = tranCount;
                cardCount++;
                tranCount = 1;
            }
            checkBounds(cardCount, tranCount);
            cardNumbers[cardCount] = transaction.cardNumber();
            transactionIds[cardCount][tranCount] = transaction.transactionId();
            transactionRest[cardCount][tranCount] = transaction.rest();
            saveCard = transaction.cardNumber();
        }
        transactionCounts[cardCount] = tranCount;
    }

    private static void checkBounds(int card, int transaction) {
        if (card > MAX_CARDS) {
            throw new IllegalStateException("WS-TRNX-TABLE overflow: more than " + MAX_CARDS + " cards");
        }
        if (transaction > MAX_TRANSACTIONS_PER_CARD) {
            throw new IllegalStateException("WS-TRNX-TABLE overflow: more than "
                    + MAX_TRANSACTIONS_PER_CARD + " transactions for card " + card);
        }
    }

    /** {@code CR-CNT}: number of distinct cards loaded. */
    public int cardCount() {
        return cardCount;
    }

    /** {@code WS-CARD-NUM (index)}. */
    public String cardNumber(int index) {
        return cardNumbers[index];
    }

    /** {@code WS-TRCT (index)}. */
    public int transactionCount(int index) {
        return transactionCounts[index];
    }

    /** {@code WS-TRAN-NUM (card, transaction)}. */
    public String transactionId(int card, int transaction) {
        return transactionIds[card][transaction];
    }

    /** {@code WS-TRAN-REST (card, transaction)}. */
    public String transactionRest(int card, int transaction) {
        return transactionRest[card][transaction];
    }
}
