CREATE TABLE IF NOT EXISTS ledger (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    transaction_id BIGINT
        NOT NULL
        REFERENCES transactions(id),
    debit_concept TEXT
        NOT NULL
        CHECK (debit_concept IN (
            'cash',       
            'customer_deposits',    
            'customer_deposits_payee', 
            'interest_expense',        
            'overdraft_receivable',    
            'interest_receivable'     
        )),
    credit_concept TEXT
        NOT NULL
        CHECK (credit_concept IN (
            'cash',
            'customer_deposits',      
            'customer_deposits_payer',
            'commission_revenue',     
            'interest_revenue',       
            'overdraft_receivable',    
            'interest_receivable'      
        )),
    debit NUMERIC(19, 2) 
        NOT NULL
        CHECK (debit > 0),
    credit NUMERIC(19, 2)
        NOT NULL
        CHECK (credit > 0),
    registered_at TIMESTAMP
        NOT NULL
        DEFAULT CURRENT_TIMESTAMP
);