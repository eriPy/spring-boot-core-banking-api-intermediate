CREATE TABLE IF NOT EXISTS token_types (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    token_type TEXT
        NOT NULL
);

INSERT INTO token_types (token_type)
VALUES ('deposit'),
    ('withdrawal'),
    ('transfer'),
    ('person'),
    ('earnings'),
    ('email'),
    ('password'),
    ('close'),
    ('holder');

CREATE TABLE IF NOT EXISTS operation_tokens (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    account_id BIGINT 
        NOT NULL
        REFERENCES accounts(id),
    token VARCHAR(6)
        NOT NULL
        CHECK (LENGTH(token) = 6),
    is_used BOOLEAN
        NOT NULL
        DEFAULT FALSE,
    create_at TIMESTAMP
        NOT NULL
        DEFAULT CURRENT_TIMESTAMP,
    token_type_id BIGINT
        NOT NULL
        REFERENCES token_types(id)
        ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS penalized_reasons (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    reason TEXT
        NOT NULL
        CHECK (reason IN ('smurfing', 'flagged'))
);

INSERT INTO penalized_reasons (reason)
VALUES ('smurfing'),
    ('flagged');

CREATE TABLE IF NOT EXISTS penalized_accounts (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    account_id BIGINT 
        NOT NULL
        REFERENCES accounts(id),
    frozen_until TIMESTAMP
        NOT NULL,
    reason_id BIGINT 
        NOT NULL
        REFERENCES penalized_reasons(id)
);

CREATE TABLE IF NOT EXISTS history (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    account_id BIGINT 
        NOT NULL
        UNIQUE
        REFERENCES accounts(id) 
);

CREATE TABLE IF NOT EXISTS account_penalties_by_smurfing (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    history_id BIGINT
        NOT NULL
        UNIQUE
        REFERENCES history(id),
    warnings INTEGER
        NOT NULL
        CHECK (warnings >= 0 AND warnings <= 9),
    suspensions INTEGER 
        NOT NULL
        DEFAULT 0
        CHECK (suspensions >= 0 AND suspensions <= 3)
);

CREATE TABLE IF NOT EXISTS account_penalties_by_flagged (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    history_id BIGINT
        NOT NULL
        UNIQUE
        REFERENCES history(id),
    suspicions INTEGER
        NOT NULL
        DEFAULT 0
        CHECK (suspicions <= 2 AND suspicions >= 0)
);

CREATE TABLE IF NOT EXISTS black_list (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    user_id BIGINT 
        NOT NULL
        REFERENCES users(id),
    adding_to TIMESTAMP 
        NOT NULL
        DEFAULT CURRENT_TIMESTAMP
);