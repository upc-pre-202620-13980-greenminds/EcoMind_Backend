@users @HU-019
Feature: Family group
  As a parent
  I want to create my family group and manage its members
  So that we can do activities together and I can review the activity of my children

  Background:
    Given the parent "Rosa" is registered
    And the student "Luis" is registered

  Scenario: A parent creates a family group
    When "Rosa" creates the family "Torres Family" with the commitment "We will separate our waste every day"
    Then the response status is 201
    And the family has 1 member
    And "Rosa" is a member of the family with role "PARENT"

  Scenario: A parent adds a member to the family
    Given "Rosa" has created the family "Torres Family"
    When "Rosa" adds "Luis" to the family with role "CHILD"
    Then the response status is 201
    And the family has 2 members
    And "Luis" is a member of the family with role "CHILD"

  Scenario: A parent removes a member from the family
    Given "Rosa" has created the family "Torres Family"
    And "Rosa" has added "Luis" to the family with role "CHILD"
    When "Rosa" removes "Luis" from the family
    Then the response status is 204
    And "Luis" does not belong to any family

  Scenario: A student cannot create a family group
    When "Luis" creates the family "Friends Club" with the commitment "We will save water"
    Then the response status is 422
    And the error code is "FAMILY_CREATOR_NOT_PARENT"

  Scenario: Only a parent of the family can add members
    Given the student "Ana" is registered
    And "Rosa" has created the family "Torres Family"
    And "Rosa" has added "Luis" to the family with role "CHILD"
    When "Luis" adds "Ana" to the family with role "CHILD"
    Then the response status is 403
    And the error code is "FAMILY_ACCESS_FORBIDDEN"

  Scenario: A user cannot belong to two families
    Given the parent "Carlos" is registered
    And "Rosa" has created the family "Torres Family"
    And "Rosa" has added "Luis" to the family with role "CHILD"
    And "Carlos" has created the family "Ramos Family"
    When "Carlos" adds "Luis" to the family with role "CHILD"
    Then the response status is 409
    And the error code is "FAMILY_MEMBERSHIP_CONFLICT"

  Scenario: A parent cannot remove themselves from the family
    Given "Rosa" has created the family "Torres Family"
    When "Rosa" removes "Rosa" from the family
    Then the response status is 422
    And the error code is "FAMILY_PARENT_SELF_REMOVAL"
