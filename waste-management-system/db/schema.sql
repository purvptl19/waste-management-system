--------------------------------------------------------------------
-- Waste Management System - Oracle Database Schema
-- Run this whole script as the schema/user the app will connect as
-- (e.g. WMS_USER), after creating that user with CREATE SESSION,
-- CREATE TABLE, CREATE SEQUENCE privileges.
--------------------------------------------------------------------

-- Drop old objects if re-running (ignore errors on first run)
-- DROP TABLE complaints CASCADE CONSTRAINTS;
-- DROP TABLE waste_requests CASCADE CONSTRAINTS;
-- DROP TABLE vehicles CASCADE CONSTRAINTS;
-- DROP TABLE users CASCADE CONSTRAINTS;

--------------------------------------------------------------------
-- USERS  (Admin / Staff / Citizen)
--------------------------------------------------------------------
CREATE TABLE users (
    user_id     NUMBER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    full_name   VARCHAR2(100)  NOT NULL,
    username    VARCHAR2(50)   NOT NULL UNIQUE,
    password    VARCHAR2(200)  NOT NULL,      -- stores SHA-256 hash
    email       VARCHAR2(100),
    phone       VARCHAR2(20),
    address     VARCHAR2(200),
    role        VARCHAR2(20)   DEFAULT 'CITIZEN' NOT NULL,
    created_at  DATE           DEFAULT SYSDATE,
    CONSTRAINT chk_user_role CHECK (role IN ('ADMIN','STAFF','CITIZEN'))
);

--------------------------------------------------------------------
-- VEHICLES  (collection trucks, assigned to staff)
--------------------------------------------------------------------
CREATE TABLE vehicles (
    vehicle_id      NUMBER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    vehicle_no      VARCHAR2(30)  NOT NULL UNIQUE,
    vehicle_type    VARCHAR2(50),
    capacity        VARCHAR2(50),
    driver_staff_id NUMBER REFERENCES users(user_id),
    status          VARCHAR2(20) DEFAULT 'AVAILABLE',
    CONSTRAINT chk_vehicle_status CHECK (status IN ('AVAILABLE','ON_ROUTE','MAINTENANCE'))
);

--------------------------------------------------------------------
-- WASTE PICKUP REQUESTS
--------------------------------------------------------------------
CREATE TABLE waste_requests (
    request_id        NUMBER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    user_id            NUMBER NOT NULL REFERENCES users(user_id),
    waste_type         VARCHAR2(50)  NOT NULL,
    quantity           VARCHAR2(50),
    address             VARCHAR2(200) NOT NULL,
    pickup_date        DATE,
    status              VARCHAR2(20) DEFAULT 'PENDING' NOT NULL,
    assigned_staff_id  NUMBER REFERENCES users(user_id),
    remarks             VARCHAR2(300),
    created_at         DATE DEFAULT SYSDATE,
    CONSTRAINT chk_req_status CHECK (status IN ('PENDING','SCHEDULED','COLLECTED','CANCELLED'))
);

--------------------------------------------------------------------
-- COMPLAINTS
--------------------------------------------------------------------
CREATE TABLE complaints (
    complaint_id  NUMBER GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    user_id       NUMBER NOT NULL REFERENCES users(user_id),
    subject       VARCHAR2(150) NOT NULL,
    description   VARCHAR2(500),
    status        VARCHAR2(20) DEFAULT 'OPEN' NOT NULL,
    created_at    DATE DEFAULT SYSDATE,
    resolved_at   DATE,
    CONSTRAINT chk_complaint_status CHECK (status IN ('OPEN','IN_PROGRESS','RESOLVED'))
);

--------------------------------------------------------------------
-- Seed data
-- Default admin login -> username: admin  password: admin123
-- (password stored as SHA-256 hash of "admin123")
--------------------------------------------------------------------
INSERT INTO users (full_name, username, password, email, role)
VALUES ('System Administrator', 'admin',
        '240be518fabd2724ddb6f04eeb1da5967448d7e831c08c8fa822809f74c720a9',
        '[email protected]', 'ADMIN');

INSERT INTO users (full_name, username, password, email, role)
VALUES ('Staff Member', 'staff1',
        '240be518fabd2724ddb6f04eeb1da5967448d7e831c08c8fa822809f74c720a9',
        '[email protected]', 'STAFF');

INSERT INTO users (full_name, username, password, email, role)
VALUES ('John Citizen', 'citizen1',
        '240be518fabd2724ddb6f04eeb1da5967448d7e831c08c8fa822809f74c720a9',
        '[email protected]', 'CITIZEN');

INSERT INTO vehicles (vehicle_no, vehicle_type, capacity, status)
VALUES ('TRK-101', 'Compactor Truck', '5 Tons', 'AVAILABLE');

INSERT INTO vehicles (vehicle_no, vehicle_type, capacity, status)
VALUES ('TRK-102', 'Open Truck', '3 Tons', 'AVAILABLE');

COMMIT;
