Feature: CardDemo Card Update
  As a regular CardDemo user
  I want to update credit card details
  So that I can modify card name, status, or expiry information

  Background:
    Given I am logged in as a regular user
    And I navigate to the Card Update screen

  Scenario: Update card name
    When I load a card for update with account "00000000001" and card "4111111111111111"
    And I update the card name to "JOHN A DOE"
    And I submit the card update
    Then I should see a card update confirmation or updated details

  Scenario: Update card status
    When I load a card for update with account "00000000001" and card "4111111111111111"
    And I update the card status to "Y"
    And I submit the card update
    Then I should see a card update confirmation or updated details

  Scenario: Update card expiry date
    When I load a card for update with account "00000000001" and card "4111111111111111"
    And I update the card expiry month to "12" and year to "2030"
    And I submit the card update
    Then I should see a card update confirmation or updated details
