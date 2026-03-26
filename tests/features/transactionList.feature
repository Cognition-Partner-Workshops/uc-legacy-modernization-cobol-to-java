Feature: CardDemo Transaction List
  As a regular CardDemo user
  I want to list transactions
  So that I can browse and select transactions for viewing

  Background:
    Given I am logged in as a regular user
    And I navigate to the Transaction List screen

  @smoke
  Scenario: Transaction List screen is displayed
    Then the Transaction List screen should be visible

  Scenario: Search transactions by transaction ID
    When I search for transaction ID "0000001"
    Then I should see transaction results listed

  Scenario: Select a transaction from the list
    When I search for transaction ID "0000001"
    And I select transaction row 1
    Then I should be taken to the transaction detail view

  Scenario: Paginate through transaction list
    When I view the transaction list
    Then I should see the page number displayed
