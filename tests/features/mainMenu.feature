Feature: CardDemo Main Menu Navigation
  As a regular CardDemo user
  I want to navigate the main menu
  So that I can access various credit card management functions

  Background:
    Given I am logged in as a regular user
    And I am on the Main Menu

  @smoke
  Scenario: Main Menu is displayed after regular user login
    Then the Main Menu should be visible
    And menu options should be displayed

  Scenario: Navigate to Account View from Main Menu
    When I select menu option "01"
    Then I should see the Account View screen

  Scenario: Navigate to Account Update from Main Menu
    When I select menu option "02"
    Then I should see the Account Update screen

  Scenario: Navigate to Card List from Main Menu
    When I select menu option "03"
    Then I should see the Card List screen

  Scenario: Navigate to Card View from Main Menu
    When I select menu option "04"
    Then I should see the Card Detail screen

  Scenario: Navigate to Card Update from Main Menu
    When I select menu option "05"
    Then I should see the Card Update screen

  Scenario: Navigate to Bill Payment from Main Menu
    When I select menu option "06"
    Then I should see the Bill Payment screen

  Scenario: Navigate to Transaction List from Main Menu
    When I select menu option "07"
    Then I should see the Transaction List screen

  Scenario: Navigate to Transaction View from Main Menu
    When I select menu option "08"
    Then I should see the Transaction View screen

  Scenario: Navigate to Transaction Add from Main Menu
    When I select menu option "09"
    Then I should see the Transaction Add screen

  Scenario: Navigate to Transaction Reports from Main Menu
    When I select menu option "10"
    Then I should see the Transaction Reports screen

  Scenario: Invalid menu option shows error
    When I select menu option "99"
    Then I should see an error message on the menu
