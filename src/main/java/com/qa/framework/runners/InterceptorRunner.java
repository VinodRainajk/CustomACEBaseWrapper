package com.qa.framework.runners;

import com.acebase.runners.APITestRunner;
import com.acebase.runners.UITestRunner;
import org.junit.platform.suite.api.SelectClasses;
import org.junit.platform.suite.api.Suite;

/**
 * Interceptor Runner - Single entry point for all test types.
 * Dynamically routes scenarios by tag; no tag parameter required.
 * Each child runner filters by its own tag:
 * - @DB  -> DBTestRunner (CustomACEBaseWrapper)
 * - @UI  -> UITestRunner (ACBase)
 * - @API -> APITestRunner (ACBase)
 *
 * Invoke: mvn test -Dtest=InterceptorRunner
 */
@Suite
@SelectClasses({
    DBTestRunner.class,
    UITestRunner.class,
    APITestRunner.class
})
public class InterceptorRunner {
}
