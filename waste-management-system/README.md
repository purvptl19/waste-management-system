# Waste Management System (Java + Oracle DB + Swing GUI)

A desktop waste-management application with role-based access (Admin / Staff / Citizen),
Oracle database connectivity via JDBC, and a modern-styled Swing GUI (green/teal theme,
rounded cards & buttons, sidebar navigation). FlatLaf is used for a polished look and
falls back gracefully to Nimbus if it isn't on the classpath.

## Features

- **Login / Registration** — citizens can self-register; admin/staff accounts are seeded.
- **Role-based dashboard**
  - **Citizen**: create pickup requests, view/cancel own requests, file & track complaints.
  - **Staff**: view all pickup requests, update status & remarks, view/update complaints.
  - **Admin**: everything Staff can do, plus manage users (add/edit/delete, assign roles)
    and view reports.
- **Waste pickup requests**: type, quantity, address, pickup date, status
  (PENDING → SCHEDULED → COLLECTED / CANCELLED), staff assignment, remarks.
- **Complaints**: subject, description, status (OPEN → IN_PROGRESS → RESOLVED).
- **Reports**: simple built-in bar charts (no external charting library needed) showing
  request/complaint counts by status.
- **Passwords** are stored as SHA-256 hashes, never plain text.

## Project structure

```
waste-management-system/
├── pom.xml                     Maven build (Oracle JDBC + FlatLaf deps, shade plugin)
├── db/schema.sql                Oracle DDL + seed data
├── src/main/resources/
│   └── db.properties            DB connection settings (EDIT THIS)
└── src/main/java/com/wms/
    ├── Main.java                 Entry point
    ├── db/DBConnection.java      JDBC connection manager
    ├── model/                    User, WasteRequest, Complaint
    ├── dao/                      UserDAO, WasteRequestDAO, ComplaintDAO
    ├── util/                     Theme, RoundedButton/Panel, PasswordUtil, Session
    └── ui/
        ├── LoginFrame.java
        ├── RegisterDialog.java
        ├── DashboardFrame.java   Sidebar + card-layout content
        └── panels/                Home, WasteRequestPanel, ComplaintPanel,
                                     UserManagementPanel, ReportsPanel
```

## Setup

### 1. Create the Oracle schema

Connect to your Oracle database (XE, on-prem, or Autonomous DB) as the user the app
will log in with, and run:

```sql
@db/schema.sql
```

This creates `users`, `waste_requests`, `complaints`, `vehicles`, and seeds:

| Username | Password  | Role    |
|----------|-----------|---------|
| admin    | admin123  | ADMIN   |
| staff1   | admin123  | STAFF   |
| citizen1 | admin123  | CITIZEN |

### 2. Configure the connection

Edit `src/main/resources/db.properties`:

```properties
db.url=jdbc:oracle:thin:@localhost:1521:xe
db.user=wms_user
db.password=wms_pass
```

Adjust the URL for your setup, e.g. for a Service Name:
`jdbc:oracle:thin:@//localhost:1521/XEPDB1`, or for Autonomous DB use the wallet-based
connection string from your `tnsnames.ora`.

### 3. Build & run

Requires JDK 17+ and Maven.

```bash
mvn clean package
java -jar target/waste-management-system.jar
```

Maven will download the Oracle JDBC driver (`ojdbc11`) and FlatLaf automatically from
Maven Central — no manual driver installation needed.

### Running from an IDE

Import as a Maven project (IntelliJ IDEA / Eclipse / VS Code), let it resolve
dependencies, then run `com.wms.Main`.

## Notes / next steps you may want to customize

- The `vehicles` table is included in the schema for future expansion (e.g. assigning
  trucks to pickups) but doesn't yet have a dedicated UI panel — easy to add following
  the same DAO/panel pattern as `WasteRequestPanel`.
- `RegisterDialog` only creates CITIZEN accounts by design; STAFF/ADMIN accounts are
  created via the admin's "Manage Users" panel.
- All SQL uses `PreparedStatement` with bind variables to prevent SQL injection.
- Swap the eco-green palette in `util/UITheme.java` to restyle the whole app in one place.
