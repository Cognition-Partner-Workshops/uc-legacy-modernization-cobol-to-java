Feature: CardDemo Account View
  As a regular CardDemo user
  I want to view account details
  So that I can see credit card account information

  Background:
    Given I am logged in as a regular user
    And I navigate to the Account View screen

  @smoke
  Scenario: View account details with valid account number
    When I enter account number "00000000001" on the Account View screen
    Then I should see the account details displayed
    And the account number should be "00000000001"

  Scenario: View account shows customer information
    When I enter account number "00000000001" on the Account View screen
    Then I should see the customer first name
    And I should see the customer last name
    And I should see the credit limit

  Scenario: View account with invalid account number
    When I enter account number "99999999999" on the Account View screen
    Then I should see an error message on the Account View screen

  Scenario: Account View displays all financial fields
    When I enter account number "00000000001" on the Account View screen
    Then I should see the current balance
    And I should see the credit limit
