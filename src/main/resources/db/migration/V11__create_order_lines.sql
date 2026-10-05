CREATE TABLE order_lines
(
    id           BIGINT       NOT NULL AUTO_INCREMENT,
    order_id     BIGINT       NOT NULL,
    sku_id       BIGINT       NOT NULL,
    product_id   BIGINT       NOT NULL,
    store_id     BIGINT       NOT NULL,
    product_name VARCHAR(100) NOT NULL,
    option_label VARCHAR(100) NOT NULL,
    unit_price   BIGINT       NOT NULL,
    quantity     INT          NOT NULL,
    status       VARCHAR(20)  NOT NULL,
    canceled_at  DATETIME(6) NULL,
    PRIMARY KEY (id),
    CONSTRAINT uk_order_lines_order_sku UNIQUE (order_id, sku_id),
    CONSTRAINT fk_order_lines_order FOREIGN KEY (order_id) REFERENCES orders (id),
    CONSTRAINT fk_order_lines_sku FOREIGN KEY (sku_id) REFERENCES skus (id),
    CONSTRAINT fk_order_lines_product FOREIGN KEY (product_id) REFERENCES products (id),
    CONSTRAINT fk_order_lines_store FOREIGN KEY (store_id) REFERENCES stores (id),
    CONSTRAINT chk_order_lines_values CHECK ( unit_price >= 0 AND quantity >= 1)
) ENGINE = InnoDB;