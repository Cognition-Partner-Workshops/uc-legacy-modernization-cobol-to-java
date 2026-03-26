Feature: CardDemo Account Update
  As a regular CardDemo user
  I want to update account details
  So that I can modify credit card account information

  Background:
    Given I am logged in as a regular user
    And I navigate to the Account Update screen

  Scenario: Update account credit limit
    When I load account "00000000001" for update
    And I update the credit limit to "5000"
    And I submit the account update
    Then I should see a confirmation message or updated account

  Scenario: Update account holder name
    When I load account "00000000001" for update
    And I update the first name to "JOHN"
    And I update the last name to "DOE"
    And I submit the account update
    Then I should see a confirmation message or updated account

  Scenario: Update account with invalid data shows error
    When I load account "00000000001" for update
    And I update the credit limit to "INVALID"
    And I submit the account update
    Then I should see an error message on the Account Update screen
