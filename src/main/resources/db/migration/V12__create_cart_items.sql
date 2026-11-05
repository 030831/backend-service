CREATE TABLE cart_items
(
    id         BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    account_id BIGINT NOT NULL,
    sku_id     BIGINT NOT NULL,
    quantity   INT    NOT NULL,
    created_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6) NOT NULL,
    CONSTRAINT uk_cart_items_account_sku UNIQUE (account_id, sku_id),
    CONSTRAINT fk_cart_items_account FOREIGN KEY (account_id) REFERENCES accounts(id),
    CONSTRAINT fk_cart_items_sku FOREIGN KEY (sku_id) REFERENCES skus(id),
    CONSTRAINT chk_cart_items_quantity CHECK ( quantity >= 1 )
) ENGINE = InnoDB;
