@quests @HU-001
Feature: Guided environmental quests
  A published quest tracks assigned activities before completion.

  Background:
    Given the student "Bruno" is registered
    And a draft environmental quest created by "Bruno"

  Scenario: Drafts are absent from the public catalog
    When "Bruno" reads the published quest catalog
    Then the response status is 200
    And the environmental quest is absent from the public catalog

  Scenario: Draft quests cannot be started
    When "Bruno" starts the environmental quest
    Then the response status is 422

  Scenario: A published quest belongs to its authenticated participant
    When "Bruno" publishes the environmental quest
    Then the response status is 200
    When "Bruno" starts the environmental quest
    Then the response status is 201
    And the quest assignment belongs to "Bruno"
    And the quest assignment status is "IN_PROGRESS"
    When "Bruno" starts the environmental quest
    Then the response status is 409

  Scenario: A quest cannot finish with pending activities
    When "Bruno" publishes the environmental quest
    And "Bruno" starts the environmental quest
    And "Bruno" completes the quest assignment
    Then the response status is 422

  @HU-004
  Scenario: Complete all activities before completing a quest
    When "Bruno" publishes the environmental quest
    And "Bruno" starts the environmental quest
    And "Bruno" checks the assigned activity
    Then the response status is 200
    When "Bruno" completes the quest assignment
    Then the response status is 200
    And the quest assignment status is "COMPLETED"
    And "Bruno" has earned 10 ecopoints from the quest
    When "Bruno" completes the quest assignment
    Then the response status is 200
    And "Bruno" has earned 10 ecopoints from the quest

  Scenario: Cancellation preserves assignment history
    When "Bruno" publishes the environmental quest
    And "Bruno" starts the environmental quest
    And "Bruno" cancels the quest assignment
    Then the response status is 200
    And the quest assignment status is "CANCELLED"
    When "Bruno" checks the assigned activity
    Then the response status is 422

  Scenario: Archived quests cannot receive new assignments
    When "Bruno" publishes the environmental quest
    And "Bruno" archives the environmental quest
    Then the response status is 200
    When "Bruno" starts the environmental quest
    Then the response status is 422

  @HU-046
  Scenario Outline: Another account cannot access or change a quest assignment
    Given the parent "Ana" is registered
    When "Bruno" publishes the environmental quest
    And "Bruno" starts the environmental quest
    Then the response status is 201
    When "Ana" attempts to "<operation>" another user's quest assignment
    Then the response status is 403
    And "Bruno" still has the quest in progress

    Examples:
      | operation       |
      | read            |
      | read version    |
      | cancel          |
      | complete        |
      | list activities |
      | read activity   |
      | find activity   |
      | submit activity |
      | assign activity |
