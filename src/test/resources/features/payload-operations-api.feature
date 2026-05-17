@API
Feature: Payload operations API demo
  Demonstrates Given variable + When I send API payload using operations registry.

  Scenario: Create post with variable in body
    Given variable title is "My test post"
    When I send API payload "create_post"
    Then the response status code should be 201

  Scenario: List users via GET payload
    When I send API payload "get_users"
    Then the response status code should be 200
