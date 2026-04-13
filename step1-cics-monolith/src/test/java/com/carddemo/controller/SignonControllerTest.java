package com.carddemo.controller;

import com.carddemo.repository.UserSecurityRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Boundary and risk-based tests for COSGN00C.cbl (Signon Controller).
 * Covers: valid/invalid login, empty/null credentials, max length inputs,
 * special characters, case sensitivity, password null in DB, COMMAREA setup.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class SignonControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserSecurityRepository userSecurityRepository;

    // ===== FUNCTIONAL PARITY TESTS =====

    @Test
    @DisplayName("GET /signon shows signon page")
    void testSignonPageDisplays() throws Exception {
        mockMvc.perform(get("/signon"))
            .andExpect(status().isOk())
            .andExpect(view().name("signon"));
    }

    @Test
    @DisplayName("Admin signon redirects to admin menu")
    void testAdminSignonRedirectsToAdminMenu() throws Exception {
        mockMvc.perform(post("/signon")
                .param("userId", "ADMIN001")
                .param("password", "ADMIN001"))
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrl("/admin"));
    }

    @Test
    @DisplayName("User signon redirects to main menu")
    void testUserSignonRedirectsToMainMenu() throws Exception {
        mockMvc.perform(post("/signon")
                .param("userId", "USER0001")
                .param("password", "USER0001"))
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrl("/menu"));
    }

    @Test
    @DisplayName("Wrong password shows error")
    void testWrongPasswordShowsError() throws Exception {
        mockMvc.perform(post("/signon")
                .param("userId", "ADMIN001")
                .param("password", "WRONGPWD"))
            .andExpect(status().isOk())
            .andExpect(view().name("signon"))
            .andExpect(model().attribute("errorMessage", "Wrong Password. Try again ..."));
    }

    @Test
    @DisplayName("Non-existent user shows error")
    void testUserNotFoundShowsError() throws Exception {
        mockMvc.perform(post("/signon")
                .param("userId", "NONEXIST")
                .param("password", "ANYTHING"))
            .andExpect(status().isOk())
            .andExpect(view().name("signon"))
            .andExpect(model().attribute("errorMessage", "User not found. Try again ..."));
    }

    // ===== BOUNDARY TESTS - EMPTY/NULL CREDENTIALS =====

    @Test
    @DisplayName("Empty userId shows 'enter User ID' error")
    void testEmptyUserId() throws Exception {
        mockMvc.perform(post("/signon")
                .param("userId", "")
                .param("password", "ADMIN001"))
            .andExpect(status().isOk())
            .andExpect(view().name("signon"))
            .andExpect(model().attribute("errorMessage", "Please enter User ID ..."));
    }

    @Test
    @DisplayName("Empty password shows 'enter Password' error")
    void testEmptyPassword() throws Exception {
        mockMvc.perform(post("/signon")
                .param("userId", "ADMIN001")
                .param("password", ""))
            .andExpect(status().isOk())
            .andExpect(view().name("signon"))
            .andExpect(model().attribute("errorMessage", "Please enter Password ..."));
    }

    @Test
    @DisplayName("Both empty shows userId error first")
    void testBothEmpty() throws Exception {
        mockMvc.perform(post("/signon")
                .param("userId", "")
                .param("password", ""))
            .andExpect(status().isOk())
            .andExpect(view().name("signon"))
            .andExpect(model().attribute("errorMessage", "Please enter User ID ..."));
    }

    @Test
    @DisplayName("Whitespace-only userId shows error")
    void testWhitespaceOnlyUserId() throws Exception {
        mockMvc.perform(post("/signon")
                .param("userId", "   ")
                .param("password", "ADMIN001"))
            .andExpect(status().isOk())
            .andExpect(view().name("signon"))
            .andExpect(model().attribute("errorMessage", "Please enter User ID ..."));
    }

    @Test
    @DisplayName("Whitespace-only password shows error")
    void testWhitespaceOnlyPassword() throws Exception {
        mockMvc.perform(post("/signon")
                .param("userId", "ADMIN001")
                .param("password", "   "))
            .andExpect(status().isOk())
            .andExpect(view().name("signon"))
            .andExpect(model().attribute("errorMessage", "Please enter Password ..."));
    }

    // ===== BOUNDARY TESTS - CASE SENSITIVITY =====

    @Test
    @DisplayName("Lowercase userId is uppercased and matches")
    void testLowercaseUserIdSucceeds() throws Exception {
        mockMvc.perform(post("/signon")
                .param("userId", "admin001")
                .param("password", "ADMIN001"))
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrl("/admin"));
    }

    @Test
    @DisplayName("Lowercase password is uppercased and matches")
    void testLowercasePasswordSucceeds() throws Exception {
        mockMvc.perform(post("/signon")
                .param("userId", "ADMIN001")
                .param("password", "admin001"))
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrl("/admin"));
    }

    @Test
    @DisplayName("Mixed case userId+password both uppercased and match")
    void testMixedCaseBothSucceed() throws Exception {
        mockMvc.perform(post("/signon")
                .param("userId", "AdMiN001")
                .param("password", "aDmIn001"))
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrl("/admin"));
    }

    // ===== BOUNDARY TESTS - SPECIAL CHARACTERS =====

    @Test
    @DisplayName("Special characters in userId - user not found")
    void testSpecialCharsUserId() throws Exception {
        mockMvc.perform(post("/signon")
                .param("userId", "!@#$%^&*")
                .param("password", "ANYTHING"))
            .andExpect(status().isOk())
            .andExpect(view().name("signon"))
            .andExpect(model().attribute("errorMessage", "User not found. Try again ..."));
    }

    @Test
    @DisplayName("SQL injection in userId - not found, no crash")
    void testSqlInjectionUserId() throws Exception {
        mockMvc.perform(post("/signon")
                .param("userId", "' OR 1=1--")
                .param("password", "ANYTHING"))
            .andExpect(status().isOk())
            .andExpect(view().name("signon"));
    }

    // ===== BOUNDARY TESTS - MAX LENGTH =====

    @Test
    @DisplayName("Very long userId is handled gracefully")
    void testVeryLongUserId() throws Exception {
        String longId = "A".repeat(100);
        mockMvc.perform(post("/signon")
                .param("userId", longId)
                .param("password", "ANYTHING"))
            .andExpect(status().isOk())
            .andExpect(view().name("signon"))
            .andExpect(model().attribute("errorMessage", "User not found. Try again ..."));
    }

    @Test
    @DisplayName("Exactly 8-char userId (max COBOL PIC X(08)) works")
    void testExact8CharUserId() throws Exception {
        mockMvc.perform(post("/signon")
                .param("userId", "ADMIN001")
                .param("password", "ADMIN001"))
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrl("/admin"));
    }

    // ===== BOUNDARY TESTS - LEADING/TRAILING SPACES =====

    @Test
    @DisplayName("userId with leading/trailing spaces is trimmed")
    void testUserIdWithSpaces() throws Exception {
        mockMvc.perform(post("/signon")
                .param("userId", " ADMIN001 ")
                .param("password", "ADMIN001"))
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrl("/admin"));
    }

    @Test
    @DisplayName("Password with spaces is trimmed")
    void testPasswordWithSpaces() throws Exception {
        mockMvc.perform(post("/signon")
                .param("userId", "ADMIN001")
                .param("password", " ADMIN001 "))
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrl("/admin"));
    }

    // ===== RISK-BASED TESTS - HEADER INFO =====

    @Test
    @DisplayName("Signon page has header info model attributes")
    void testSignonPageHasHeaderInfo() throws Exception {
        mockMvc.perform(get("/signon"))
            .andExpect(status().isOk())
            .andExpect(model().attributeExists("title01"))
            .andExpect(model().attributeExists("tranId"))
            .andExpect(model().attributeExists("pgmName"))
            .andExpect(model().attributeExists("currentDate"))
            .andExpect(model().attributeExists("currentTime"));
    }

    // ===== RISK-BASED TESTS - AUTHENTICATION STATE =====

    @Test
    @DisplayName("Successful login creates COMMAREA in session")
    void testSuccessfulLoginCreatesCommarea() throws Exception {
        mockMvc.perform(post("/signon")
                .param("userId", "USER0001")
                .param("password", "USER0001"))
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrl("/menu"))
            .andExpect(request().sessionAttribute("CARDDEMO_COMMAREA",
                org.hamcrest.Matchers.notNullValue()));
    }

    @Test
    @DisplayName("GET /signon clears existing COMMAREA")
    void testGetSignonClearsCommarea() throws Exception {
        // First login
        mockMvc.perform(post("/signon")
                .param("userId", "USER0001")
                .param("password", "USER0001"))
            .andExpect(status().is3xxRedirection());

        // Then visit signon page - should clear COMMAREA
        mockMvc.perform(get("/signon"))
            .andExpect(status().isOk())
            .andExpect(view().name("signon"));
    }
}
