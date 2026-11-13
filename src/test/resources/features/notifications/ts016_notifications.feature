@notifications @TS-016
Feature: Notification inbox
  As a student
  I want to see my notifications and mark them as read
  So that I know what happened in EcoMind

  Background:
    Given the student "Luis" is registered
    And the student "Ana" is registered

  Scenario: Accepting a friend request notifies the requester
    Given "Luis" has sent a friend request to "Ana"
    And "Ana" accepts the friend request
    When "Luis" lists their notifications
    Then the response status is 200
    And "Luis" has 1 notification of type "FRIEND_REQUEST_ACCEPTED"
    And the first notification says "Ana accepted your friend request."
    And "Luis" has 1 unread notification
    And "Ana" has 0 unread notifications

  Scenario: A notification is marked as read
    Given "Luis" has sent a friend request to "Ana"
    And "Ana" accepts the friend request
    When "Luis" marks the first notification as read
    Then the response status is 200
    And "Luis" has 0 unread notifications

  Scenario: Every notification is marked as read at once
    Given "Luis" creates a notification "Water reminder"
    And "Luis" creates a notification "Energy reminder"
    When "Luis" marks all notifications as read
    Then the response status is 204
    And "Luis" has 0 unread notifications

  Scenario: The notification of another user cannot be marked as read
    Given "Luis" creates a notification "Water reminder"
    When "Ana" marks the notification of "Luis" as read
    Then the response status is 403
    And the error code is "NOTIFICATION_ACCESS_FORBIDDEN"

  Scenario: A notification that does not exist cannot be marked as read
    When "Luis" marks a notification that does not exist as read
    Then the response status is 404
    And the error code is "NOTIFICATION_NOT_FOUND"

  Scenario: Notification in Latin American Spanish
    Given my language is "es-419"
    And "Luis" has sent a friend request to "Ana"
    And "Ana" accepts the friend request
    When "Luis" lists their notifications
    Then the first notification says "Ana aceptó tu solicitud de amistad."

  @HU-018 @community
  Scenario: Only the parents are alerted about a new community event
    Given the parent "Carla" is registered
    And the parent "Diego" is registered
    And a local community created by "Carla"
    And "Diego" joins the community
    And "Luis" joins the community
    When "Carla" creates an event with 10 places
    Then the response status is 201
    And "Diego" has 1 notification of type "COMMUNITY_EVENT_CREATED"
    And "Luis" has 0 notifications of type "COMMUNITY_EVENT_CREATED"
    And "Carla" has 0 notifications of type "COMMUNITY_EVENT_CREATED"

  @HU-018 @community
  Scenario: The alert of a community event in Latin American Spanish
    Given my language is "es-419"
    And the parent "Carla" is registered
    And the parent "Diego" is registered
    And a local community created by "Carla"
    And "Diego" joins the community
    When "Carla" creates an event with 10 places
    And "Diego" lists their notifications
    Then the first notification says "Park cleanup ya está disponible para las familias."
