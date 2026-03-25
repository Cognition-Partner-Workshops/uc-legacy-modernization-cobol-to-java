package com.carddemo.qa.tests;

import com.carddemo.qa.model.CardRecord;
import com.carddemo.qa.util.TestDataFactory;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test cases for Credit Card List (COCRDLIC / CCLI), View (COCRDSLC / CCDL),
 * and Update (COCRDUPC / CCUP) transactions.
 * Validates card record integrity and business rules migrated from COBOL.
 *
 * @see <a href="../../app/cbl/COCRDLIC.cbl">COCRDLIC.cbl</a>
 * @see <a href="../../app/cbl/COCRDSLC.cbl">COCRDSLC.cbl</a>
 * @see <a href="../../app/cbl/COCRDUPC.cbl">COCRDUPC.cbl</a>
 * @see <a href="../../app/cpy/CVACT02Y.cpy">CVACT02Y.cpy</a>
 */
@DisplayName("Credit Card Management Tests (COCRDLIC / COCRDSLC / COCRDUPC)")
class CreditCardManagementTest {

    @Nested
    @DisplayName("Card Record Fields")
    class CardRecordFields {

        @Test
        @DisplayName("TC-CARD-001: Card number is 16 characters")
        void cardNumberIs16Characters() {
            CardRecord card = TestDataFactory.createActiveCard();

            assertEquals(16, card.getCardNumber().length(),
                    "CARD-NUM is PIC X(16), must be exactly 16 characters");
        }

        @Test
        @DisplayName("TC-CARD-002: Card is linked to a valid account ID")
        void cardLinkedToValidAccountId() {
            CardRecord card = TestDataFactory.createActiveCard();

            assertTrue(card.getAccountId() > 0,
                    "Card must be associated with a valid account ID");
        }

        @Test
        @DisplayName("TC-CARD-003: CVV code is 3 digits")
        void cvvCodeIs3Digits() {
            CardRecord card = TestDataFactory.createActiveCard();

            assertTrue(card.getCvvCode() >= 0 && card.getCvvCode() <= 999,
                    "CARD-CVV-CD is PIC 9(03), must be between 000 and 999");
        }

        @Test
        @DisplayName("TC-CARD-004: Embossed name limited to 50 characters")
        void embossedNameFieldLength() {
            CardRecord card = TestDataFactory.createActiveCard();

            assertTrue(card.getEmbossedName().length() <= 50,
                    "CARD-EMBOSSED-NAME is PIC X(50), max 50 characters");
        }

        @Test
        @DisplayName("TC-CARD-005: Expiration date is present")
        void expirationDatePresent() {
            CardRecord card = TestDataFactory.createActiveCard();

            assertNotNull(card.getExpirationDate(), "Expiration date should not be null");
            assertFalse(card.getExpirationDate().isBlank(),
                    "Expiration date should not be blank");
        }
    }

    @Nested
    @DisplayName("Card Status")
    class CardStatus {

        @Test
        @DisplayName("TC-CARD-006: Active card has status 'Y'")
        void activeCardStatus() {
            CardRecord card = TestDataFactory.createActiveCard();

            assertTrue(card.isActive(), "Active card should return true for isActive()");
            assertEquals("Y", card.getActiveStatus());
        }

        @Test
        @DisplayName("TC-CARD-007: Inactive card has status 'N'")
        void inactiveCardStatus() {
            CardRecord card = TestDataFactory.createInactiveCard();

            assertFalse(card.isActive(), "Inactive card should return false for isActive()");
            assertEquals("N", card.getActiveStatus());
        }

        @Test
        @DisplayName("TC-CARD-008: Card status must be 'Y' or 'N'")
        void cardStatusValidValues() {
            CardRecord active = TestDataFactory.createActiveCard();
            CardRecord inactive = TestDataFactory.createInactiveCard();

            assertTrue("Y".equals(active.getActiveStatus()) || "N".equals(active.getActiveStatus()));
            assertTrue("Y".equals(inactive.getActiveStatus()) || "N".equals(inactive.getActiveStatus()));
        }
    }

    @Nested
    @DisplayName("Card-Account Relationship")
    class CardAccountRelationship {

        @Test
        @DisplayName("TC-CARD-009: Active and inactive cards can share the same account")
        void cardsShareSameAccount() {
            CardRecord active = TestDataFactory.createActiveCard();
            CardRecord inactive = TestDataFactory.createInactiveCard();

            assertEquals(active.getAccountId(), inactive.getAccountId(),
                    "Multiple cards can be linked to the same account");
        }

        @Test
        @DisplayName("TC-CARD-010: Different cards have different card numbers")
        void differentCardsHaveDifferentNumbers() {
            CardRecord card1 = TestDataFactory.createActiveCard();
            CardRecord card2 = TestDataFactory.createInactiveCard();

            assertNotEquals(card1.getCardNumber(), card2.getCardNumber(),
                    "Each card must have a unique card number");
        }
    }
}
