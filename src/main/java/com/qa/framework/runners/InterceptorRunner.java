package com.qa.framework.runners;

import org.junit.platform.suite.api.SelectClasses;
import org.junit.platform.suite.api.Suite;

/**
 * Interceptor Runner - Entry point for DB tests (JUnit).
 * Runs only @DB tagged scenarios via DBTestRunner.
 * For UI/API use {@link UIAPITestNGRunner} (TestNG).
 *
 * Invoke: mvn test -Dtest=InterceptorRunner
 */
@Suite
@SelectClasses({
    DBTestRunner.class
})
public class InterceptorRunner {
}
