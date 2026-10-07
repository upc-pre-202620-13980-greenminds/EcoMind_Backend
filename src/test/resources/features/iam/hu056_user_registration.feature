@iam @HU-056
Feature: User registration
  As a user
  I want to create an account and select my role as student or parent
  So that I can access the personalized features of the application

  Scenario: Successful registration
    Given no account exists for "camila@example.com"
    When I sign up with name "Camila Torres", email "camila@example.com", password "GreenPlanet2026" and role "STUDENT"
    Then the response status is 201
    And a verification code is sent to "camila@example.com"
    When I verify "camila@example.com" with the code I received
    Then the response status is 201
    And the account "camila@example.com" exists
    And a profile is requested for "Camila Torres" with role "STUDENT"

  Scenario: Registration with a required field left empty
    When I sign up with name "", email "camila@example.com", password "GreenPlanet2026" and role "STUDENT"
    Then the response status is 400
    And the error code is "VALIDATION_ERROR"
    And the error details mention "name"
    And no verification code is sent to "camila@example.com"

  Scenario: Registration with an email that is already registered
    Given a registered account with email "camila@example.com" and password "GreenPlanet2026"
    When I sign up with name "Another Camila", email "camila@example.com", password "OtherPassword2026" and role "PARENT"
    Then the response status is 409
    And the error code is "EMAIL_CONFLICT"
    And the error message is "This email address is already registered."

  Scenario: Registration with a role that does not exist
    When I sign up with name "Camila Torres", email "camila@example.com", password "GreenPlanet2026" and role "TEACHER"
    Then the response status is 400
    And the error details mention "socialRole"

  Scenario: The account is not created while the email is not verified
    Given I sign up with name "Camila Torres", email "camila@example.com", password "GreenPlanet2026" and role "PARENT"
    When I verify "camila@example.com" with a wrong code
    Then the response status is 422
    And the error code is "VERIFICATION_CODE_INVALID"
    And the account "camila@example.com" does not exist
