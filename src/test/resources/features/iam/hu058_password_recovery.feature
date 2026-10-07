@iam @HU-058
Feature: Password recovery
  As a user
  I want to recover my credentials through my linked email address
  So that I can access my account again

  Background:
    Given a registered account with email "camila@example.com" and password "GreenPlanet2026"

  Scenario: Recovery link sent to a registered email
    When I request a password recovery for "camila@example.com"
    Then the response status is 200
    And a recovery link is sent to "camila@example.com"

  Scenario: Recovery requested for an email that is not registered
    When I request a password recovery for "nobody@example.com"
    Then the response status is 200
    And the response message is "If the email address is registered, a recovery link has been sent."
    And no recovery link is sent

  Scenario: Successful password reset
    Given I requested a password recovery for "camila@example.com"
    When I set the new password "NewGreenPlanet2026" with the recovery link I received
    Then the response status is 204
    When I sign in with email "camila@example.com" and password "NewGreenPlanet2026"
    Then the response status is 200
    When I sign in with email "camila@example.com" and password "GreenPlanet2026"
    Then the response status is 401

  Scenario: A recovery link cannot be used twice
    Given I requested a password recovery for "camila@example.com"
    And I set the new password "NewGreenPlanet2026" with the recovery link I received
    When I set the new password "AnotherPassword2026" with the recovery link I received
    Then the response status is 422
    And the error code is "PASSWORD_RESET_TOKEN_INVALID"

  Scenario: New password that does not meet the security rules
    Given I requested a password recovery for "camila@example.com"
    When I set the new password "short" with the recovery link I received
    Then the response status is 400
    And the error code is "PASSWORD_POLICY_VIOLATION"
