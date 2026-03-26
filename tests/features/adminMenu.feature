Feature: CardDemo Admin Menu Navigation
  As an admin CardDemo user
  I want to navigate the admin menu
  So that I can manage users and perform admin functions

  Background:
    Given I am logged in as an admin user
    And I am on the Admin Menu

  @smoke
  Scenario: Admin Menu is displayed after admin user login
    Then the Admin Menu should be visible

  Scenario: Navigate to User List from Admin Menu
    When I select admin menu option "01"
    Then I should see the User List screen

  Scenario: Navigate to User Add from Admin Menu
    When I select admin menu option "02"
    Then I should see the User Add screen

  Scenario: Navigate to User Update from Admin Menu
    When I select admin menu option "03"
    Then I should see the User Update screen

  Scenario: Navigate to User Delete from Admin Menu
    When I select admin menu option "04"
    Then I should see the User Delete screen

  Scenario: Invalid admin menu option shows error
    When I select admin menu option "99"
    Then I should see an error message on the admin menu
