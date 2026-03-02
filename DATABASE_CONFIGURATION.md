# Database Configuration Guide

This library supports multiple database types through a profile-based configuration system. You can easily switch between MySQL, PostgreSQL, SQL Server, and Oracle databases.

## Supported Databases

- **MySQL** - Profile: `mysql`
- **PostgreSQL** - Profile: `postgresql`
- **SQL Server** - Profile: `sqlserver`
- **Oracle** - Profile: `oracle`

## Configuration Files

Configuration files are located in `src/test/resources/config/` directory:

- `mysql-config.properties`
- `postgresql-config.properties`
- `sqlserver-config.properties`
- `oracle-config.properties`

### Configuration File Structure

Each configuration file contains the following properties:

```properties
# Database type
db.type=mysql

# JDBC driver class
db.driver=com.mysql.cj.jdbc.Driver

# Connection details
db.url=jdbc:mysql://localhost:3306/testdb
db.host=localhost
db.port=3306
db.database=testdb
db.username=root
db.password=password

# Connection settings
db.connection.timeout=30000
db.connection.pool.size=10
db.ssl.enabled=false

# Database-specific properties
db.mysql.useSSL=false
db.mysql.serverTimezone=UTC
```

## Setting the Active Profile

### Method 1: System Property (Recommended)

Set the profile using Maven command line:

```bash
mvn test -Ddb.profile=mysql
mvn test -Ddb.profile=postgresql
mvn test -Ddb.profile=sqlserver
mvn test -Ddb.profile=oracle
```

### Method 2: Programmatically

```java
ConfigurationManager configManager = ConfigurationManager.getInstance();
configManager.setActiveProfile("mysql");
```

### Method 3: In Feature Files

```gherkin
Given I set the active database profile to "mysql"
```

## Usage Examples

### Example 1: Using Profile in Java Code

```java
import com.qa.framework.config.ConfigurationManager;
import com.qa.framework.db.DatabaseConnection;
import com.qa.framework.db.DatabaseConnectionFactory;

// Set active profile
ConfigurationManager.getInstance().setActiveProfile("mysql");

// Create connection from active profile
DatabaseConnection conn = DatabaseConnectionFactory.createAndConnect();

// Execute queries
List<Map<String, Object>> results = conn.executeQuery("SELECT * FROM users");

conn.disconnect();
```

### Example 2: Using Specific Profile

```java
// Create connection from specific profile (ignores active profile)
DatabaseConnection mysqlConn = DatabaseConnectionFactory.createAndConnectFromProfile("mysql");
DatabaseConnection pgConn = DatabaseConnectionFactory.createAndConnectFromProfile("postgresql");

// Use different databases simultaneously
List<Map<String, Object>> mysqlData = mysqlConn.executeQuery("SELECT * FROM mysql_table");
List<Map<String, Object>> pgData = pgConn.executeQuery("SELECT * FROM pg_table");

mysqlConn.disconnect();
pgConn.disconnect();
```

### Example 3: Using in Cucumber Feature Files

```gherkin
@DB
Feature: Multi-Database Testing

  Scenario: Test MySQL database
    Given I set the active database profile to "mysql"
    And I have a database connection named "mydb" using profile "mysql"
    When I connect to the database "mydb"
    And I execute the query "SELECT * FROM users"
    Then the query should execute successfully

  Scenario: Test PostgreSQL database
    Given I set the active database profile to "postgresql"
    And I have a database connection named "pgdb" using profile "postgresql"
    When I connect to the database "pgdb"
    And I execute the query "SELECT * FROM users"
    Then the query should execute successfully
```

## Customizing Configuration

### For Your Environment

1. Copy the appropriate config file (e.g., `mysql-config.properties`)
2. Update the connection details:
   ```properties
   db.host=your-db-host
   db.port=3306
   db.database=your_database
   db.username=your_username
   db.password=your_password
   ```
3. Run tests with the profile:
   ```bash
   mvn test -Ddb.profile=mysql
   ```

### Creating Custom Profiles

You can create additional profiles for different environments:

1. Create a new file: `src/test/resources/config/dev-mysql-config.properties`
2. Add your configuration
3. Use it in your code:
   ```java
   DatabaseConnection conn = DatabaseConnectionFactory.createConnectionFromProfile("dev-mysql");
   ```

## Database-Specific Notes

### MySQL
- Default port: 3306
- Driver: `com.mysql.cj.jdbc.Driver`
- URL format: `jdbc:mysql://host:port/database`
- Included in library dependencies

### PostgreSQL
- Default port: 5432
- Driver: `org.postgresql.Driver`
- URL format: `jdbc:postgresql://host:port/database`
- Included in library dependencies

### SQL Server
- Default port: 1433
- Driver: `com.microsoft.sqlserver.jdbc.SQLServerDriver`
- URL format: `jdbc:sqlserver://host:port;databaseName=database`
- Included in library dependencies
- Note: May require additional authentication setup

### Oracle
- Default port: 1521
- Driver: `oracle.jdbc.driver.OracleDriver`
- URL format: `jdbc:oracle:thin:@host:port:sid`
- Included in library dependencies
- Note: Supports both SID and Service Name connection types

## Running Tests with Different Profiles

```bash
# Run all DB tests with MySQL
mvn test -Dtest=CucumberTestRunner -Ddb.profile=mysql

# Run all DB tests with PostgreSQL
mvn test -Dtest=CucumberTestRunner -Ddb.profile=postgresql

# Run specific tagged scenarios
mvn test -Dtest=CucumberTestRunner -Ddb.profile=mysql -Dcucumber.filter.tags="@DB and @MySQL"
```

## Troubleshooting

### Connection Issues

1. **Driver not found**: Ensure the JDBC driver is included in dependencies
2. **Connection timeout**: Check host, port, and network connectivity
3. **Authentication failed**: Verify username and password in config file
4. **Database not found**: Confirm database name and ensure it exists

### Configuration Issues

1. **Profile not found**: Check that the config file exists in `src/test/resources/config/`
2. **Property not found**: Ensure all required properties are defined in the config file
3. **Invalid URL**: Verify the JDBC URL format for your database type

## Best Practices

1. **Never commit sensitive credentials**: Use environment variables or external config for production
2. **Use separate profiles for environments**: dev, test, staging, prod
3. **Keep config files organized**: Use consistent naming conventions
4. **Document custom properties**: Add comments to explain non-standard settings
5. **Test connection before running tests**: Verify database connectivity first
