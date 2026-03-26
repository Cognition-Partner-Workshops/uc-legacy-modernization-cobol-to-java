Feature: CardDemo Transaction View
  As a regular CardDemo user
  I want to view transaction details
  So that I can see full transaction information including merchant details

  Background:
    Given I am logged in as a regular user
    And I navigate to the Transaction View screen

  @smoke
  Scenario: View transaction details with valid transaction ID
    When I enter transaction ID "0000001" on the Transaction View screen
    Then I should see the transaction details displayed

  Scenario: Transaction details show merchant information
    When I enter transaction ID "0000001" on the Transaction View screen
    Then I should see the merchant name
    And I should see the merchant city
    And I should see the transaction amount

  Scenario: View transaction with invalid ID
    When I enter transaction ID "9999999" on the Transaction View screen
    Then I should see an error message on the Transaction View screen
