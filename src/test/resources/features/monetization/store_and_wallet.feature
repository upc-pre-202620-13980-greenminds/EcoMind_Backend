@monetization
Feature: Store and gem wallet
  The authenticated user browses products and purchases within the available balance.

  Background:
    Given the parent "Ana" is registered

  @HU-035 @HU-029
  Scenario: Browse available products
    When "Ana" opens the store catalog
    Then the response status is 200
    And the store lists gem packages and personalization products

  @HU-035
  Scenario: A new user has no gems
    When "Ana" checks the gem wallet
    Then the response status is 200
    And the gem wallet balance is 0

  @HU-067
  Scenario: Insufficient balance does not grant a protector or debit gems
    When "Ana" buys a streak protector without enough gems
    Then the response status is 409
    And the error code is "INSUFFICIENT_GEM_BALANCE"
    When "Ana" checks the gem wallet
    Then the gem wallet balance is 0
    When "Ana" checks the inventory
    Then the response status is 200
    And the inventory has no purchased items

  @HU-067
  Scenario: Purchases require an idempotency key
    When "Ana" submits a purchase without an idempotency key
    Then the response status is 400
