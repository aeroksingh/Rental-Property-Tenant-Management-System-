-- V1: baseline schema for the rental management system.
-- Column names follow Hibernate's default (snake_case) naming strategy so
-- spring.jpa.hibernate.ddl-auto=validate passes without any manual mapping.
-- Written to be compatible with both H2 (dev) and MySQL (prod).

CREATE TABLE owners (
    id       BIGINT       AUTO_INCREMENT PRIMARY KEY,
    name     VARCHAR(255) NOT NULL,
    email    VARCHAR(255) NOT NULL,
    password VARCHAR(255) NOT NULL,
    phone    VARCHAR(50),
    role     VARCHAR(20)  NOT NULL DEFAULT 'OWNER',
    CONSTRAINT uk_owners_email UNIQUE (email)
);

CREATE TABLE properties (
    id          BIGINT         AUTO_INCREMENT PRIMARY KEY,
    address     VARCHAR(255)   NOT NULL,
    city        VARCHAR(255)   NOT NULL,
    type        VARCHAR(20)    NOT NULL,
    rent_amount DECIMAL(12, 2) NOT NULL,
    is_occupied BOOLEAN        NOT NULL DEFAULT FALSE,
    owner_id    BIGINT         NOT NULL,
    CONSTRAINT fk_properties_owner FOREIGN KEY (owner_id) REFERENCES owners (id)
);

CREATE TABLE tenants (
    id                BIGINT       AUTO_INCREMENT PRIMARY KEY,
    name              VARCHAR(255) NOT NULL,
    email             VARCHAR(255) NOT NULL,
    password          VARCHAR(255) NOT NULL,
    phone             VARCHAR(50),
    property_id       BIGINT,
    lease_start_date  DATE,
    lease_end_date    DATE,
    role              VARCHAR(20)  NOT NULL DEFAULT 'TENANT',
    CONSTRAINT uk_tenants_email UNIQUE (email),
    CONSTRAINT fk_tenants_property FOREIGN KEY (property_id) REFERENCES properties (id)
);

CREATE TABLE rent_payments (
    id          BIGINT         AUTO_INCREMENT PRIMARY KEY,
    tenant_id   BIGINT         NOT NULL,
    property_id BIGINT         NOT NULL,
    amount_due  DECIMAL(12, 2) NOT NULL,
    amount_paid DECIMAL(12, 2) NOT NULL DEFAULT 0,
    due_date    DATE           NOT NULL,
    paid_date   DATE,
    status      VARCHAR(20)    NOT NULL DEFAULT 'PENDING',
    CONSTRAINT fk_rent_payments_tenant FOREIGN KEY (tenant_id) REFERENCES tenants (id),
    CONSTRAINT fk_rent_payments_property FOREIGN KEY (property_id) REFERENCES properties (id)
);

CREATE TABLE maintenance_requests (
    id           BIGINT       AUTO_INCREMENT PRIMARY KEY,
    tenant_id    BIGINT       NOT NULL,
    property_id  BIGINT       NOT NULL,
    title        VARCHAR(255) NOT NULL,
    description  VARCHAR(2000),
    status       VARCHAR(20)  NOT NULL DEFAULT 'RAISED',
    priority     VARCHAR(20)  NOT NULL DEFAULT 'MEDIUM',
    created_at   TIMESTAMP    NOT NULL,
    resolved_at  TIMESTAMP,
    CONSTRAINT fk_maintenance_tenant FOREIGN KEY (tenant_id) REFERENCES tenants (id),
    CONSTRAINT fk_maintenance_property FOREIGN KEY (property_id) REFERENCES properties (id)
);

CREATE INDEX idx_properties_owner_id ON properties (owner_id);
CREATE INDEX idx_tenants_property_id ON tenants (property_id);
CREATE INDEX idx_rent_payments_tenant_id ON rent_payments (tenant_id);
CREATE INDEX idx_rent_payments_property_id ON rent_payments (property_id);
CREATE INDEX idx_rent_payments_status ON rent_payments (status);
CREATE INDEX idx_maintenance_property_id ON maintenance_requests (property_id);
CREATE INDEX idx_maintenance_tenant_id ON maintenance_requests (tenant_id);
