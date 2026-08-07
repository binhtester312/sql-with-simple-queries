# nopCommerce Local – Automation Testing Environment

## Cấu trúc project
```
nopcommerce-local/
├── docker-compose.yml          # 4 services: SQL Server, nopCommerce, Selenium Hub, Chrome
├── manage.sh                   # Script quản lý (start/stop/reset/status)
> Local Docker setup for Selenium automation testing on nopCommerce, bypassing rate-limiting on `demo.nopcommerce.com`.

## Stack

| Service | Image | Port |
|---|---|---|
| nopCommerce Web | `nopcommerceteam/nopcommerce:latest` | `8080` |
| SQL Server 2022 | `mcr.microsoft.com/mssql/server:2022-latest` | `1433` |
| Selenium Grid Hub | `selenium/hub:4.21.0` | `4444` |
| Chrome Node | `selenium/node-chrome:4.21.0` | — |

---

## Prerequisites

- **Docker Desktop for Mac (Apple Silicon)** → [Download here](https://www.docker.com/products/docker-desktop/)
- Java 17+, Maven 3.x (for test framework integration)

---

## Quick Start

```bash
# 1. Clone repo
git clone https://github.com/binhtester312/nopcommerce-local.git
cd nopcommerce-local

# 2. Start all services
chmod +x manage.sh
./manage.sh start

# 3. Open browser → complete Installation Wizard
open http://localhost:8080
```

---

## nopCommerce Installation Wizard (first time only)

At `http://localhost:8080`, fill in:

| Field | Value |
|---|---|
| Admin email | `<your-admin-email>` |
| Admin password | `<your-admin-password>` |
| Database type | `SQL Server` |
| Server name | `nop_sqlserver,1433` |
| Database name | `NopCommerceDB` |
| Auth type | SQL Authentication |
| Username | `<your-db-username>` |
| Password | `<your-db-password>` |

Wait ~2–3 min for schema creation (120+ tables).

---

## Endpoints

| Service | URL | Credentials |
|---|---|---|
| nopCommerce | http://localhost:8080 | `<your-admin-email>` / `<your-admin-password>` |
| Selenium Grid UI | http://localhost:4444 | — |
| SQL Server (JDBC) | localhost:1433 | `<your-db-username>` / `<your-db-password>` |

---

## Java Test Framework Integration

**1. Add JDBC dependency to `pom.xml`:**
```xml
<dependency>
    <groupId>com.microsoft.sqlserver</groupId>
    <artifactId>mssql-jdbc</artifactId>
    <version>12.6.0.jre11</version>
    <scope>test</scope>
</dependency>
```

**2. Use RemoteWebDriver with Selenium Grid:**
```java
WebDriver driver = new RemoteWebDriver(
    new URL("http://localhost:4444/wd/hub"),
    new ChromeOptions()
);
driver.get("http://localhost:8080");
```

**3. Copy helper classes** from this repo into your framework:
- `NopCommerceConfig.java` – all connection constants
- `DatabaseHelper.java` – JDBC queries for DB verification

---

## Daily Management

```bash
./manage.sh start    # Start all containers
./manage.sh stop     # Stop (data preserved)
./manage.sh status   # Check container status
./manage.sh seed     # 🌱 Seed sample test data for DB testing
./manage.sh reset    # ⚠️ Wipe all data, fresh start
./manage.sh logs     # Tail all logs
```

---

## Project Structure

```
nopcommerce-local/
├── docker-compose.yml        # 4-service orchestration
├── manage.sh                 # Start/stop/reset/seed helper
├── NopCommerceConfig.java    # URLs & credentials constants
├── DatabaseHelper.java       # JDBC helper for DB verification
├── init-scripts/
│   ├── init-db.sql           # Sample SQL queries for test validation
│   └── seed-test-data.sql    # Seed script for test data
├── src/docs/
│   ├── sql_db_testing_guide.md  # Hướng dẫn thực hành SQL DB Testing cho Fresher
│   └── guide_json_jackson_pojo.md # Hướng dẫn Jackson/POJO
└── volumes/                  # Persistent data (gitignored)
```

---

## Compatibility

Zero impact on existing projects. Docker runs fully isolated from your host Java/Maven/Appium/Node.js tools.
