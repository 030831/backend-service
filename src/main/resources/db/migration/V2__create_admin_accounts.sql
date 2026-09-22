CREATE TABLE admin_accounts (
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    email VARCHAR(254) NOT NULL,
    name VARCHAR(50) NOT NULL DEFAULT '관리자',
    password_hash VARCHAR(255) NOT NULL ,
    created_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6) NOT NULL,
    CONSTRAINT uk_admin_accounts_email UNIQUE (email)
) ENGINE = InnoDB;