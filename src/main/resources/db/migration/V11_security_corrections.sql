ALTER TABLE IF EXISTS account_penalties_by_smurfing
    DROP CONSTRAINT account_penalties_by_smurfing_warnings_check,
    ADD CONSTRAINT account_penalties_by_smurfing_warnings_check 
    CHECK (warnings >= 0 AND warnings <= 9),
    ALTER COLUMN warnings SET DEFAULT 0,
    DROP CONSTRAINT account_penalties_by_smurfing_suspensions_check,
    ADD CONSTRAINT account_penalties_by_smurfing_suspensions_check
    CHECK (suspensions >= 0 AND suspensions <= 3);

CREATE FUNCTION add_row_to_penalties()
RETURNS trigger AS $$
BEGIN
    INSERT INTO account_penalties_by_smurfing (history_id)
    VALUES (NEW.id);

    INSERT INTO account_penalties_by_flagged (history_id)
    VALUES (NEW.id);

    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_add_row_to_penalties
AFTER INSERT ON history
FOR EACH ROW
EXECUTE FUNCTION add_row_to_penalties();