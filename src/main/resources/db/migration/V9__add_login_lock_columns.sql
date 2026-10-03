ALTER TABLE accounts
    ADD COLUMN failed_login_count INT NOT NULL DEFAULT 0,
    ADD COLUMN locked_until DATETIME(6) NULL;

ALTER TABLE admin_accounts
    ADD COLUMN failed_login_count INT NOT NULL DEFAULT 0,
    ADD COLUMN locked_until DATETIME(6) NULL;

-- D. 이미 저장된 이메일을 규칙(공백 제거, 소문자)에 맞춘다. 콜레이션이 대소문자를 무시하므로 중복이 생기지 않는다.
UPDATE accounts SET email = LOWER(TRIM(email));
UPDATE admin_accounts SET email = LOWER(TRIM(email));