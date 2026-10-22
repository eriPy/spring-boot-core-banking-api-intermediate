CREATE TABLE IF NOT EXISTS accounts_types (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    account_type TEXT
        NOT NULL
        CHECK (account_type IN (
            'savings_account', 
            'checking_account', 
            'payroll_account',
            'fixed_deposit_account'
        ))
);

INSERT INTO accounts_types (account_type) 
VALUES 
    ('savings_account'),
    ('checking_account'),
    ('payroll_account'),
    ('fixed_deposit_account');

CREATE TABLE IF NOT EXISTS person_types (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    person_type TEXT
        NOT NULL
        CHECK (person_type IN (
            'student',  
            'unemployed', 
            'employed', 
            'professional', 
            'businessman'
        )),
    salary_cap NUMERIC(19, 4)
        NOT NULL
);

INSERT INTO person_types (person_type, salary_cap)
VALUES 
    ('student', 500),
    ('unemployed', 750),
    ('employed', 10000),
    ('professional', 15000),
    ('businessman', 20000);

CREATE TABLE IF NOT EXISTS accounts (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    account_holder VARCHAR(50) 
        NOT NULL,
    id_card VARCHAR(10) 
        NOT NULL
        CHECK (id_card LIKE '%-_'),
    email VARCHAR(254) 
        NOT NULL
        CHECK (
            email LIKE '%_@_%_._%' 
            AND email LIKE ANY (ARRAY[
                '%.com', '%.net', '%.org', '%.edu', '%.gob',
                '%.gov', '%.sv', '%.mx', '%.co', '%.io',
                '%.app', '%.info'
            ])
        ), 
    account_password VARCHAR(255)
        NOT NULL,
    account_status TEXT
        NOT NULL
        CHECK (account_status IN (
            'frozen', 
            'closed', 
            'active', 
            'inactive'
        ))
        DEFAULT 'active',
    accounts_type_id BIGINT
        NOT NULL
        REFERENCES accounts_types(id),
    balance NUMERIC(19, 4)
        NOT NULL
        CHECK (balance >= 0)
        DEFAULT 0,
    create_at TIMESTAMP
        NOT NULL
        DEFAULT CURRENT_TIMESTAMP,
    account_card VARCHAR(12)
        NOT NULL
        UNIQUE,
    person_type_id BIGINT
        NOT NULL
        REFERENCES person_types(id),
    monthly_earnings NUMERIC(19, 4)
        NOT NULL,
    UNIQUE (id_card, accounts_type_id, email)
);

CREATE TABLE IF NOT EXISTS savings_accounts (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    account_id BIGINT 
        NOT NULL
        REFERENCES accounts(id),
    excess_transaction_fee NUMERIC(19, 4)
        NOT NULL
        DEFAULT 5,
    credit_interest_rate NUMERIC(5, 4)
        NOT NULL
        DEFAULT 0.0100,
    interest_receivable NUMERIC(19, 4)
        NOT NULL
        DEFAULT 0
        CHECK (interest_receivable >= 0),
    transactions_without_fee INTEGER 
        NOT NULL 
        DEFAULT 6
        CHECK (transactions_without_fee >= 0)
);

ALTER TABLE IF EXISTS savings_accounts
    ADD COLUMN transactions_without_fee INT 
        NOT NULL 
        DEFAULT 6
        CHECK (transactions_without_fee >= 0);

CREATE TABLE IF NOT EXISTS checking_accounts (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    account_id BIGINT 
        NOT NULL
        REFERENCES accounts(id),
    overdraft_limit NUMERIC(19, 4)
        NOT NULL,
    overdraft_used NUMERIC(19, 4)
        NOT NULL
        DEFAULT 0,
    overdraft_interest_rate NUMERIC(5, 4)
        NOT NULL
        DEFAULT 0.0100,
    management_fee NUMERIC(19, 4)
        NOT NULL
        DEFAULT 5,
    interest_payable NUMERIC(19, 4)
        NOT NULL
        DEFAULT 0
        CHECK (interest_payable >= 0)
);

CREATE TABLE IF NOT EXISTS payroll_accounts (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    account_id BIGINT 
        NOT NULL
        REFERENCES accounts(id)
);

CREATE TABLE IF NOT EXISTS fixed_deposit_account (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    account_id BIGINT 
        NOT NULL
        REFERENCES accounts(id),
    maturity_date TIMESTAMP
        NOT NULL,
    credit_interest_rate NUMERIC(5, 4)
        NOT NULL
        DEFAULT 0.0750,
    interest_receivable NUMERIC(19, 4)
        NOT NULL
        DEFAULT 0
        CHECK (interest_receivable >= 0)
);