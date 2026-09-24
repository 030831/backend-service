CREATE TABLE products
(
    id          BIGINT        NOT NULL AUTO_INCREMENT PRIMARY KEY,
    store_id    BIGINT        NOT NULL,
    name        VARCHAR(100)  NOT NULL,
    description VARCHAR(2000) NOT NULL,
    status      VARCHAR(20)   NOT NULL,
    created_at  DATETIME(6) NOT NULL,
    updated_at  DATETIME(6) NOT NULL,

    CONSTRAINT fk_products_store FOREIGN KEY (store_id) REFERENCES stores (id),
    INDEX       idx_products_status_id (status, id)
)