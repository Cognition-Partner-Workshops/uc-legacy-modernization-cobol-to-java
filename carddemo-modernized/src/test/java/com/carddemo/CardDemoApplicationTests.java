package com.carddemo;

import org.junit.jupiter.api.Test;

/**
 * Smoke test to verify the application context loads.
 * Full integration tests require Cassandra and JDBC datasource.
 */
class CardDemoApplicationTests {

    @Test
    void contextLoadsWithoutSpring() {
        // Verify core classes can be instantiated without Spring context
        // (Spring context load requires Cassandra connectivity)
        var account = new com.carddemo.model.Account();
        account.setAccountId("00000000001");
        assert account.getAccountId().equals("00000000001");

        var card = new com.carddemo.model.Card();
        card.setCardNumber("4111111111111111");
        assert card.getCardNumber().equals("4111111111111111");

        var customer = new com.carddemo.model.Customer();
        customer.setCustomerId("000000001");
        assert customer.getCustomerId().equals("000000001");

        var transaction = new com.carddemo.model.Transaction();
        transaction.setTransactionId("0000000000000001");
        assert transaction.getTransactionId().equals("0000000000000001");

        var user = new com.carddemo.model.UserSecurity("ADMIN001", "Admin", "User", "PASSWORD", "A");
        assert user.isAdmin();
        assert user.getUserId().equals("ADMIN001");

        var loginReq = new com.carddemo.dto.LoginRequest("USER0001", "PASSWORD");
        assert loginReq.getUserId().equals("USER0001");

        var transType = new com.carddemo.model.TransactionType("SA", "Sale");
        assert transType.getTypeCode().equals("SA");

        var transCat = new com.carddemo.model.TransactionCategory("SA", 5001, "Retail Purchase");
        assert transCat.getCategoryCode().equals(5001);
    }
}
