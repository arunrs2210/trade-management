-- ============================================================
-- SHNOOR International LLC - Trade Management System
-- Database Schema (PostgreSQL)
-- ============================================================

-- Drop tables if they exist (for clean setup)
DROP TABLE IF EXISTS shipment_items CASCADE;
DROP TABLE IF EXISTS shipments CASCADE;
DROP TABLE IF EXISTS products CASCADE;
DROP TABLE IF EXISTS suppliers CASCADE;
DROP TABLE IF EXISTS customers CASCADE;
DROP TABLE IF EXISTS users CASCADE;

-- ============================================================
-- USERS TABLE
-- ============================================================
CREATE TABLE users (
    user_id     SERIAL PRIMARY KEY,
    username    VARCHAR(50)  NOT NULL UNIQUE,
    password    VARCHAR(255) NOT NULL,
    role        VARCHAR(20)  NOT NULL CHECK (role IN ('ADMIN', 'MANAGER', 'STAFF')),
    email       VARCHAR(100) NOT NULL UNIQUE,
    created_at  TIMESTAMP    DEFAULT CURRENT_TIMESTAMP
);

-- ============================================================
-- CUSTOMERS TABLE
-- ============================================================
CREATE TABLE customers (
    customer_id   SERIAL PRIMARY KEY,
    name          VARCHAR(100) NOT NULL,
    email         VARCHAR(100) NOT NULL UNIQUE,
    phone         VARCHAR(20),
    country       VARCHAR(60)  NOT NULL,
    address       TEXT,
    created_at    TIMESTAMP    DEFAULT CURRENT_TIMESTAMP
);

-- ============================================================
-- SUPPLIERS TABLE
-- ============================================================
CREATE TABLE suppliers (
    supplier_id   SERIAL PRIMARY KEY,
    name          VARCHAR(100) NOT NULL,
    email         VARCHAR(100) NOT NULL UNIQUE,
    phone         VARCHAR(20),
    country       VARCHAR(60)  NOT NULL,
    address       TEXT,
    created_at    TIMESTAMP    DEFAULT CURRENT_TIMESTAMP
);

-- ============================================================
-- PRODUCTS TABLE
-- ============================================================
CREATE TABLE products (
    product_id    SERIAL PRIMARY KEY,
    product_name  VARCHAR(150) NOT NULL,
    hs_code       VARCHAR(20),          -- Harmonized System code for trade
    unit_price    NUMERIC(12, 2) NOT NULL,
    unit          VARCHAR(30)  NOT NULL, -- kg, piece, litre, etc.
    stock_qty     INTEGER      NOT NULL DEFAULT 0,
    supplier_id   INTEGER      REFERENCES suppliers(supplier_id) ON DELETE SET NULL,
    created_at    TIMESTAMP    DEFAULT CURRENT_TIMESTAMP
);

-- ============================================================
-- SHIPMENTS TABLE
-- ============================================================
CREATE TABLE shipments (
    shipment_id      SERIAL PRIMARY KEY,
    shipment_type    VARCHAR(10) NOT NULL CHECK (shipment_type IN ('IMPORT', 'EXPORT')),
    status           VARCHAR(20) NOT NULL DEFAULT 'PENDING'
                         CHECK (status IN ('PENDING','IN_TRANSIT','CUSTOMS','DELIVERED','CANCELLED')),
    customer_id      INTEGER     REFERENCES customers(customer_id) ON DELETE SET NULL,
    supplier_id      INTEGER     REFERENCES suppliers(supplier_id) ON DELETE SET NULL,
    origin_country   VARCHAR(60) NOT NULL,
    dest_country     VARCHAR(60) NOT NULL,
    shipping_date    DATE,
    expected_arrival DATE,
    actual_arrival   DATE,
    tracking_number  VARCHAR(50) UNIQUE,
    total_value      NUMERIC(14, 2) DEFAULT 0.00,
    created_by       INTEGER     REFERENCES users(user_id),
    created_at       TIMESTAMP   DEFAULT CURRENT_TIMESTAMP
);

