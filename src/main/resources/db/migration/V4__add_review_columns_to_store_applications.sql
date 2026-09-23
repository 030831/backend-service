ALTER TABLE store_applications
    ADD COLUMN reviewer_admin_id BIGINT NULL,
    ADD COLUMN reviewed_at DATETIME(6) NULL,
    ADD COLUMN rejection_reason VARCHAR(500) NULL,
    ADD CONSTRAINT fk_store_applications_reviewer
        FOREIGN KEY (reviewer_admin_id) REFERENCES admin_accounts(id);