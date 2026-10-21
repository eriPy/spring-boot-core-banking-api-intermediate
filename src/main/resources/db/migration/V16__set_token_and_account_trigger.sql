TRUNCATE TABLE token_types RESTART IDENTITY;

INSERT INTO token_types (token_type)
VALUES ('deposit'),
    ('withdrawal'),
    ('transfer'),
    ('person'),
    ('earning'),
    ('account'),
    ('close');

CREATE TABLE IF NOT EXISTS overdrafts (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    person_types_id BIGINT  
        NOT NULL
        REFERENCES person_types(id),
    overdraft_value NUMERIC(19, 4)
        NOT NULL
        UNIQUE
);

INSERT INTO overdrafts (person_type_id, overdraft_value)
SELECT pt.id, v.overdraft_value
FROM (VALUES
    ('student', 200),
    ('unemployed', 500),
    ('employed', 1000),
    ('professional', 2000),
    ('businessman', 5000)
) AS v(person_type, overdraft_value)
JOIN person_types pt ON pt.person_type = v.person_type;

DROP TRIGGER IF EXISTS trg_approve_overdraft ON accounts;
DROP FUNCTION IF EXISTS approve_overdraft();

CREATE FUNCTION approve_overdraft() 
RETURNS trigger AS $$
DECLARE
    ac_ty TEXT;
    p_t TEXT;
BEGIN
    SELECT act.account_type, pt.person_type INTO ac_ty, p_t
    FROM accounts a
    JOIN accounts_types act ON act.id = a.accounts_type_id
    JOIN person_types pt ON pt.id = a.person_type_id
    WHERE a.id = NEW.id;    

    IF ac_ty = 'checking_account' THEN
        INSERT INTO checking_accounts (account_id, overdraft_limit)
        VALUES (
            NEW.id,
            (SELECT o.overdraft_value
            FROM overdrafts o
            WHERE o.overdraft_for = p_t)
        )
        ON CONFLICT (account_id)
        DO UPDATE SET overdraft_limit = EXCLUDED.overdraft_limit;
    END IF;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_approve_overdraft
AFTER INSERT OR UPDATE OF person_type_id ON accounts
FOR EACH ROW
EXECUTE FUNCTION approve_overdraft();