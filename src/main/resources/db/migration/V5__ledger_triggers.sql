CREATE FUNCTION row_to_ledger()
RETURNS trigger AS $$
DECLARE
    amount_transaction NUMERIC(19, 4);
BEGIN
    SELECT t.amount INTO amount_transaction
    FROM transactions t
    WHERE t.id = NEW.transaction_id;

    INSERT INTO ledger (transaction_id, debit_concept, credit_concept, debit, credit)
    VALUES (NEW.transaction_id, TG_ARGV[0], TG_ARGV[1], amount_transaction, amount_transaction);
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_row_to_ledger_to_withdrawal_transactions
AFTER INSERT ON withdrawal_transactions
FOR EACH ROW
EXECUTE FUNCTION row_to_ledger('customer_deposits', 'cash');

CREATE TRIGGER trg_row_to_ledger_to_deposit_transactions
AFTER INSERT ON deposit_transactions
FOR EACH ROW
EXECUTE FUNCTION row_to_ledger('cash', 'customer_deposits');

CREATE TRIGGER trg_row_to_ledger_to_transfer_transactions
AFTER INSERT ON transfer_transactions
FOR EACH ROW
EXECUTE FUNCTION row_to_ledger('customer_deposits_payee', 'customer_deposits_payer');

CREATE TRIGGER trg_row_to_ledger_to_fee_transactions
AFTER INSERT ON fee_transactions
FOR EACH ROW
EXECUTE FUNCTION row_to_ledger('customer_deposits', 'commission_revenue');

CREATE TRIGGER trg_row_to_ledger_to_interest_credit_transactions
AFTER INSERT ON interest_credit_transactions
FOR EACH ROW
EXECUTE FUNCTION row_to_ledger('interest_expense', 'customer_deposits');

CREATE FUNCTION interest_ledger()
RETURNS trigger AS $$
DECLARE
    overdraft_difference NUMERIC(19, 4);
    interest_difference NUMERIC(19, 4);
BEGIN
    SELECT NEW.ending_overdraft - NEW.previous_overdraft, NEW.ending_interest - NEW.previous_interest
    INTO overdraft_difference, interest_difference;

    IF overdraft_difference > 0 THEN
        INSERT INTO ledger (transaction_id, debit_concept, credit_concept, debit, credit)
        VALUES (NEW.transaction_id, 'overdraft_receivable', 'customer_deposits', overdraft_difference, overdraft_difference);
    ELSIF interest_difference > 0 THEN
        INSERT INTO ledger (transaction_id, debit_concept, credit_concept, debit, credit)
        VALUES (NEW.transaction_id, 'interest_receivable', 'interest_revenue', interest_difference, interest_difference);
    END IF;

    IF interest_difference < 0 AND overdraft_difference < 0 THEN
        INSERT INTO ledger (transaction_id, debit_concept, credit_concept, debit, credit)
        VALUES (NEW.transaction_id, 'customer_deposits', 'overdraft_receivable', 
            (-1 * overdraft_difference), (-1 * overdraft_difference));
        INSERT INTO ledger (transaction_id, debit_concept, credit_concept, debit, credit)
        VALUES (NEW.transaction_id, 'customer_deposits', 'interest_receivable', 
            (-1 * interest_difference), (-1 * interest_difference));

    ELSIF interest_difference < 0 THEN
        INSERT INTO ledger (transaction_id, debit_concept, credit_concept, debit, credit)
        VALUES (NEW.transaction_id, 'customer_deposits', 'interest_receivable', 
            (-1 * interest_difference), (-1 * interest_difference));

    ELSIF overdraft_difference < 0 THEN
        INSERT INTO ledger (transaction_id, debit_concept, credit_concept, debit, credit)
        VALUES (NEW.transaction_id, 'customer_deposits', 'overdraft_receivable', 
            (-1 * overdraft_difference), (-1 * overdraft_difference));
    END IF;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_row_to_ledger_to_interest_accrual_transactions
AFTER INSERT ON interest_accrual_transactions
FOR EACH ROW
EXECUTE FUNCTION interest_ledger();