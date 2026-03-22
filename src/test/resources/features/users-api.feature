@API
Feature: Users API using feature payload YAML

  Base URL comes from merged API config; paths and JSON bodies live in users-api_payload.yml.

  Background:
    Given the API base URL from config

  Scenario: Create user with body from payload YAML
    When I set the body from feature payload "bodies.create_user"
    And I send a POST request to path from feature payload "paths.users"
    Then the response status code should be 201

  Scenario: Update user with body from payload YAML
    When I set the body from feature payload "bodies.update_user"
    And I send a PUT request to path from feature payload "paths.user_by_id"
    Then the response status code should be 200

  Scenario: Partial update with PATCH
    When I set the body from feature payload "bodies.patch_user"
    And I send a PATCH request to path from feature payload "paths.user_by_id"
    Then the response status code should be 200

  Scenario: Fetch user (no body)
    When I send a GET request to path from feature payload "paths.user_by_id"
    Then the response status code should be 200
