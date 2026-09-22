CREATE TABLE accounts (
                          id            BIGINT       NOT NULL AUTO_INCREMENT,
                          email         VARCHAR(254) NOT NULL,
                          nickname      VARCHAR(20)  NOT NULL,
                          password_hash VARCHAR(255) NOT NULL,
                          created_at    DATETIME(6)  NOT NULL,
                          updated_at    DATETIME(6)  NOT NULL,
                          PRIMARY KEY (id),
                          CONSTRAINT uk_accounts_email UNIQUE (email),
                          CONSTRAINT uk_accounts_nickname UNIQUE (nickname)
) ENGINE = InnoDB;