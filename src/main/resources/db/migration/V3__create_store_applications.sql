CREATE TABLE store_applications(
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    applicant_account_id BIGINT NOT NULL,
    store_name VARCHAR(50) NOT NULL,
    status VARCHAR(20) NOT NULL,
    pending_applicant_id BIGINT DEFAULT NULL,
    created_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6) NOT NULL,
    CONSTRAINT uk_store_applications_pending UNIQUE(pending_applicant_id),
    CONSTRAINT fk_store_applications_applicant FOREIGN KEY(applicant_account_id) REFERENCES accounts(id)

)