-- ============================================================
-- SHIPMENT ITEMS TABLE (junction: shipment ↔ products)
-- ============================================================
CREATE TABLE shipment_items (
    item_id       SERIAL PRIMARY KEY,
    shipment_id   INTEGER        NOT NULL REFERENCES shipments(shipment_id) ON DELETE CASCADE,
    product_id    INTEGER        NOT NULL REFERENCES products(product_id)   ON DELETE RESTRICT,
    quantity      INTEGER        NOT NULL CHECK (quantity > 0),
    unit_price    NUMERIC(12, 2) NOT NULL,
    line_total    NUMERIC(14, 2) GENERATED ALWAYS AS (quantity * unit_price) STORED
);

-- ============================================================
-- INDEXES
-- ============================================================
CREATE INDEX idx_shipments_status   ON shipments(status);
CREATE INDEX idx_shipments_type     ON shipments(shipment_type);
CREATE INDEX idx_shipments_customer ON shipments(customer_id);
CREATE INDEX idx_products_supplier  ON products(supplier_id);

-- ============================================================
-- SEED DATA
-- ============================================================
INSERT INTO users (username, password, role, email) VALUES
  ('admin',   'admin123',   'ADMIN',   'admin@shnoor.com'),
  ('manager', 'manager123', 'MANAGER', 'manager@shnoor.com'),
  ('staff1',  'staff123',   'STAFF',   'staff1@shnoor.com');

INSERT INTO customers (name, email, phone, country, address) VALUES
  ('GlobalTech Inc.',   'contact@globaltech.com',  '+1-555-1001', 'USA',    '123 Market St, St. Louis, MO'),
  ('EuroTrade GmbH',    'info@eurotrade.de',        '+49-30-2002', 'Germany','Berliner Str. 45, Berlin'),
  ('AsiaPacific Ltd.',  'hello@asiapac.sg',         '+65-6300-3003','Singapore','1 Raffles Quay, Singapore');

INSERT INTO suppliers (name, email, phone, country, address) VALUES
  ('TechSource Co.',    'sales@techsource.cn',  '+86-21-4001', 'China',   '88 Nanjing Rd, Shanghai'),
  ('AmeriParts LLC',    'orders@ameriparts.com','+1-214-4002', 'USA',     '500 Commerce St, Dallas, TX'),
  ('EuroComponents AG', 'supply@eurocomp.de',   '+49-89-4003', 'Germany', 'Maximilianstr. 12, Munich');

INSERT INTO products (product_name, hs_code, unit_price, unit, stock_qty, supplier_id) VALUES
  ('Industrial Sensor Module',  '8543.70', 149.99, 'piece',  200, 1),
  ('Ethernet Switch 24-Port',   '8517.62', 399.00, 'piece',   80, 1),
  ('Steel Pipe 50mm',           '7304.39',  18.50, 'meter', 1500, 2),
  ('PCB Assembly Board',        '8534.00',  72.00, 'piece',  350, 3),
  ('Hydraulic Pump Unit',       '8413.50', 875.00, 'piece',   40, 2);

INSERT INTO shipments (shipment_type, status, customer_id, supplier_id, origin_country,
                       dest_country, shipping_date, expected_arrival, tracking_number, total_value, created_by)
VALUES
  ('EXPORT','IN_TRANSIT', 1, NULL, 'USA','Germany',  '2026-06-01','2026-06-15','SH-2026-EX-001', 59980.00, 1),
  ('IMPORT','PENDING',    NULL, 1, 'China','USA',    '2026-06-10','2026-06-25','SH-2026-IM-002', 29998.00, 2),
  ('EXPORT','DELIVERED',  3, NULL, 'USA','Singapore','2026-05-20','2026-05-30','SH-2026-EX-003', 14400.00, 1);

INSERT INTO shipment_items (shipment_id, product_id, quantity, unit_price) VALUES
  (1, 2, 100, 399.00),
  (1, 4, 200,  72.00),
  (2, 1, 200, 149.99),
  (3, 4, 200,  72.00);
