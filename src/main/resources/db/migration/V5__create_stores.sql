CREATE TABLE stores (
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    account_id BIGINT NOT NULL,
    name VARCHAR(50) NOT NULL ,
    created_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6) NOT NULL,
    CONSTRAINT uk_stores_account UNIQUE (account_id),
    CONSTRAINT fk_stores_account FOREIGN KEY (account_id) REFERENCES accounts(id)
)