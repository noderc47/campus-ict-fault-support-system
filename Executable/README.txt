Campus ICT Fault Reporting and Support System - Executable

Requirements
- Java 17 or later must be installed and available on PATH. The application is compiled for Java 17.
- MySQL Server must be running, and a MySQL account must have access to campus_ict_db.

Database import
1. Before importing, back up any campus_ict_db database whose records must be retained.
2. In MySQL Workbench, choose File > Open SQL Script.
3. Open this database export relative to this Executable folder:
   ..\Database\campus_ict_db.sql
4. Review the script before executing it. This export contains table definitions and demonstration records. It includes DROP TABLE statements, so it replaces the listed tables and their records when run.
5. Execute the script against the intended MySQL server.

Database configuration
1. In this Executable folder, copy:
   config\db.properties.example
   to:
   config\db.properties
2. Edit config\db.properties with the local MySQL URL, username, and password. Do not submit or share db.properties.
3. The local development MySQL server uses port 3307. For that computer, use a URL such as:
   db.url=jdbc:mysql://localhost:3307/campus_ict_db?useSSL=false&serverTimezone=UTC
   On another computer, use that computer's actual MySQL host and port.

Run
- Double-click RunCampusICT.cmd, or open Command Prompt in this folder and run:
  RunCampusICT.cmd

RunCampusICT.cmd changes to this Executable folder before it runs java -jar CampusICT.jar, so the relative configuration path config\db.properties works.