/**
 * Test Runners - Interceptors that route tests to appropriate step definitions.
 * <p>
 * Available Runners:
 * <ul>
 *   <li>{@link com.qa.framework.runners.BaseTestRunner} - Routes to all test types (DB, UI, API)</li>
 *   <li>{@link com.qa.framework.runners.DBTestRunner} - Routes only DB tagged tests to database step definitions</li>
 *   <li>{@link com.qa.framework.runners.UITestRunner} - Routes only UI tagged tests to UI step definitions</li>
 *   <li>{@link com.qa.framework.runners.APITestRunner} - Routes only API tagged tests to API step definitions</li>
 * </ul>
 * </p>
 * <p>
 * How Routing Works:
 * Each runner acts as an interceptor that scans for feature files, filters scenarios by tags,
 * and routes to appropriate step definitions based on glue path.
 * </p>
 * <p>
 * Usage in Template Project:
 * Run all tests: mvn test -Dtest=BaseTestRunner
 * Run only DB tests: mvn test -Dtest=DBTestRunner
 * Run only UI tests: mvn test -Dtest=UITestRunner
 * Run only API tests: mvn test -Dtest=APITestRunner
 * </p>
 * <p>
 * Extending for Custom Libraries:
 * To add support for your UI or API library, update the glue path in UITestRunner or APITestRunner,
 * point to your library's step definitions package, and import your library as a Maven dependency
 * in the template project.
 * </p>
 * 
 * @since 1.0.0
 */
package com.qa.framework.runners;
