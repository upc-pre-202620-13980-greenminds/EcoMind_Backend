@iam @HU-057
Feature: Sign in
  As a registered user
  I want to authenticate with my email and password
  So that I can establish a secure session

  Background:
    Given a registered account with email "camila@example.com" and password "GreenPlanet2026"

  Scenario: Successful sign in
    When I sign in with email "camila@example.com" and password "GreenPlanet2026"
    Then the response status is 200
    And I receive an access token
    And the access token identifies the account "camila@example.com"

  Scenario: Sign in with a wrong password
    When I sign in with email "camila@example.com" and password "WrongPassword2026"
    Then the response status is 401
    And the error code is "INVALID_CREDENTIALS"
    And I do not receive an access token

  Scenario: Sign in with an email that is not registered
    When I sign in with email "nobody@example.com" and password "GreenPlanet2026"
    Then the response status is 401
    And the error code is "INVALID_CREDENTIALS"

  Scenario: Sign in with empty fields
    When I sign in with email "" and password ""
    Then the response status is 400
    And the error code is "VALIDATION_ERROR"

  Scenario: Error message in Latin American Spanish
    Given my language is "es-419"
    When I sign in with email "camila@example.com" and password "WrongPassword2026"
    Then the response status is 401
    And the error message is "El correo o la contraseña son incorrectos."
