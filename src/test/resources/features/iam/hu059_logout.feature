@iam @HU-059
Feature: Log out
  As a user
  I want to finish my active session with the "Log out" option
  So that my personal information stays protected

  Background:
    Given a registered account with email "camila@example.com" and password "GreenPlanet2026"

  Scenario: Successful log out
    Given I am signed in as "camila@example.com" with password "GreenPlanet2026"
    When I log out
    Then the response status is 204

  Scenario: Access is restricted after logging out
    Given I am signed in as "camila@example.com" with password "GreenPlanet2026"
    And I log out
    When I request my account without an access token
    Then the response status is 401
    And the error code is "UNAUTHORIZED"
    And the error message is "Authentication is required to access this resource."

  Scenario: Log out without a session
    When I log out without an access token
    Then the response status is 401
    And the error code is "UNAUTHORIZED"
