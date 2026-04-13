package com.carddemo.controller;

import com.carddemo.model.commarea.CardDemoCommarea;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Boundary and risk-based tests for COTRN02C.cbl (Transaction Add Controller).
 * Covers: empty fields, invalid amounts, invalid dates, invalid merchant IDs,
 * account/card xref lookup, amount format boundaries, date validation, TRAN-ID generation.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@WithMockUser(username = "USER0001")
class TransactionAddControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private MockHttpSession createAuthenticatedSession() {
        MockHttpSession session = new MockHttpSession();
        CardDemoCommarea commarea = new CardDemoCommarea();
        commarea.setUserId("USER0001");
        commarea.setUserType("U");
        session.setAttribute("CARDDEMO_COMMAREA", commarea);
        return session;
    }

    // ===== FUNCTIONAL PARITY TESTS =====

    @Test
    @DisplayName("Transaction add page displays")
    void testTransactionAddPageDisplays() throws Exception {
        MockHttpSession session = createAuthenticatedSession();

        mockMvc.perform(get("/transaction/add").session(session))
            .andExpect(status().isOk())
            .andExpect(view().name("tran-add"));
    }

    @Test
    @DisplayName("Empty account and card shows error")
    void testEmptyAccountAndCard() throws Exception {
        MockHttpSession session = createAuthenticatedSession();

        mockMvc.perform(post("/transaction/add")
                .param("acctId", "")
                .param("cardNum", "")
                .session(session))
            .andExpect(status().isOk())
            .andExpect(view().name("tran-add"))
            .andExpect(model().attribute("errorMessage", "Account or Card Number must be entered..."));
    }

    @Test
    @DisplayName("PF3 returns to menu")
    void testPF3ReturnsToMenu() throws Exception {
        MockHttpSession session = createAuthenticatedSession();

        mockMvc.perform(post("/transaction/add")
                .param("action", "PF3")
                .session(session))
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrl("/menu"));
    }

    @Test
    @DisplayName("PF4 clears form by redirecting")
    void testPF4ClearsForm() throws Exception {
        MockHttpSession session = createAuthenticatedSession();

        mockMvc.perform(post("/transaction/add")
                .param("action", "PF4")
                .session(session))
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrl("/transaction/add"));
    }

    // ===== BOUNDARY TESTS - ACCOUNT/CARD XREF =====

    @Test
    @DisplayName("Non-numeric account ID shows error")
    void testNonNumericAccountId() throws Exception {
        MockHttpSession session = createAuthenticatedSession();

        mockMvc.perform(post("/transaction/add")
                .param("acctId", "ABC")
                .param("cardNum", "")
                .session(session))
            .andExpect(status().isOk())
            .andExpect(model().attribute("errorMessage", "Account ID must be Numeric..."));
    }

    @Test
    @DisplayName("Account ID not in xref shows error")
    void testAccountNotInXref() throws Exception {
        MockHttpSession session = createAuthenticatedSession();

        mockMvc.perform(post("/transaction/add")
                .param("acctId", "99999")
                .param("cardNum", "")
                .session(session))
            .andExpect(status().isOk())
            .andExpect(model().attribute("errorMessage", "Account not found in cross reference..."));
    }

    @Test
    @DisplayName("Non-numeric card number shows error")
    void testNonNumericCardNum() throws Exception {
        MockHttpSession session = createAuthenticatedSession();

        mockMvc.perform(post("/transaction/add")
                .param("acctId", "")
                .param("cardNum", "ABCD")
                .session(session))
            .andExpect(status().isOk())
            .andExpect(model().attribute("errorMessage", "Card Number must be Numeric..."));
    }

    @Test
    @DisplayName("Card number not in xref shows error")
    void testCardNotInXref() throws Exception {
        MockHttpSession session = createAuthenticatedSession();

        mockMvc.perform(post("/transaction/add")
                .param("acctId", "")
                .param("cardNum", "9999999999999999")
                .session(session))
            .andExpect(status().isOk())
            .andExpect(model().attribute("errorMessage", "Card not found in cross reference..."));
    }

    // ===== BOUNDARY TESTS - FIELD VALIDATION =====

    @Test
    @DisplayName("Valid account but empty type CD shows error")
    void testEmptyTypeCd() throws Exception {
        MockHttpSession session = createAuthenticatedSession();

        mockMvc.perform(post("/transaction/add")
                .param("acctId", "1")
                .param("cardNum", "")
                .param("typeCd", "")
                .param("catCd", "1")
                .param("source", "TEST")
                .param("description", "Test transaction")
                .param("amount", "100.00")
                .param("origDate", "2024-01-15")
                .param("procDate", "2024-01-15")
                .param("merchantId", "12345")
                .param("merchantName", "Test Merchant")
                .param("merchantCity", "Test City")
                .param("merchantZip", "12345")
                .session(session))
            .andExpect(status().isOk())
            .andExpect(model().attribute("errorMessage", "Type CD can NOT be empty..."));
    }

    @Test
    @DisplayName("Non-numeric type CD shows error")
    void testNonNumericTypeCd() throws Exception {
        MockHttpSession session = createAuthenticatedSession();

        mockMvc.perform(post("/transaction/add")
                .param("acctId", "1")
                .param("cardNum", "")
                .param("typeCd", "AB")
                .param("catCd", "1")
                .param("source", "TEST")
                .param("description", "Test")
                .param("amount", "100.00")
                .param("origDate", "2024-01-15")
                .param("procDate", "2024-01-15")
                .param("merchantId", "12345")
                .param("merchantName", "Test")
                .param("merchantCity", "City")
                .param("merchantZip", "12345")
                .session(session))
            .andExpect(status().isOk())
            .andExpect(model().attribute("errorMessage", "Type CD must be Numeric..."));
    }

    // ===== BOUNDARY TESTS - AMOUNT FORMAT =====

    @Test
    @DisplayName("Valid amount format 100.00 accepted")
    void testValidAmountFormat() throws Exception {
        MockHttpSession session = createAuthenticatedSession();

        mockMvc.perform(post("/transaction/add")
                .param("acctId", "1")
                .param("typeCd", "01")
                .param("catCd", "1")
                .param("source", "ONLINE")
                .param("description", "Test Purchase")
                .param("amount", "100.00")
                .param("origDate", "2024-01-15")
                .param("procDate", "2024-01-15")
                .param("merchantId", "12345")
                .param("merchantName", "Test Merchant")
                .param("merchantCity", "Test City")
                .param("merchantZip", "12345")
                .param("confirm", "")
                .session(session))
            .andExpect(status().isOk())
            .andExpect(model().attribute("errorMessage", "Confirm to add this transaction..."));
    }

    @Test
    @DisplayName("Negative amount format -100.00 accepted")
    void testNegativeAmountFormat() throws Exception {
        MockHttpSession session = createAuthenticatedSession();

        mockMvc.perform(post("/transaction/add")
                .param("acctId", "1")
                .param("typeCd", "01")
                .param("catCd", "1")
                .param("source", "ONLINE")
                .param("description", "Refund")
                .param("amount", "-100.00")
                .param("origDate", "2024-01-15")
                .param("procDate", "2024-01-15")
                .param("merchantId", "12345")
                .param("merchantName", "Test Merchant")
                .param("merchantCity", "Test City")
                .param("merchantZip", "12345")
                .param("confirm", "")
                .session(session))
            .andExpect(status().isOk())
            .andExpect(model().attribute("errorMessage", "Confirm to add this transaction..."));
    }

    @Test
    @DisplayName("Amount without decimal rejected")
    void testAmountWithoutDecimal() throws Exception {
        MockHttpSession session = createAuthenticatedSession();

        mockMvc.perform(post("/transaction/add")
                .param("acctId", "1")
                .param("typeCd", "01")
                .param("catCd", "1")
                .param("source", "TEST")
                .param("description", "Test")
                .param("amount", "100")
                .param("origDate", "2024-01-15")
                .param("procDate", "2024-01-15")
                .param("merchantId", "12345")
                .param("merchantName", "Test")
                .param("merchantCity", "City")
                .param("merchantZip", "12345")
                .session(session))
            .andExpect(status().isOk())
            .andExpect(model().attribute("errorMessage", "Amount should be in format -99999999.99"));
    }

    @Test
    @DisplayName("Maximum amount 99999999.99 accepted")
    void testMaxAmount() throws Exception {
        MockHttpSession session = createAuthenticatedSession();

        mockMvc.perform(post("/transaction/add")
                .param("acctId", "1")
                .param("typeCd", "01")
                .param("catCd", "1")
                .param("source", "ONLINE")
                .param("description", "Max Purchase")
                .param("amount", "99999999.99")
                .param("origDate", "2024-01-15")
                .param("procDate", "2024-01-15")
                .param("merchantId", "12345")
                .param("merchantName", "Test")
                .param("merchantCity", "City")
                .param("merchantZip", "12345")
                .param("confirm", "")
                .session(session))
            .andExpect(status().isOk())
            .andExpect(model().attribute("errorMessage", "Confirm to add this transaction..."));
    }

    @Test
    @DisplayName("Amount exceeding 8 digits before decimal rejected")
    void testAmountExceedingMaxDigits() throws Exception {
        MockHttpSession session = createAuthenticatedSession();

        mockMvc.perform(post("/transaction/add")
                .param("acctId", "1")
                .param("typeCd", "01")
                .param("catCd", "1")
                .param("source", "TEST")
                .param("description", "Test")
                .param("amount", "100000000.00")
                .param("origDate", "2024-01-15")
                .param("procDate", "2024-01-15")
                .param("merchantId", "12345")
                .param("merchantName", "Test")
                .param("merchantCity", "City")
                .param("merchantZip", "12345")
                .session(session))
            .andExpect(status().isOk())
            .andExpect(model().attribute("errorMessage", "Amount should be in format -99999999.99"));
    }

    // ===== BOUNDARY TESTS - DATE VALIDATION =====

    @Test
    @DisplayName("Invalid orig date format rejected")
    void testInvalidOrigDateFormat() throws Exception {
        MockHttpSession session = createAuthenticatedSession();

        mockMvc.perform(post("/transaction/add")
                .param("acctId", "1")
                .param("typeCd", "01")
                .param("catCd", "1")
                .param("source", "TEST")
                .param("description", "Test")
                .param("amount", "100.00")
                .param("origDate", "01-15-2024")
                .param("procDate", "2024-01-15")
                .param("merchantId", "12345")
                .param("merchantName", "Test")
                .param("merchantCity", "City")
                .param("merchantZip", "12345")
                .session(session))
            .andExpect(status().isOk())
            .andExpect(model().attribute("errorMessage", "Orig Date should be in format YYYY-MM-DD"));
    }

    @Test
    @DisplayName("Invalid orig date value (Feb 30) rejected")
    void testInvalidOrigDateValue() throws Exception {
        MockHttpSession session = createAuthenticatedSession();

        mockMvc.perform(post("/transaction/add")
                .param("acctId", "1")
                .param("typeCd", "01")
                .param("catCd", "1")
                .param("source", "TEST")
                .param("description", "Test")
                .param("amount", "100.00")
                .param("origDate", "2024-02-30")
                .param("procDate", "2024-01-15")
                .param("merchantId", "12345")
                .param("merchantName", "Test")
                .param("merchantCity", "City")
                .param("merchantZip", "12345")
                .session(session))
            .andExpect(status().isOk())
            .andExpect(model().attribute("errorMessage", containsString("Orig Date is invalid")));
    }

    @Test
    @DisplayName("Non-numeric merchant ID rejected")
    void testNonNumericMerchantId() throws Exception {
        MockHttpSession session = createAuthenticatedSession();

        mockMvc.perform(post("/transaction/add")
                .param("acctId", "1")
                .param("typeCd", "01")
                .param("catCd", "1")
                .param("source", "TEST")
                .param("description", "Test")
                .param("amount", "100.00")
                .param("origDate", "2024-01-15")
                .param("procDate", "2024-01-15")
                .param("merchantId", "ABC")
                .param("merchantName", "Test")
                .param("merchantCity", "City")
                .param("merchantZip", "12345")
                .session(session))
            .andExpect(status().isOk())
            .andExpect(model().attribute("errorMessage", "Merchant ID must be Numeric..."));
    }

    // ===== RISK-BASED TESTS - TRANSACTION CONFIRMATION =====

    @Test
    @DisplayName("Confirm=Y adds transaction successfully")
    void testConfirmYAddsTransaction() throws Exception {
        MockHttpSession session = createAuthenticatedSession();

        mockMvc.perform(post("/transaction/add")
                .param("acctId", "1")
                .param("typeCd", "01")
                .param("catCd", "1")
                .param("source", "ONLINE")
                .param("description", "Confirmed Purchase")
                .param("amount", "50.00")
                .param("origDate", "2024-01-15")
                .param("procDate", "2024-01-15")
                .param("merchantId", "12345")
                .param("merchantName", "Test Merchant")
                .param("merchantCity", "Test City")
                .param("merchantZip", "12345")
                .param("confirm", "Y")
                .session(session))
            .andExpect(status().isOk())
            .andExpect(model().attribute("errorMessage", containsString("Transaction added successfully")));
    }

    @Test
    @DisplayName("Confirm=N cancels transaction")
    void testConfirmNCancels() throws Exception {
        MockHttpSession session = createAuthenticatedSession();

        mockMvc.perform(post("/transaction/add")
                .param("acctId", "1")
                .param("typeCd", "01")
                .param("catCd", "1")
                .param("source", "ONLINE")
                .param("description", "Cancelled Purchase")
                .param("amount", "50.00")
                .param("origDate", "2024-01-15")
                .param("procDate", "2024-01-15")
                .param("merchantId", "12345")
                .param("merchantName", "Test Merchant")
                .param("merchantCity", "Test City")
                .param("merchantZip", "12345")
                .param("confirm", "N")
                .session(session))
            .andExpect(status().isOk())
            .andExpect(model().attribute("errorMessage", "Transaction cancelled."));
    }

    // ===== RISK-BASED TESTS - MISSING SESSION =====

    @Test
    @DisplayName("GET without COMMAREA redirects to signon")
    void testGetWithoutCommareaRedirects() throws Exception {
        MockHttpSession session = new MockHttpSession();

        mockMvc.perform(get("/transaction/add").session(session))
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrl("/signon"));
    }

    @Test
    @DisplayName("POST without COMMAREA redirects to signon")
    void testPostWithoutCommareaRedirects() throws Exception {
        MockHttpSession session = new MockHttpSession();

        mockMvc.perform(post("/transaction/add")
                .param("acctId", "1")
                .session(session))
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrl("/signon"));
    }
}
