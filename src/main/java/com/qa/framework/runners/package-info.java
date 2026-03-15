/**
 * Test Runners - Entry points for DB (JUnit) and UI/API (TestNG).
 * <p>
 * Available Runners:
 * <ul>
 *   <li>{@link com.qa.framework.runners.InterceptorRunner} - JUnit suite; runs only @DB via DBTestRunner</li>
 *   <li>{@link com.qa.framework.runners.DBTestRunner} - DB tagged tests only (JUnit)</li>
 *   <li>{@link com.qa.framework.runners.UIAPITestNGRunner} - UI and API scenarios (TestNG, extends ace-base TestNGRunner)</li>
 *   <li>{@link com.qa.framework.runners.BaseTestRunner} - All test types in one JUnit run (db + ui + api glue)</li>
 * </ul>
 * </p>
 * <p>
 * Usage:
 * Run DB only: mvn test -Dtest=InterceptorRunner or -Dtest=DBTestRunner
 * Run UI/API: mvn test -Dtest=com.qa.framework.runners.UIAPITestNGRunner
 * Run all (JUnit): mvn test -Dtest=BaseTestRunner
 * </p>
 *
 * @since 1.0.0
 */
package com.qa.framework.runners;
