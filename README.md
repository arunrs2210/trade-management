# SHNOOR Trade Management System 🚢

> **Technology Division – Apprenticeship Project**  
> A full-stack Java + PostgreSQL application modelling Import/Export operations for SHNOOR International LLC.

---

## 📌 Project Overview

This system manages the end-to-end lifecycle of international trade shipments — from product cataloguing and customer management to shipment creation, status tracking, and revenue reporting. It directly mirrors SHNOOR's core business verticals:

| Business Area | Coverage in this project |
|---|---|
| Import & Export Management | Shipment CRUD, type filtering, status workflow |
| Enterprise Solutions | Service layer, DAO pattern, transactional DB ops |
| IT Product Development | OOP design, SQL schema, JDBC integration |
| Risk Management | Stock validation, low-stock alerts, business rules |

---

## 🏗️ Architecture

```
com.shnoor
├── Main.java               ← Entry point / interactive console menu
├── config/
│   └── DBConnection.java   ← Singleton JDBC connection manager
├── model/
│   ├── Customer.java
│   ├── Product.java
│   ├── Shipment.java       ← Composed with ShipmentItem list
│   ├── ShipmentItem.java
│   ├── ShipmentType.java   ← Enum: IMPORT | EXPORT
│   └── ShipmentStatus.java ← Enum: PENDING → IN_TRANSIT → CUSTOMS → DELIVERED
├── dao/
│   ├── GenericDAO.java     ← Generic CRUD interface
│   ├── CustomerDAO.java
│   ├── ProductDAO.java
│   ├── ShipmentDAO.java    ← Transactions, JOINs, aggregates
│   └── ShipmentItemDAO.java
├── service/
│   └── ShipmentService.java ← Business logic & validation
└── util/
    └── TablePrinter.java   ← Console table formatter
```

---

## 🗃️ Database Schema (PostgreSQL)

```
users ──────────────────────────────────────────────────────┐
customers ──────┐                                           │
                │                                           │
                ▼                                           ▼
suppliers ──► shipments ◄──────────────────────────── created_by
                │
                ▼
         shipment_items ◄──── products ◄──── suppliers
```

**Tables:** `users`, `customers`, `suppliers`, `products`, `shipments`, `shipment_items`

Key SQL features demonstrated:
- `SERIAL` primary keys & `FOREIGN KEY` constraints with `ON DELETE CASCADE / SET NULL`
- `CHECK` constraints for enum-like columns
- `GENERATED ALWAYS AS` computed column (`line_total`)
- Aggregate queries with `GROUP BY` and `SUM`
- Indexes on frequently filtered columns

---

## 🔑 Key Java Concepts Demonstrated

| Concept | Where |
|---|---|
| OOP / Encapsulation | All model classes |
| Interfaces | `GenericDAO<T, ID>` |
| Enums | `ShipmentType`, `ShipmentStatus` |
| Generics | `GenericDAO`, `Optional<T>`, `List<T>` |
| Design Patterns | Singleton (`DBConnection`), DAO pattern, Service layer |
| JDBC | `PreparedStatement`, `ResultSet`, transactions |
| Switch Expressions (Java 17) | `ShipmentService.advanceStatus()` |
| Text Blocks (Java 17) | SQL strings in DAO classes |
| Exception Handling | Try-with-resources throughout |

---

## 🚀 Getting Started

### Prerequisites

- Java 17+
- PostgreSQL 14+
- Maven 3.8+

### 1. Set Up the Database

```bash
# Connect to PostgreSQL
psql -U postgres

# Create the database
CREATE DATABASE shnoor_trade;
\c shnoor_trade

# Run the schema (creates tables + seed data)
\i sql/schema.sql
```

### 2. Configure Connection

Edit `src/main/resources/db.properties`:

```properties
db.url=jdbc:postgresql://localhost:5432/shnoor_trade
db.username=postgres
db.password=your_password_here
```

### 3. Build

```bash
mvn clean package
```

### 4. Run

```bash
java -jar target/shnoor-trade-management.jar
```

You'll see an interactive console menu:

```
╔══════════════════════════════════════════════════════════╗
║   SHNOOR International LLC                               ║
║   Trade Management System  v1.0                          ║
╚══════════════════════════════════════════════════════════╝

┌─────────────────────────────────────┐
│  MAIN MENU                          │
│  1. List all shipments              │
│  2. Create sample export shipment   │
│  3. Advance shipment status         │
│  4. List customers                  │
│  5. List products / inventory       │
│  6. Low-stock alerts                │
│  7. Revenue summary (aggregate)     │
│  8. Filter shipments by type        │
│  0. Exit                            │
└─────────────────────────────────────┘
```

---

## 📊 Sample Queries

Run directly in `psql` to explore the data:

```sql
-- All active shipments with customer name
SELECT s.shipment_id, s.shipment_type, s.status,
       c.name AS customer, s.origin_country, s.dest_country, s.total_value
  FROM shipments s
  LEFT JOIN customers c ON s.customer_id = c.customer_id
 ORDER BY s.created_at DESC;

-- Revenue by shipment type
SELECT shipment_type,
       COUNT(*)         AS count,
       SUM(total_value) AS total_revenue
  FROM shipments
 WHERE status != 'CANCELLED'
 GROUP BY shipment_type;

-- Low stock products
SELECT product_name, stock_qty, unit
  FROM products
 WHERE stock_qty < 100
 ORDER BY stock_qty;
```

---

## 📁 Project Structure

```
shnoor-trade-management/
├── pom.xml
├── README.md
├── sql/
│   └── schema.sql
└── src/
    └── main/
        ├── java/com/shnoor/
        │   ├── Main.java
        │   ├── config/DBConnection.java
        │   ├── model/  (6 files)
        │   ├── dao/    (5 files)
        │   ├── service/ShipmentService.java
        │   └── util/TablePrinter.java
        └── resources/
            └── db.properties
```

---

## 👤 Author

Built as part of the **SHNOOR International LLC Technology Apprenticeship Program**  
Stack: Java 17 · PostgreSQL · JDBC · Maven

---

*"Join us. Learn. Innovate. Grow."* — SHNOOR International LLC
