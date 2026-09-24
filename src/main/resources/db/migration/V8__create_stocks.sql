CREATE TABLE stocks
(
    id       BIGINT NOT NULL AUTO_INCREMENT,
    sku_id   BIGINT NOT NULL,
    quantity INT    NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT uk_stocks_sku UNIQUE (sku_id),
    CONSTRAINT fk_stocks_sku FOREIGN KEY (sku_id) REFERENCES skus (id),
    CONSTRAINT chk_stocks_quantity CHECK ( quantity >= 0 )
) ENGINE = InnoDB;