@users @HU-039
Feature: Friend requests
  As a student
  I want to send friend requests
  So that I can share learning experiences with my friends

  Background:
    Given the student "Luis" is registered
    And the student "Ana" is registered

  Scenario: Friend request sent successfully
    When "Luis" sends a friend request to "Ana"
    Then the response status is 201
    And the friend request is "PENDING"
    And "Ana" sees a "PENDING" friend request from "Luis"

  Scenario: Friend request accepted
    Given "Luis" has sent a friend request to "Ana"
    When "Ana" accepts the friend request
    Then the response status is 200
    And the friend request is "ACCEPTED"
    And "Luis" sees a "ACCEPTED" friend request from "Luis"

  Scenario: Friend request rejected
    Given "Luis" has sent a friend request to "Ana"
    When "Ana" rejects the friend request
    Then the response status is 200
    And the friend request is "REJECTED"

  Scenario: A rejected request cannot be sent again right away
    Given "Luis" has sent a friend request to "Ana"
    And "Ana" rejects the friend request
    When "Luis" sends a friend request to "Ana"
    Then the response status is 422
    And the error code is "FRIEND_REQUEST_REJECTED_RECENTLY"

  Scenario: A rejected request can be sent again after 7 days
    Given "Luis" has sent a friend request to "Ana"
    And "Ana" rejects the friend request
    And 8 days have passed since the friend request was answered
    When "Luis" sends a friend request to "Ana"
    Then the response status is 201
    And the friend request is "PENDING"

  Scenario: A friend request cannot be duplicated
    Given "Luis" has sent a friend request to "Ana"
    When "Ana" sends a friend request to "Luis"
    Then the response status is 409
    And the error code is "FRIENDSHIP_CONFLICT"

  Scenario: A user cannot send a friend request to themselves
    When "Luis" sends a friend request to "Luis"
    Then the response status is 422
    And the error code is "FRIEND_REQUEST_TO_SELF"

  Scenario: Only the receiver can answer a friend request
    Given "Luis" has sent a friend request to "Ana"
    When "Luis" accepts the friend request
    Then the response status is 403
    And the error code is "FRIEND_REQUEST_ACCESS_FORBIDDEN"

  Scenario: Error message in Latin American Spanish
    Given my language is "es-419"
    When "Luis" sends a friend request to "Luis"
    Then the error message is "No puedes enviarte una solicitud de amistad a ti mismo."
