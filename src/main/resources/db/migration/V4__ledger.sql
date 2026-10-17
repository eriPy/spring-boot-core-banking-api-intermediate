CREATE TABLE IF NOT EXISTS ledger (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    transaction_id BIGINT
        NOT NULL
        REFERENCES transactions(id),
    debit_concept TEXT
        NOT NULL
        CHECK (debit_concept IN (
            'customer_deposits',
            'cash',
            'customer_deposits_payee',
            'interest_expense',
            'interest_receivable'
        )),
    credit_concept TEXT
        NOT NULL
        CHECK (credit_concept IN (
            'cash',
            'customer_deposits',
            'customer_deposits_payer',
            'commission_revenue',
            'interest_revenue'
        )),
    debit NUMERIC(19, 2) 
        NOT NULL
        CHECK (debit > 0),
    credit NUMERIC(19, 2)
        NOT NULL
        CHECK (credit > 0)
);