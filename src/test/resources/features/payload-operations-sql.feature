@DB
Feature: Payload operations SQL demo
  Requires a live database matching config/local master-config db.mysql.

  Scenario: Count users by email variable
    Given I connect to database "mysql" as "app"
    And variable email is "john@example.com"
    When I run SQL payload "count_users_by_email" on "app"
    Then the query should execute successfully
