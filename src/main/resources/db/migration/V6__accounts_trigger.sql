CREATE FUNCTION approve_overdraft() 
RETURNS trigger AS $$
DECLARE
    ac_ty TEXT;
    p_t TEXT;
BEGIN
    SELECT act.account_type INTO ac_ty
    FROM accounts a
    JOIN accounts_types act ON act.id = a.accounts_type_id
    WHERE a.id = NEW.id;
    
    SELECT pt.person_type INTO p_t
    FROM accounts a
    JOIN person_types pt ON pt.id = a.person_type_id
    WHERE a.id = NEW.id;

    IF ac_ty = 'checking_account' THEN
        INSERT INTO checking_accounts (account_id, overdraft_limit)
        VALUES (
            NEW.id,
            CASE p_t
                WHEN 'student' THEN 200
                WHEN 'unemployed' THEN 500
                WHEN 'employed' THEN 1000
                WHEN 'professional' THEN 2000
                WHEN 'businessman' THEN 5000
            END
        );
    END IF;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_approve_overdraft
AFTER INSERT ON accounts
FOR EACH ROW
EXECUTE FUNCTION approve_overdraft();