CREATE TABLE IF NOT EXISTS users (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    username    VARCHAR(50)  NOT NULL UNIQUE,
    password    VARCHAR(255) NOT NULL,
    email       VARCHAR(100) NOT NULL UNIQUE,
    full_name   VARCHAR(100) NOT NULL,
    role        VARCHAR(20)  NOT NULL,
    enabled     BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at  TIMESTAMP    DEFAULT CURRENT_TIMESTAMP,
    updated_at  TIMESTAMP    DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS rooms (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    room_number     VARCHAR(10)  NOT NULL UNIQUE,
    room_type       VARCHAR(30)  NOT NULL,
    price_per_night DECIMAL(10,2) NOT NULL,
    floor_number    INT          NOT NULL DEFAULT 1,
    max_occupancy   INT          NOT NULL DEFAULT 1,
    status          VARCHAR(20)  NOT NULL DEFAULT 'AVAILABLE',
    description     TEXT,
    created_at      TIMESTAMP    DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP    DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS customers (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    name        VARCHAR(100) NOT NULL,
    phone       VARCHAR(20)  NOT NULL,
    gender      VARCHAR(10)  NOT NULL,
    email       VARCHAR(100),
    address     TEXT,
    created_at  TIMESTAMP    DEFAULT CURRENT_TIMESTAMP,
    updated_at  TIMESTAMP    DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS bookings (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    customer_id     BIGINT       NOT NULL,
    room_id         BIGINT       NOT NULL,
    check_in_date   DATE         NOT NULL,
    check_out_date  DATE         NOT NULL,
    adults          INT          NOT NULL DEFAULT 1,
    children        INT          NOT NULL DEFAULT 0,
    status          VARCHAR(20)  NOT NULL DEFAULT 'CONFIRMED',
    special_requests TEXT,
    created_by      BIGINT,
    created_at      TIMESTAMP    DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP    DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS food_items (
    id           BIGINT AUTO_INCREMENT PRIMARY KEY,
    name         VARCHAR(100) NOT NULL UNIQUE,
    category     VARCHAR(20)  NOT NULL,
    price        DECIMAL(10,2) NOT NULL,
    is_available BOOLEAN      NOT NULL DEFAULT TRUE,
    description  TEXT,
    created_at   TIMESTAMP    DEFAULT CURRENT_TIMESTAMP,
    updated_at   TIMESTAMP    DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS food_orders (
    id           BIGINT AUTO_INCREMENT PRIMARY KEY,
    booking_id   BIGINT       NOT NULL,
    food_item_id BIGINT       NOT NULL,
    quantity     INT          NOT NULL DEFAULT 1,
    unit_price   DECIMAL(10,2) NOT NULL,
    total_price  DECIMAL(10,2) NOT NULL,
    order_status VARCHAR(20)  NOT NULL DEFAULT 'PLACED',
    notes        VARCHAR(255),
    ordered_at   TIMESTAMP    DEFAULT CURRENT_TIMESTAMP,
    updated_at   TIMESTAMP    DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS bills (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    booking_id      BIGINT       NOT NULL UNIQUE,
    room_charges    DECIMAL(10,2) NOT NULL DEFAULT 0.00,
    food_charges    DECIMAL(10,2) NOT NULL DEFAULT 0.00,
    tax_amount      DECIMAL(10,2) NOT NULL DEFAULT 0.00,
    discount_amount DECIMAL(10,2) NOT NULL DEFAULT 0.00,
    total_amount    DECIMAL(10,2) NOT NULL,
    payment_status  VARCHAR(20)  NOT NULL DEFAULT 'PENDING',
    payment_method  VARCHAR(20),
    bill_date       TIMESTAMP    DEFAULT CURRENT_TIMESTAMP,
    paid_at         TIMESTAMP,
    notes           TEXT
);
