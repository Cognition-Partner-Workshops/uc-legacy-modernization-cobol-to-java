Feature: CardDemo Transaction Add
  As a regular CardDemo user
  I want to add new transactions
  So that I can record credit card transactions

  Background:
    Given I am logged in as a regular user
    And I navigate to the Transaction Add screen

  Scenario: Add a new transaction with valid data
    When I enter account ID "00000000001" on the Transaction Add screen
    And I enter transaction details:
      | typeCode | categoryCode | source | description       | amount  | originDate | processDate | merchantId | merchantName  | merchantCity | merchantZip |
      | SA       | 5411         | ONLINE | Test Transaction  | 100.00  | 2024-01-15 | 2024-01-15  | 123456789  | Test Merchant | New York     | 10001       |
    And I submit the transaction
    And I confirm the transaction with "Y"
    Then the transaction should be added successfully

  Scenario: Add transaction with missing required fields
    When I enter account ID "00000000001" on the Transaction Add screen
    And I submit the transaction
    Then I should see an error message on the Transaction Add screen

  Scenario: Cancel transaction addition
    When I enter account ID "00000000001" on the Transaction Add screen
    And I enter transaction details:
      | typeCode | categoryCode | source | description       | amount  | originDate | processDate | merchantId | merchantName  | merchantCity | merchantZip |
      | SA       | 5411         | ONLINE | Test Transaction  | 100.00  | 2024-01-15 | 2024-01-15  | 123456789  | Test Merchant | New York     | 10001       |
    And I submit the transaction
    And I confirm the transaction with "N"
    Then the transaction should not be added
