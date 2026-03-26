Feature: CardDemo Card List
  As a regular CardDemo user
  I want to list credit cards
  So that I can browse and select cards for viewing or updating

  Background:
    Given I am logged in as a regular user
    And I navigate to the Card List screen

  @smoke
  Scenario: Card List screen is displayed
    Then the Card List screen should be visible

  Scenario: Filter cards by account number
    When I filter cards by account number "00000000001"
    Then I should see cards listed for that account

  Scenario: Select a card from the list
    When I filter cards by account number "00000000001"
    And I select card row 1
    Then I should be taken to the card detail view

  Scenario: Filter cards with invalid account shows error
    When I filter cards by account number "99999999999"
    Then I should see an error or empty results on the Card List screen
