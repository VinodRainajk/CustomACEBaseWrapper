# Quick Start Guide

## Installation

1. Build and install the library:
```bash
mvn clean install
```

2. Add to your project's `pom.xml`:
```xml
<dependency>
    <groupId>com.qa.framework</groupId>
    <artifactId>custom-ace-base-wrapper</artifactId>
    <version>1.0.0</version>
</dependency>
```

## Database Configuration

### Step 1: Choose Your Database Profile

The library supports 4 database types out of the box:
- `mysql` - MySQL database
- `postgresql` - PostgreSQL database
- `sqlserver` - Microsoft SQL Server
- `oracle` - Oracle database

### Step 2: Configure Database Connection

Edit the appropriate config file in `src/test/resources/config/`:

**Example: `mysql-config.properties`**
```properties
db.type=mysql
db.driver=com.mysql.cj.jdbc.Driver
db.url=jdbc:mysql://localhost:3306/testdb
db.host=localhost
db.port=3306
db.database=testdb
db.username=root
db.password=password
```

### Step 3: Use in Your Code

**Java Example:**
```java
import com.qa.framework.db.DatabaseConnection;
import com.qa.framework.db.DatabaseConnectionFactory;
import java.util.List;
import java.util.Map;

public class DatabaseTest {
    public static void main(String[] args) {
        // Create connection from profile
        DatabaseConnection conn = DatabaseConnectionFactory
            .createAndConnectFromProfile("mysql");
        
        // Execute query
        List<Map<String, Object>> results = 
            conn.executeQuery("SELECT * FROM users");
        
        // Print results
        results.forEach(row -> System.out.println(row));
        
        // Close connection
        conn.disconnect();
    }
}
```

**Cucumber Example:**
```gherkin
@DB
Feature: User Database Tests

  Scenario: Verify user data
    Given I set the active database profile to "mysql"
    And I have a database connection named "testdb" using profile "mysql"
    When I connect to the database "testdb"
    And I execute the query "SELECT * FROM users WHERE id = 1"
    Then the query should return 1 row(s)
    And the first row should contain column "name" with value "John Doe"
```

### Step 4: Run Tests

**Run with specific profile:**
```bash
mvn test -Ddb.profile=mysql
mvn test -Ddb.profile=postgresql
```

**Run Cucumber tests:**
```bash
mvn test -Dtest=CucumberTestRunner -Ddb.profile=mysql
```

**Run specific tagged scenarios:**
```bash
mvn test -Dtest=CucumberTestRunner -Dcucumber.filter.tags="@DB and @MySQL"
```

## Common Use Cases

### Use Case 1: Single Database Connection

```java
DatabaseConnection conn = DatabaseConnectionFactory
    .createAndConnectFromProfile("mysql");

List<Map<String, Object>> users = 
    conn.executeQuery("SELECT * FROM users");

conn.disconnect();
```

### Use Case 2: Multiple Database Connections

```java
DatabaseConnection mysql = DatabaseConnectionFactory
    .createAndConnectFromProfile("mysql");
DatabaseConnection postgres = DatabaseConnectionFactory
    .createAndConnectFromProfile("postgresql");

// Query both databases
List<Map<String, Object>> mysqlData = 
    mysql.executeQuery("SELECT * FROM mysql_table");
List<Map<String, Object>> pgData = 
    postgres.executeQuery("SELECT * FROM pg_table");

mysql.disconnect();
postgres.disconnect();
```

### Use Case 3: Prepared Statements

```java
DatabaseConnection conn = DatabaseConnectionFactory
    .createAndConnectFromProfile("mysql");

List<Map<String, Object>> results = conn.executePreparedQuery(
    "SELECT * FROM users WHERE email = ? AND status = ?",
    "user@example.com",
    "active"
);

conn.disconnect();
```

### Use Case 4: Database Updates

```java
DatabaseConnection conn = DatabaseConnectionFactory
    .createAndConnectFromProfile("mysql");

int rowsAffected = conn.executeUpdate(
    "UPDATE users SET last_login = NOW() WHERE id = 1"
);

System.out.println("Updated " + rowsAffected + " rows");

conn.disconnect();
```

## Available Cucumber Step Definitions

### Connection Steps
```gherkin
Given I have a database connection named "mydb" using profile "mysql"
Given I set the active database profile to "mysql"
Given I connect to the database "mydb"
When I connect to database using the active profile
When I disconnect from the database
Then I should be connected to the database
Then I should not be connected to the database
```

### Query Execution Steps
```gherkin
When I execute the query "SELECT * FROM users"
When I execute the update query "UPDATE users SET status = 'active'"
When I execute the prepared query "SELECT * FROM users WHERE id = ?" with parameters:
```

### Verification Steps
```gherkin
Then the query should return 1 row(s)
Then the query should return at least 5 row(s)
Then the first row should contain column "name" with value "John"
Then the update should affect 1 row(s)
Then the query should execute successfully
Then the query should fail with an error
Then the result set should contain a column "email"
Then all rows should have column "id" not null
```

## Troubleshooting

### Problem: Driver not found
**Solution:** Ensure the database driver is included in your `pom.xml` dependencies.

### Problem: Connection refused
**Solution:** Check that:
- Database server is running
- Host and port are correct in config file
- Firewall allows connections

### Problem: Authentication failed
**Solution:** Verify username and password in the config file.

### Problem: Configuration file not found
**Solution:** Ensure the config file exists in `src/test/resources/config/` directory.

## Next Steps

- Read [DATABASE_CONFIGURATION.md](DATABASE_CONFIGURATION.md) for detailed configuration options
- Check example feature files in `src/test/resources/features/db/`
- Review step definitions in `src/test/java/com/qa/framework/stepdefinitions/db/`
- Customize configuration files for your environment
