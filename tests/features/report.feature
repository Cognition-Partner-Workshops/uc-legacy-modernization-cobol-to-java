Feature: CardDemo Transaction Reports
  As a regular CardDemo user
  I want to generate transaction reports
  So that I can review my transaction history

  Background:
    Given I am logged in as a regular user
    And I navigate to the Transaction Reports screen

  @smoke
  Scenario: Transaction Reports screen is displayed
    Then the Transaction Reports screen should be visible

  Scenario: Generate monthly report
    When I select the monthly report option
    And I confirm the report generation with "Y"
    Then the report should be submitted for processing

  Scenario: Generate yearly report
    When I select the yearly report option
    And I confirm the report generation with "Y"
    Then the report should be submitted for processing

  Scenario: Generate custom date range report
    When I select the custom report option
    And I enter start date "01" "/" "01" "/" "2024"
    And I enter end date "12" "/" "31" "/" "2024"
    And I confirm the report generation with "Y"
    Then the report should be submitted for processing

  Scenario: Cancel report generation
    When I select the monthly report option
    And I confirm the report generation with "N"
    Then the report should not be generated
