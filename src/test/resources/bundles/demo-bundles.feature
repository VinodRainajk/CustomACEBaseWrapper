@Bundle
Feature: Bundles - engine demo
  Vocabulary used by features/declarative-demo.feature.
  Nothing here runs as a test: the runners only scan classpath:features, and a guard hook fails any
  scenario from a @Bundle file that is picked up anyway.

  @bundle:engineDemo
  Scenario: Recording and pausing
    # BA: the tester records a search term of {string}
    Given variable "searchTerm" is "<0>"
    Given variable "lastSearchTerm" is "<0>"

    # BA: the tester waits {int} second(s) for the system to settle
    Then I wait for <0> seconds before next step
