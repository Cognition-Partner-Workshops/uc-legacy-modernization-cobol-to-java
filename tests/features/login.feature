@smoke
Feature: CardDemo Login
  As a CardDemo user
  I want to sign in to the application
  So that I can access the credit card management system

  Background:
    Given I am on the CardDemo login page

  Scenario: Successful login with regular user credentials
    When I enter user ID "USER0001" and password "PASSWORD"
    And I click the sign-on button
    Then I should be redirected to the Main Menu

  Scenario: Successful login with admin user credentials
    When I enter user ID "ADMIN001" and password "PASSWORD"
    And I click the sign-on button
    Then I should be redirected to the Admin Menu

  Scenario: Failed login with invalid user ID
    When I enter user ID "INVALID1" and password "PASSWORD"
    And I click the sign-on button
    Then I should see an error message on the login page

  Scenario: Failed login with invalid password
    When I enter user ID "USER0001" and password "WRONGPWD"
    And I click the sign-on button
    Then I should see an error message on the login page

  Scenario: Failed login with empty credentials
    When I enter user ID "" and password ""
    And I click the sign-on button
    Then I should see an error message on the login page

  Scenario: Login page displays application title
    Then the application title should be visible
