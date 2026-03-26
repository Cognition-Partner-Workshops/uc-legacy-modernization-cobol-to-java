Feature: CardDemo Card Detail View
  As a regular CardDemo user
  I want to view credit card details
  So that I can see card information like name, status, and expiry

  Background:
    Given I am logged in as a regular user
    And I navigate to the Card Detail screen

  @smoke
  Scenario: View card detail with valid account and card number
    When I enter account number "00000000001" on the Card Detail screen
    And I enter card number "4111111111111111" on the Card Detail screen
    Then I should see the card details displayed

  Scenario: Card detail shows card name and status
    When I enter account number "00000000001" on the Card Detail screen
    And I enter card number "4111111111111111" on the Card Detail screen
    Then I should see the card name
    And I should see the card status

  Scenario: View card with invalid card number
    When I enter account number "00000000001" on the Card Detail screen
    And I enter card number "0000000000000000" on the Card Detail screen
    Then I should see an error message on the Card Detail screen
