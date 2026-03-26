Feature: CardDemo Bill Payment
  As a regular CardDemo user
  I want to pay my credit card balance
  So that I can settle my outstanding balance

  Background:
    Given I am logged in as a regular user
    And I navigate to the Bill Payment screen

  @smoke
  Scenario: Bill Payment screen is displayed
    Then the Bill Payment screen should be visible

  Scenario: View current balance for account
    When I enter account ID "00000000001" on the Bill Payment screen
    Then I should see the current balance displayed

  Scenario: Confirm bill payment
    When I enter account ID "00000000001" on the Bill Payment screen
    And I confirm the bill payment with "Y"
    Then the payment should be processed successfully

  Scenario: Cancel bill payment
    When I enter account ID "00000000001" on the Bill Payment screen
    And I confirm the bill payment with "N"
    Then the payment should not be processed

  Scenario: Bill payment with invalid account
    When I enter account ID "99999999999" on the Bill Payment screen
    Then I should see an error message on the Bill Payment screen
