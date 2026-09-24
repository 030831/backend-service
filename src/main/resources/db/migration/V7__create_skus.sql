CREATE TABLE skus
(
    id           BIGINT       NOT NULL AUTO_INCREMENT PRIMARY KEY,
    product_id   BIGINT       NOT NULL,
    option_label VARCHAR(100) NOT NULL DEFAULT '',
    price        BIGINT       NOT NULL,
    created_at   DATETIME(6) NOT NULL,
    updated_at   DATETIME(6) NOT NULL,

    CONSTRAINT uk_skus_product_option UNIQUE (product_id, option_label),
    CONSTRAINT fk_skus_product FOREIGN KEY (product_id) REFERENCES products (id)
)