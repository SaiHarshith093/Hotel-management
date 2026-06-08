-- =============================================================================
-- Hotel Management System - MySQL Schema
-- Database : hotel_db
-- Engine   : InnoDB | Charset : utf8mb4
-- MySQL    : 8.0+
-- =============================================================================

CREATE DATABASE IF NOT EXISTS hotel_db
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

USE hotel_db;

-- Drop tables in reverse dependency order (safe re-run)
SET FOREIGN_KEY_CHECKS = 0;
DROP TABLE IF EXISTS bills;
DROP TABLE IF EXISTS food_orders;
DROP TABLE IF EXISTS food_items;
DROP TABLE IF EXISTS bookings;
DROP TABLE IF EXISTS customers;
DROP TABLE IF EXISTS rooms;
DROP TABLE IF EXISTS users;
SET FOREIGN_KEY_CHECKS = 1;

-- =============================================================================
-- 1. USERS
-- =============================================================================
CREATE TABLE users (
    id              BIGINT          NOT NULL AUTO_INCREMENT,
    username        VARCHAR(50)     NOT NULL,
    password        VARCHAR(255)    NOT NULL,
    email           VARCHAR(100)    NOT NULL,
    full_name       VARCHAR(100)    NOT NULL,
    role            ENUM('ADMIN', 'MANAGER', 'RECEPTIONIST') NOT NULL DEFAULT 'RECEPTIONIST',
    enabled         TINYINT(1)      NOT NULL DEFAULT 1,
    created_at      TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    PRIMARY KEY (id),
    CONSTRAINT uq_users_username UNIQUE (username),
    CONSTRAINT uq_users_email UNIQUE (email),
    CONSTRAINT chk_users_enabled CHECK (enabled IN (0, 1))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE INDEX idx_users_role ON users (role);
CREATE INDEX idx_users_enabled ON users (enabled);

-- =============================================================================
-- 2. ROOMS
-- =============================================================================
CREATE TABLE rooms (
    id              BIGINT          NOT NULL AUTO_INCREMENT,
    room_number     VARCHAR(10)     NOT NULL,
    room_type       ENUM(
                        'Luxury Double',
                        'Deluxe Double',
                        'Luxury Single',
                        'Deluxe Single'
                    )               NOT NULL,
    price_per_night DECIMAL(10, 2)  NOT NULL,
    floor_number    INT             NOT NULL,
    max_occupancy   INT             NOT NULL,
    status          ENUM('AVAILABLE', 'OCCUPIED', 'MAINTENANCE', 'RESERVED') NOT NULL DEFAULT 'AVAILABLE',
    description     TEXT            NULL,
    created_at      TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    PRIMARY KEY (id),
    CONSTRAINT uq_rooms_room_number UNIQUE (room_number),
    CONSTRAINT chk_rooms_price CHECK (price_per_night > 0),
    CONSTRAINT chk_rooms_floor CHECK (floor_number >= 0),
    CONSTRAINT chk_rooms_occupancy CHECK (max_occupancy > 0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE INDEX idx_rooms_type ON rooms (room_type);
CREATE INDEX idx_rooms_status ON rooms (status);
CREATE INDEX idx_rooms_type_status ON rooms (room_type, status);

-- =============================================================================
-- 3. CUSTOMERS
-- =============================================================================
CREATE TABLE customers (
    id              BIGINT          NOT NULL AUTO_INCREMENT,
    name            VARCHAR(100)    NOT NULL,
    phone           VARCHAR(20)     NOT NULL,
    gender          ENUM('MALE', 'FEMALE', 'OTHER') NOT NULL,
    email           VARCHAR(100)    NULL,
    address         TEXT            NULL,
    created_at      TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    PRIMARY KEY (id),
    CONSTRAINT chk_customers_email_format CHECK (
        email IS NULL OR email REGEXP '^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$'
    )
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE INDEX idx_customers_phone ON customers (phone);
CREATE INDEX idx_customers_email ON customers (email);
CREATE INDEX idx_customers_name ON customers (name);
CREATE INDEX idx_customers_gender ON customers (gender);

-- =============================================================================
-- 4. BOOKINGS
-- =============================================================================
CREATE TABLE bookings (
    id              BIGINT          NOT NULL AUTO_INCREMENT,
    customer_id     BIGINT          NOT NULL,
    room_id         BIGINT          NOT NULL,
    check_in_date   DATE            NOT NULL,
    check_out_date  DATE            NOT NULL,
    adults          INT             NOT NULL DEFAULT 1,
    children        INT             NOT NULL DEFAULT 0,
    status          ENUM('PENDING', 'CONFIRMED', 'CHECKED_IN', 'CHECKED_OUT', 'COMPLETED', 'CANCELLED') NOT NULL DEFAULT 'PENDING',
    special_requests TEXT           NULL,
    created_by      BIGINT          NULL,
    created_at      TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    PRIMARY KEY (id),
    CONSTRAINT fk_bookings_customer
        FOREIGN KEY (customer_id) REFERENCES customers (id)
        ON UPDATE CASCADE ON DELETE RESTRICT,
    CONSTRAINT fk_bookings_room
        FOREIGN KEY (room_id) REFERENCES rooms (id)
        ON UPDATE CASCADE ON DELETE RESTRICT,
    CONSTRAINT fk_bookings_created_by
        FOREIGN KEY (created_by) REFERENCES users (id)
        ON UPDATE CASCADE ON DELETE SET NULL,
    CONSTRAINT chk_bookings_dates CHECK (check_out_date > check_in_date),
    CONSTRAINT chk_bookings_adults CHECK (adults >= 1),
    CONSTRAINT chk_bookings_children CHECK (children >= 0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE INDEX idx_bookings_customer ON bookings (customer_id);
CREATE INDEX idx_bookings_room ON bookings (room_id);
CREATE INDEX idx_bookings_status ON bookings (status);
CREATE INDEX idx_bookings_check_in ON bookings (check_in_date);
CREATE INDEX idx_bookings_check_out ON bookings (check_out_date);
CREATE INDEX idx_bookings_date_range ON bookings (check_in_date, check_out_date);
CREATE INDEX idx_bookings_room_dates ON bookings (room_id, check_in_date, check_out_date);

-- =============================================================================
-- 5. FOOD ITEMS
-- =============================================================================
CREATE TABLE food_items (
    id              BIGINT          NOT NULL AUTO_INCREMENT,
    name            VARCHAR(100)    NOT NULL,
    category        ENUM('BREAKFAST', 'LUNCH', 'DINNER', 'SNACKS', 'BEVERAGES') NOT NULL,
    price           DECIMAL(10, 2)  NOT NULL,
    is_available    TINYINT(1)      NOT NULL DEFAULT 1,
    description     TEXT            NULL,
    created_at      TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    PRIMARY KEY (id),
    CONSTRAINT uq_food_items_name UNIQUE (name),
    CONSTRAINT chk_food_items_price CHECK (price >= 0),
    CONSTRAINT chk_food_items_available CHECK (is_available IN (0, 1))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE INDEX idx_food_items_category ON food_items (category);
CREATE INDEX idx_food_items_available ON food_items (is_available);
CREATE INDEX idx_food_items_category_available ON food_items (category, is_available);

-- =============================================================================
-- 6. FOOD ORDERS
-- =============================================================================
CREATE TABLE food_orders (
    id              BIGINT          NOT NULL AUTO_INCREMENT,
    booking_id      BIGINT          NOT NULL,
    food_item_id    BIGINT          NOT NULL,
    quantity        INT             NOT NULL DEFAULT 1,
    unit_price      DECIMAL(10, 2)  NOT NULL,
    total_price     DECIMAL(10, 2)  NOT NULL,
    order_status    ENUM('PLACED', 'PREPARING', 'DELIVERED', 'CANCELLED') NOT NULL DEFAULT 'PLACED',
    notes           VARCHAR(255)    NULL,
    ordered_at      TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    PRIMARY KEY (id),
    CONSTRAINT fk_food_orders_booking
        FOREIGN KEY (booking_id) REFERENCES bookings (id)
        ON UPDATE CASCADE ON DELETE CASCADE,
    CONSTRAINT fk_food_orders_food_item
        FOREIGN KEY (food_item_id) REFERENCES food_items (id)
        ON UPDATE CASCADE ON DELETE RESTRICT,
    CONSTRAINT chk_food_orders_quantity CHECK (quantity > 0),
    CONSTRAINT chk_food_orders_unit_price CHECK (unit_price >= 0),
    CONSTRAINT chk_food_orders_total_price CHECK (total_price >= 0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE INDEX idx_food_orders_booking ON food_orders (booking_id);
CREATE INDEX idx_food_orders_food_item ON food_orders (food_item_id);
CREATE INDEX idx_food_orders_status ON food_orders (order_status);
CREATE INDEX idx_food_orders_ordered_at ON food_orders (ordered_at);

-- =============================================================================
-- 7. BILLS
-- =============================================================================
CREATE TABLE bills (
    id              BIGINT          NOT NULL AUTO_INCREMENT,
    booking_id      BIGINT          NOT NULL,
    room_charges    DECIMAL(10, 2)  NOT NULL DEFAULT 0.00,
    food_charges    DECIMAL(10, 2)  NOT NULL DEFAULT 0.00,
    tax_amount      DECIMAL(10, 2)  NOT NULL DEFAULT 0.00,
    discount_amount DECIMAL(10, 2)  NOT NULL DEFAULT 0.00,
    total_amount    DECIMAL(10, 2)  NOT NULL,
    payment_status  ENUM('PENDING', 'PARTIAL', 'PAID', 'REFUNDED') NOT NULL DEFAULT 'PENDING',
    payment_method  ENUM('CASH', 'CARD', 'UPI', 'ONLINE') NULL,
    bill_date       TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    paid_at         TIMESTAMP       NULL,
    notes           TEXT            NULL,

    PRIMARY KEY (id),
    CONSTRAINT uq_bills_booking UNIQUE (booking_id),
    CONSTRAINT fk_bills_booking
        FOREIGN KEY (booking_id) REFERENCES bookings (id)
        ON UPDATE CASCADE ON DELETE RESTRICT,
    CONSTRAINT chk_bills_room_charges CHECK (room_charges >= 0),
    CONSTRAINT chk_bills_food_charges CHECK (food_charges >= 0),
    CONSTRAINT chk_bills_tax CHECK (tax_amount >= 0),
    CONSTRAINT chk_bills_discount CHECK (discount_amount >= 0),
    CONSTRAINT chk_bills_total CHECK (total_amount >= 0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE INDEX idx_bills_payment_status ON bills (payment_status);
CREATE INDEX idx_bills_bill_date ON bills (bill_date);
CREATE INDEX idx_bills_paid_at ON bills (paid_at);

-- =============================================================================
-- SAMPLE DATA
-- Password for all users: admin123 (BCrypt encoded)
-- =============================================================================

INSERT INTO users (username, password, email, full_name, role, enabled) VALUES
('admin',       '$2b$10$IIcMssWLV0TGb6qsTA/IVOH2ZM1SHSo9v/osC9D5rjE/wskI6dSNm', 'admin@hotel.com',       'System Administrator', 'ADMIN',        1),
('manager',     '$2b$10$IIcMssWLV0TGb6qsTA/IVOH2ZM1SHSo9v/osC9D5rjE/wskI6dSNm', 'manager@hotel.com',     'Rajesh Kumar',         'MANAGER',      1),
('reception1',  '$2b$10$IIcMssWLV0TGb6qsTA/IVOH2ZM1SHSo9v/osC9D5rjE/wskI6dSNm', 'reception1@hotel.com',  'Priya Sharma',         'RECEPTIONIST', 1),
('reception2',  '$2b$10$IIcMssWLV0TGb6qsTA/IVOH2ZM1SHSo9v/osC9D5rjE/wskI6dSNm', 'reception2@hotel.com',  'Anil Verma',           'RECEPTIONIST', 1);

INSERT INTO rooms (room_number, room_type, price_per_night, floor_number, max_occupancy, status, description) VALUES
('101', 'Luxury Double',  8500.00, 1, 2, 'AVAILABLE',   'Spacious luxury room with king bed, city view, and premium amenities.'),
('102', 'Luxury Double',  8500.00, 1, 2, 'OCCUPIED',    'Spacious luxury room with king bed, city view, and premium amenities.'),
('103', 'Deluxe Double',  5500.00, 1, 2, 'AVAILABLE',   'Comfortable deluxe room with queen bed and modern furnishings.'),
('104', 'Deluxe Double',  5500.00, 1, 2, 'RESERVED',    'Comfortable deluxe room with queen bed and modern furnishings.'),
('105', 'Luxury Single',  6500.00, 1, 1, 'AVAILABLE',   'Premium single occupancy room with luxury bedding and workspace.'),
('106', 'Luxury Single',  6500.00, 1, 1, 'MAINTENANCE', 'Premium single occupancy room with luxury bedding and workspace.'),
('201', 'Luxury Double',  9000.00, 2, 2, 'AVAILABLE',   'Top-floor luxury double with panoramic view and lounge access.'),
('202', 'Deluxe Double',  5800.00, 2, 2, 'AVAILABLE',   'Deluxe double room on upper floor with enhanced lighting.'),
('203', 'Deluxe Single',  4200.00, 2, 1, 'AVAILABLE',   'Cozy deluxe single ideal for business travelers.'),
('204', 'Deluxe Single',  4200.00, 2, 1, 'OCCUPIED',    'Cozy deluxe single ideal for business travelers.'),
('301', 'Luxury Single',  7200.00, 3, 1, 'AVAILABLE',   'Executive luxury single with premium bathroom and minibar.'),
('302', 'Deluxe Single',  4500.00, 3, 1, 'AVAILABLE',   'Standard deluxe single with essential amenities.');

INSERT INTO customers (name, phone, gender, email, address) VALUES
('Amit Patel',     '+91-9876543210', 'MALE',   'amit.patel@email.com',     '12 MG Road, Ahmedabad'),
('Sneha Reddy',    '+91-9876543211', 'FEMALE', 'sneha.reddy@email.com',    '45 Banjara Hills, Hyderabad'),
('Vikram Singh',   '+91-9876543212', 'MALE',   'vikram.singh@email.com',   '78 Civil Lines, Jaipur'),
('Meera Nair',     '+91-9876543213', 'FEMALE', 'meera.nair@email.com',     '23 Marine Drive, Kochi'),
('Rahul Gupta',    '+91-9876543214', 'MALE',   'rahul.gupta@email.com',    '56 Park Street, Kolkata'),
('Divya Iyer',     '+91-9876543215', 'FEMALE', 'divya.iyer@email.com',     '89 Anna Salai, Chennai'),
('Karan Malhotra', '+91-9876543216', 'MALE',   'karan.malhotra@email.com', '34 Connaught Place, New Delhi');

INSERT INTO bookings (customer_id, room_id, check_in_date, check_out_date, adults, children, status, special_requests, created_by) VALUES
(1, 2,  '2026-06-01', '2026-06-05', 2, 0, 'CHECKED_IN',  'Late check-in requested',        3),
(2, 4,  '2026-06-10', '2026-06-12', 2, 1, 'CONFIRMED',   'Extra bed for child',            3),
(3, 10, '2026-06-03', '2026-06-07', 1, 0, 'CHECKED_IN',  'Quiet room preferred',           4),
(4, 1,  '2026-06-15', '2026-06-18', 2, 0, 'PENDING',     'Anniversary decoration',         3),
(5, 7,  '2026-05-28', '2026-05-30', 2, 0, 'CHECKED_OUT', NULL,                             4),
(6, 3,  '2026-06-20', '2026-06-22', 1, 0, 'CONFIRMED',   'Early breakfast at 7 AM',      3),
(7, 8,  '2026-06-08', '2026-06-09', 2, 0, 'CANCELLED',   'Travel plans changed',           4);

INSERT INTO food_items (name, category, price, is_available, description) VALUES
('Sandwich', 'SNACKS',    50.00, 1, 'Classic veg sandwich'),
('Pasta',    'DINNER',   60.00, 1, 'Creamy pasta'),
('Noodles',  'DINNER',   70.00, 1, 'Stir-fried noodles'),
('Coke',     'BEVERAGES', 30.00, 1, 'Soft drink');

INSERT INTO food_orders (booking_id, food_item_id, quantity, unit_price, total_price, order_status, notes) VALUES
(1, 1, 2, 50.00, 100.00, 'DELIVERED', 'Room service'),
(1, 4, 2, 30.00,  60.00, 'DELIVERED', NULL),
(3, 2, 1, 60.00,  60.00, 'PREPARING', NULL),
(3, 3, 1, 70.00,  70.00, 'PLACED',    'Less spicy');

INSERT INTO bills (booking_id, room_charges, food_charges, tax_amount, discount_amount, total_amount, payment_status, payment_method, paid_at, notes) VALUES
(5, 18000.00, 1980.00, 3596.40, 0.00, 23576.40, 'PAID',    'CARD',  '2026-05-30 11:30:00', 'Checkout bill - 2 nights luxury double'),
(1, 34000.00, 1660.00, 6419.40, 500.00, 41579.40, 'PARTIAL', 'UPI',   NULL,                  'Partial payment received; balance pending'),
(3, 26000.00,  930.00, 4841.40, 0.00, 31771.40, 'PENDING', NULL,    NULL,                  'Bill generated at checkout pending');

-- =============================================================================
-- END OF SCHEMA
-- =============================================================================
