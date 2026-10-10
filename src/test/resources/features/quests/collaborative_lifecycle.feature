@quest_lifecycle @HU-001 @HU-004
Feature: Collaborative quest participation
  As a family participant
  I want invitations and shared progress to respect my membership
  So that only accepted participants can act on the session

  Background:
    Given the parent "Rosa" is registered
    And the student "Luis" is registered
    And the student "Ana" is registered
    And "Rosa" and "Luis" share a family for collaborative quests
    And "Rosa" has a published collaborative quest session
    And "Rosa" invites "Luis" to the collaborative session

  Scenario Outline: An unrelated account cannot answer someone else's invitation
    When "Ana" attempts to "<action>" the collaborative invitation
    Then the response status is 403
    And the collaborative invitation remains pending for "Luis"
    Examples:
      | action  |
      | accept  |
      | decline |
      | leave   |

  Scenario: Accepting the invitation enables shared progress
    When "Luis" attempts to "accept" the collaborative invitation
    Then the response status is 200
    When "Rosa" starts the shared session
    Then the response status is 200
    When "Luis" completes the shared activity
    Then both collaborative participants are ready to complete

  Scenario: Declining does not supply the required second participant
    When "Luis" attempts to "decline" the collaborative invitation
    Then the response status is 200
    When "Rosa" starts the shared session
    Then the response status is 422

  Scenario: An owner can withdraw a pending invitation
    When "Rosa" attempts to "remove" the collaborative invitation
    Then the response status is 200
    And the collaborative invitation is rejected for "Luis"
