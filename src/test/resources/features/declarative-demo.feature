@Declarative @Smoke @all
Feature: Declarative layer demo
  Proves the declarative layer end to end without a database or a browser: both steps below are
  sentences from bundles/demo-bundles.feature, and neither has a Java step definition.

  @declarative:engineDemo
  Scenario: A declarative scenario runs its bundle recipe
    Given the tester records a search term of "laptop"
    Then the tester waits 1 second for the system to settle
