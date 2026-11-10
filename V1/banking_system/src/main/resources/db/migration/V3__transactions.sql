CREATE TABLE IF NOT EXISTS transaction_types (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    transaction_type TEXT
        NOT NULL
);

INSERT INTO transaction_types (transaction_type)
VALUES 
    ('withdrawal'), 
    ('deposit'), 
    ('transfer'),
    ('fee'), 
    ('interest_credit'), 
    ('interest_accrual');                                                                                                                

CREATE TABLE IF NOT EXISTS payment_methods (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    payment_method TEXT
        NOT NULL
);

INSERT INTO payment_methods (payment_method)
VALUES ('cash'),
    ('check'),
    ('bank_teller'),
    ('atm'),
    ('bank'),
    ('system');

CREATE TABLE IF NOT EXISTS transactions (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    account_id BIGINT
        NOT NULL
        REFERENCES accounts(id),
    transaction_type_id BIGINT
        NOT NULL
        REFERENCES transaction_types(id),
    amount NUMERIC(19, 4)
        NOT NULL
        CHECK (amount > 0),
    transaction_date TIMESTAMP
        NOT NULL
        DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS withdrawal_transactions (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    transaction_id BIGINT
        NOT NULL
        REFERENCES transactions(id),
    previous_balance NUMERIC(19, 4),
    ending_balance NUMERIC(19, 4),
    payment_method_id BIGINT
        NOT NULL
        REFERENCES payment_methods(id),
    CONSTRAINT opening_balance CHECK (previous_balance >= 0),
    CONSTRAINT closing_balance CHECK (previous_balance > ending_balance)
);

CREATE TABLE IF NOT EXISTS deposit_transactions (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    transaction_id BIGINT
        NOT NULL
        REFERENCES transactions(id),
    previous_balance NUMERIC(19, 4),
    ending_balance NUMERIC(19, 4),
    payment_method_id BIGINT
        NOT NULL
        REFERENCES payment_methods(id),
    CONSTRAINT opening_balance CHECK (previous_balance >= 0),
    CONSTRAINT closing_balance CHECK (ending_balance > previous_balance)
);

CREATE TABLE IF NOT EXISTS transfer_transactions (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    transaction_id BIGINT
        NOT NULL
        REFERENCES transactions(id),
    previous_balance_payer NUMERIC(19, 4),
    ending_balance_payer NUMERIC(19, 4),
    payee_id BIGINT 
        NOT NULL
        REFERENCES accounts(id),
    previous_balance_payee NUMERIC(19, 4),
    ending_balance_payee NUMERIC(19, 4),
    payment_method_id BIGINT
        NOT NULL
        REFERENCES payment_methods(id),
    CONSTRAINT opening_balance_payer CHECK (previous_balance_payer >= 0),
    CONSTRAINT closing_balance_payer CHECK (previous_balance_payer > ending_balance_payer),
    CONSTRAINT opening_balance_payee CHECK (previous_balance_payee >= 0),
    CONSTRAINT closing_balance_payee CHECK (ending_balance_payee > previous_balance_payee)   
);

CREATE TABLE IF NOT EXISTS fee_transactions (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    transaction_id BIGINT
        NOT NULL
        REFERENCES transactions(id),
    payment_method_id BIGINT
        NOT NULL
        REFERENCES payment_methods(id),
    previous_balance NUMERIC(19, 4),
    ending_balance NUMERIC(19, 4),
    CONSTRAINT opening_balance CHECK (previous_balance >= 0),
    CONSTRAINT closing_balance CHECK (ending_balance < previous_balance)
);

CREATE TABLE IF NOT EXISTS interest_credit_transactions (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    transaction_id BIGINT
        NOT NULL
        REFERENCES transactions(id),
    payment_method_id BIGINT
        NOT NULL
        REFERENCES payment_methods(id),
    previous_balance NUMERIC(19, 4),
    ending_balance NUMERIC(19, 4),
    CONSTRAINT opening_balance CHECK (previous_balance >= 0),
    CONSTRAINT closing_balance CHECK (ending_balance > previous_balance)
);

CREATE TABLE IF NOT EXISTS interest_accrual_transactions (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    transaction_id BIGINT
        NOT NULL
        REFERENCES transactions(id),
    payment_method_id BIGINT
        NOT NULL
        REFERENCES payment_methods(id),
    previous_overdraft NUMERIC(19, 4)
        NOT NULL
        CHECK (previous_overdraft >= 0),
    ending_overdraft NUMERIC(19, 4)
        NOT NULL
        CHECK (ending_overdraft >= 0),
    previous_interest NUMERIC(19, 4)
        NOT NULL
        CHECK (previous_interest >= 0),
    ending_interest NUMERIC(19, 4)
        NOT NULL
        CHECK (ending_interest >= 0)
);