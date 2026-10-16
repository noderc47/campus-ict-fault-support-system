# Database Setup and Connection Check

## Important: saving the SQL file does not create a database

`database/schema.sql` is only a text file in this project. MySQL does **not** create `campus_ict_db`, its tables, or any records until you execute that file against a MySQL server.

The final schema uses these exact table names:

- `users`
- `technicians`
- `tickets`

The Java JDBC connection check uses those same table names.

## 1. Inspect MySQL before running the schema

If `campus_ict_db` may already exist, inspect it first. Do not run destructive SQL and do not delete or recreate existing tables.

1. Open **MySQL Workbench**.
2. On the home screen, open your local MySQL connection. Enter the MySQL password when Workbench asks for it.
3. In the SQL editor, run this inspection query:

```sql
SHOW DATABASES LIKE 'campus_ict_db';
```

4. If no result is returned, the database does not exist yet. Continue to the fresh-database instructions below.
5. If `campus_ict_db` is returned, inspect the existing tables and records before making any changes:

```sql
USE campus_ict_db;
SHOW TABLES;
DESCRIBE users;
DESCRIBE technicians;
DESCRIBE tickets;
SELECT COUNT(*) AS user_count FROM users;
SELECT COUNT(*) AS technician_count FROM technicians;
SELECT COUNT(*) AS ticket_count FROM tickets;
```

Before changing an existing database, create a backup in MySQL Workbench using **Server** > **Data Export**. Select `campus_ict_db`, choose **Export to Self-Contained File**, select a safe backup location, and start the export.

The supplied schema uses `CREATE DATABASE IF NOT EXISTS` and `CREATE TABLE IF NOT EXISTS`; it has no `DROP TABLE` or `INSERT` statements. This prevents automatic deletion and avoids sample-record duplication. However, `CREATE TABLE IF NOT EXISTS` does not alter an already existing table to match a newer definition. If the inspection shows different columns or constraints, stop and review the differences before applying manual migration SQL.

## 2. Run the schema in MySQL Workbench for a fresh database

Use these steps only when `campus_ict_db` does not exist, or when inspection confirms that applying the script is appropriate.

1. In MySQL Workbench, choose **File** > **Open SQL Script**.
2. Select:

```text
Source_code\database\schema.sql
```

3. Confirm that the editor contains `CREATE DATABASE IF NOT EXISTS campus_ict_db;` and `USE campus_ict_db;`.
4. Click the lightning-bolt **Execute** button, or press **Ctrl+Shift+Enter**, to execute the full script.
5. Refresh the **Schemas** panel. Expand `campus_ict_db` > **Tables** and confirm `users`, `technicians`, and `tickets` appear.

Optionally verify the result in a new query tab:

```sql
USE campus_ict_db;
SHOW TABLES;
```

## 3. Configure the Java application

Copy the template to a private configuration file:

```powershell
Copy-Item .\config\db.properties.example .\config\db.properties
```

Open `config\db.properties` and replace the placeholder username and password. For a normal local MySQL installation, keep the URL unless your MySQL server uses a different host or port:

```properties
db.url=jdbc:mysql://localhost:3307/campus_ict_db?useSSL=false&serverTimezone=UTC
db.username=your_mysql_username
db.password=your_mysql_password
```

`config\db.properties` is listed in `.gitignore`, so it is not committed to Git. Do not put real credentials in `db.properties.example`.

## 4. Test the connection from the terminal

From the project directory, run:

```powershell
.\mvnw.cmd -q "-Dexec.mainClass=com.campusict.database.DatabaseConnectionCheck" exec:java
```

The check opens a JDBC connection, runs `SELECT 1`, and confirms that `users`, `technicians`, and `tickets` exist. It prints helpful connection or configuration errors but never prints the password.

## Optional: use a different configuration file

You can keep credentials elsewhere and point the check at that file:

```powershell
.\mvnw.cmd -q "-Ddb.config=C:\safe-folder\db.properties" "-Dexec.mainClass=com.campusict.database.DatabaseConnectionCheck" exec:java
```