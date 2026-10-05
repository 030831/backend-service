CREATE TABLE orders
(
    id              BIGINT       NOT NULL AUTO_INCREMENT,
    account_id      BIGINT       NOT NULL,
    status          VARCHAR(20)  NOT NULL,
    total_amount    BIGINT       NOT NULL,
    pay_amount      BIGINT       NOT NULL,
    refunded_amount BIGINT       NOT NULL DEFAULT 0,
    recipient_name  VARCHAR(50)  NOT NULL,
    recipient_phone VARCHAR(20)  NOT NULL,
    zip_code        VARCHAR(10)  NOT NULL,
    address1        VARCHAR(200) NOT NULL,
    address2        VARCHAR(200) NULL,
    delivery_memo   VARCHAR(200) NULL,
    idempotency_key VARCHAR(64)  NOT NULL,
    expires_at      DATETIME(6) NOT NULL,
    paid_at         DATETIME(6)  NULL,
    canceled_at     DATETIME(6)  NULL,
    created_at      DATETIME(6)  NOT NULL,
    updated_at      DATETIME(6)  NOT NULL,


    PRIMARY KEY (id),
    CONSTRAINT uk_orders_idempotency UNIQUE (account_id, idempotency_key),
    CONSTRAINT fk_orders_account FOREIGN KEY (account_id) REFERENCES accounts (id),
    CONSTRAINT chk_orders_amounts CHECK (
        total_amount >= 0
            AND pay_amount >= 0 AND pay_amount <= total_amount
            AND refunded_amount >= 0 AND refunded_amount <= pay_amount ),
    INDEX           idx_orders_status_expires (status, expires_at)
) ENGINE = InnoDB;