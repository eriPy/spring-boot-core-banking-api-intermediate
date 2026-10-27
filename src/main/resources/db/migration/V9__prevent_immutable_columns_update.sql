CREATE FUNCTION inmutable_table()
RETURNS trigger AS $$
BEGIN
    RAISE EXCEPTION 'Updates are not allowed on %', TG_TABLE_NAME;
    RETURN OLD;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_inmutable_table_transactions
BEFORE UPDATE OR DELETE ON transactions
FOR EACH ROW 
EXECUTE FUNCTION inmutable_table();

CREATE TRIGGER trg_inmutable_table_ledger
BEFORE UPDATE OR DELETE ON ledger
FOR EACH ROW 
EXECUTE FUNCTION inmutable_table();

CREATE TRIGGER trg_inmutable_table_withdrawal_transactions
BEFORE UPDATE OR DELETE ON withdrawal_transactions
FOR EACH ROW 
EXECUTE FUNCTION inmutable_table();

CREATE TRIGGER trg_inmutable_table_deposit_transactions
BEFORE UPDATE OR DELETE ON deposit_transactions
FOR EACH ROW 
EXECUTE FUNCTION inmutable_table();

CREATE TRIGGER trg_inmutable_table_transfer_transactions
BEFORE UPDATE OR DELETE ON transfer_transactions
FOR EACH ROW 
EXECUTE FUNCTION inmutable_table();

CREATE TRIGGER trg_inmutable_table_fee_transactions
BEFORE UPDATE OR DELETE ON fee_transactions
FOR EACH ROW 
EXECUTE FUNCTION inmutable_table();

CREATE TRIGGER trg_inmutable_table_interest_credit_transactions
BEFORE UPDATE OR DELETE ON interest_credit_transactions
FOR EACH ROW 
EXECUTE FUNCTION inmutable_table();

CREATE TRIGGER trg_inmutable_table_interest_accrual_transactions
BEFORE UPDATE OR DELETE ON interest_accrual_transactions
FOR EACH ROW 
EXECUTE FUNCTION inmutable_table();

CREATE FUNCTION inmutable_column_on_accounts()
RETURNS trigger AS $$
BEGIN
    IF TG_OP = 'DELETE' THEN
        RAISE EXCEPTION 'ID card and Account card cannot be delete';
    END IF;
    IF NEW.id_card IS DISTINCT FROM OLD.id_card
    OR NEW.account_card IS DISTINCT FROM OLD.account_card THEN
        RAISE EXCEPTION 'ID card and Account card cannot be change';
    END IF;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_inmutable_column_on_accounts
BEFORE UPDATE OR DELETE ON accounts
FOR EACH ROW 
EXECUTE FUNCTION inmutable_column_on_accounts();

CREATE FUNCTION inmutable_columns()
RETURNS trigger AS $$
BEGIN
    IF TG_OP = 'DELETE' THEN
        RAISE EXCEPTION 'ID account cannot be delete';
    END IF;
    IF NEW.account_id IS DISTINCT FROM OLD.account_id THEN
        RAISE EXCEPTION 'The references cannot be change';
    END IF;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_inmutable_columns_in_savings_accounts
BEFORE UPDATE OR DELETE ON savings_accounts
FOR EACH ROW 
EXECUTE FUNCTION inmutable_columns();

CREATE TRIGGER trg_inmutable_columns_in_checking_accounts
BEFORE UPDATE OR DELETE ON checking_accounts
FOR EACH ROW 
EXECUTE FUNCTION inmutable_columns();

CREATE TRIGGER trg_inmutable_columns_in_payroll_accounts
BEFORE UPDATE OR DELETE ON payroll_accounts
FOR EACH ROW 
EXECUTE FUNCTION inmutable_columns();

CREATE TRIGGER trg_inmutable_columns_in_fixed_deposit_account
BEFORE UPDATE OR DELETE ON fixed_deposit_account
FOR EACH ROW 
EXECUTE FUNCTION inmutable_columns();