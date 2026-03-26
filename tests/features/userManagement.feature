Feature: CardDemo User Management
  As an admin CardDemo user
  I want to manage user accounts
  So that I can add, update, delete, and list users

  # User List
  @smoke
  Scenario: View user list
    Given I am logged in as an admin user
    And I navigate to the User List screen
    Then the User List screen should be visible

  Scenario: Search for a user by ID
    Given I am logged in as an admin user
    And I navigate to the User List screen
    When I search for user ID "USER0001"
    Then I should see user results listed

  # User Add
  Scenario: Add a new user
    Given I am logged in as an admin user
    And I navigate to the User Add screen
    When I enter new user details:
      | firstName | lastName | userId   | password | userType |
      | TEST      | USER     | TESTUS01 | TESTPWD1 | U        |
    And I submit the new user
    Then the user should be added successfully

  Scenario: Add user with missing fields shows error
    Given I am logged in as an admin user
    And I navigate to the User Add screen
    When I enter new user details:
      | firstName | lastName | userId | password | userType |
      |           |          |        |          |          |
    And I submit the new user
    Then I should see an error message on the User Add screen

  # User Update
  Scenario: Update an existing user
    Given I am logged in as an admin user
    And I navigate to the User Update screen
    When I fetch user "USER0001" for update
    And I update the user first name to "UPDATED"
    And I save the user update
    Then the user should be updated successfully

  # User Delete
  Scenario: Delete user - fetch and confirm
    Given I am logged in as an admin user
    And I navigate to the User Delete screen
    When I fetch user "TESTUS01" for deletion
    Then I should see the user details for confirmation

  # Select user from list for update
  Scenario: Select user from list for update
    Given I am logged in as an admin user
    And I navigate to the User List screen
    When I select user row 1 for update
    Then I should see the User Update screen

  # Select user from list for delete
  Scenario: Select user from list for delete
    Given I am logged in as an admin user
    And I navigate to the User List screen
    When I select user row 1 for delete
    Then I should see the User Delete screen
