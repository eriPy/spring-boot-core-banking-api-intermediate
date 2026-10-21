ALTER TABLE IF EXISTS savings_accounts
    ADD COLUMN transactions_without_fee INT 
        NOT NULL 
        DEFAULT 6
        CHECK (transactions_without_fee >= 0);