# Campus ICT Fault Reporting and Support System

## Purpose and coursework context

This repository contains a completed desktop coursework project: a Java Swing application for recording, viewing, and managing ICT fault reports in a campus setting. It stores users, support technicians, and fault tickets in MySQL through JDBC.

The repository includes the source code, Maven Wrapper, database export, user manual, and a runnable JAR distribution. It is a standalone local Git repository; no remote repository is configured.

## Java Swing forms

The application has five top-level Swing forms:

1. **MainMenuFrame** — the main navigation window for opening the functional areas or exiting the application.
2. **UsersManagementFrame** — creates, searches, updates, refreshes, and deletes student or staff reporter records.
3. **TechniciansManagementFrame** — creates, searches, updates, refreshes, and deletes ICT technician records, including availability status.
4. **ReportFaultFrame** — records new faults and displays existing tickets for update or deletion.
5. **SearchManageTicketsFrame** — searches tickets by ID, location, category, or status, and manages ticket details, technician assignment, resolution notes, and resolution status.

## Repository structure

```text
.
├── Database/
│   └── campus_ict_db.sql              # MySQL schema and fictional demonstration records
├── Executable/
│   ├── CampusICT.jar                  # Runnable shaded application JAR
│   ├── README.txt                     # Distribution-specific notes
│   ├── RunCampusICT.cmd               # Windows launcher
│   └── config/
│       └── db.properties.example      # Credential-free configuration template
├── Source_code/
│   ├── .mvn/wrapper/                  # Maven Wrapper, including maven-wrapper.jar
│   ├── config/db.properties.example   # Credential-free source configuration template
│   ├── database/schema.sql            # Non-destructive schema-only script
│   ├── src/main/java/                 # Java source code
│   ├── DATABASE_SETUP.md              # Database setup and connection-check notes
│   ├── mvnw and mvnw.cmd              # Maven Wrapper launchers
│   └── pom.xml                        # Maven project definition
├── user_manual/
│   └── Campus_ICT_User_Manual_Updated.pdf
├── .gitignore
└── README.md
```

`Source_code/target/` is Maven-generated output and is intentionally not versioned. The runnable distribution JAR in `Executable/CampusICT.jar` and Maven Wrapper JAR in `Source_code/.mvn/wrapper/maven-wrapper.jar` are intentionally versioned.

## Requirements

- Java 17 or later. The Maven project compiles for Java 17.
- MySQL Server.
- Maven is not required as a separate installation because the Maven Wrapper is included.
- A MySQL account with access to the `campus_ict_db` database.

## Database import

1. Back up any existing `campus_ict_db` database whose data must be retained.
2. In MySQL Workbench, choose **File > Open SQL Script**.
3. Open `Database/campus_ict_db.sql` and review it before execution.
4. Execute the script against the intended MySQL server.

> **Warning:** `Database/campus_ict_db.sql` includes `DROP TABLE IF EXISTS` statements for `technicians`, `tickets`, and `users`. Running it replaces those tables and their data. Its included rows are fictional demonstration records only.

For a non-destructive, schema-only alternative, see `Source_code/database/schema.sql` and `Source_code/DATABASE_SETUP.md`.

## External database configuration

Credentials are deliberately kept outside Git. Copy the appropriate template and create a private `db.properties` file beside it:

```powershell
Copy-Item .\Executable\config\db.properties.example .\Executable\config\db.properties
```

The coursework distribution documents the local development MySQL endpoint as `localhost` on port `3307`:

```properties
db.url=jdbc:mysql://localhost:3307/campus_ict_db?useSSL=false&serverTimezone=UTC
db.username=your_mysql_username
db.password=your_mysql_password
```

Use the actual host and port for the MySQL server on the computer where the application runs. Both `Executable/config/db.properties` and `Source_code/config/db.properties` are ignored by Git; only their placeholder-only `.example` templates are tracked.

## Run the supplied executable

After importing the database and creating `Executable/config/db.properties`, run either:

- `Executable/RunCampusICT.cmd` by double-clicking it; or
- from Command Prompt or PowerShell:

```powershell
.\Executable\RunCampusICT.cmd
```

The launcher changes to the `Executable` directory before executing `java -jar CampusICT.jar`, allowing the application to find `config/db.properties` through its relative path.

## Build from source

From `Source_code`, use the Maven Wrapper:

```powershell
cd .\Source_code
.\mvnw.cmd clean package
```

The build produces Maven output under `Source_code/target/`. To run the source version after configuring `Source_code/config/db.properties`:

```powershell
.\mvnw.cmd exec:java
```

To run the included database connection check instead:

```powershell
.\mvnw.cmd -q "-Dexec.mainClass=com.campusict.database.DatabaseConnectionCheck" exec:java
```

## Testing status and AI-assisted development

The project has been prepared with a successful Maven packaging check using the included Maven Wrapper and JDK 17. The GUI and live JDBC workflows still require manual testing against a locally configured MySQL instance; no automated test suite is currently included. AI assistance was used for repository preparation and documentation review. All submitted code, configuration decisions, and testing claims should be reviewed by the coursework authors before submission.