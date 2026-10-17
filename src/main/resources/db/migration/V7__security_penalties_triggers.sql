CREATE FUNCTION sum_to_penalties()
RETURNS trigger AS $$
DECLARE
    reason_type TEXT;
    history_searching BIGINT;
BEGIN
    SELECT pr.reason INTO reason_type
    FROM penalized_accounts pa
    JOIN penalized_reasons pr ON pr.id = pa.reason_id
    WHERE pa.id = NEW.id;

    SELECT h.id INTO history_searching
    FROM history h
    WHERE h.account_id = NEW.account_id;

    IF reason_type = 'smurfing' THEN
        UPDATE account_penalties_by_smurfing
        SET suspensions = suspensions + 1
        WHERE account_penalties_by_smurfing.history_id = history_searching;
    ELSEIF reason_type = 'flagged' THEN
        UPDATE account_penalties_by_flagged
        SET suspicions = suspicions + 1
        WHERE account_penalties_by_flagged.history_id = history_searching;
    END IF;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_sum_to_penalties
AFTER INSERT ON penalized_accounts
FOR EACH ROW
EXECUTE FUNCTION sum_to_penalties();