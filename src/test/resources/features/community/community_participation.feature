@community
Feature: Community participation
  Registered users publish and join environmental activities within their community.

  Background:
    Given the parent "Ana" is registered
    And the student "Bruno" is registered
    And a local community managed by "Ana"

  @HU-060
  Scenario: Join a local community once
    When "Bruno" joins the community
    Then the response status is 201
    When "Bruno" joins the community
    Then the response status is 409

  @HU-061 @HU-062
  Scenario: A student cannot create a topic community
    When "Bruno" creates a topic community with 3 places
    Then the response status is 403

  @HU-061 @HU-062
  Scenario: The creator occupies a place in the topic community
    When "Ana" creates a topic community with 1 places
    Then the response status is 201
    When "Bruno" joins the community
    Then the response status is 409

  @HU-033
  Scenario: A member publishes using the authenticated identity
    When "Bruno" joins the community
    Then the response status is 201
    When "Bruno" publishes "Today we saved energy" in the community
    Then the response status is 201
    And the publication belongs to "Bruno"

  @HU-033
  Scenario: A nonmember cannot publish
    When "Bruno" publishes "Today we saved energy" in the community
    Then the response status is 403

  @HU-033
  Scenario: Only the author can remove a publication
    When "Ana" publishes "Today we saved energy" in the community
    Then the response status is 201
    When "Bruno" deletes the publication
    Then the response status is 403
    When "Ana" deletes the publication
    Then the response status is 204

  @HU-014 @HU-037
  Scenario: An event rejects registrations when its places are full
    When "Ana" creates an event with 1 places
    Then the response status is 201
    When "Bruno" registers for the event individually
    Then the response status is 409

  @HU-014 @HU-037
  Scenario: Cancellation releases a place and allows a new registration
    When "Ana" creates an event with 2 places
    Then the response status is 201
    When "Bruno" registers for the event individually
    Then the response status is 201
    When "Ana" cancels the event registration
    Then the response status is 403
    When "Bruno" cancels the event registration
    Then the response status is 204
    When "Bruno" registers for the event individually
    Then the response status is 201

  @HU-037
  Scenario: Only parents create environmental events
    When "Bruno" creates an event with 2 places
    Then the response status is 403

  @HU-063 @HU-064
  Scenario: Members complete a community goal only once
    When "Ana" creates a goal of 1 energy quests
    Then the response status is 201
    When "Bruno" contributes to the community goal
    Then the response status is 403
    When "Bruno" joins the community
    Then the response status is 201
    When "Bruno" contributes to the community goal
    Then the response status is 200
    And the community goal is completed
    And the community has exactly 1 goal achievement
    When "Bruno" contributes to the community goal
    Then the response status is 422
    And the community has exactly 1 goal achievement

  @HU-063
  Scenario: A community can only have one active goal
    When "Ana" creates a goal of 2 energy quests
    Then the response status is 201
    When "Ana" creates a goal of 3 energy quests
    Then the response status is 409

  @HU-034
  Scenario: A user changes and removes a reaction without duplicating it
    When "Ana" publishes "We planted a tree" in the community
    Then the response status is 201
    When "Bruno" reacts to the publication with "like"
    Then the response status is 201
    And the publication has 1 reactions
    When "Bruno" changes the publication reaction to "love"
    Then the response status is 200
    And the publication has 1 reactions
    When "Bruno" removes the publication reaction
    Then the response status is 204
    And the publication has 0 reactions

  @HU-034
  Scenario: Unsupported reactions are rejected
    When "Ana" publishes "We planted a tree" in the community
    And "Bruno" reacts to the publication with "unknown"
    Then the response status is 400
    And the publication has 0 reactions
