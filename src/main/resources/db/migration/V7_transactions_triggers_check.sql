CREATE FUNCTION check_payment_method()
RETURNS trigger AS $$
DECLARE
    method TEXT;
BEGIN
    SELECT pm.payment_method INTO method
    FROM payment_methods pm
    WHERE pm.id = NEW.payment_method_id;

    IF method IS NULL OR NOT (method = ANY (TG_ARGV)) THEN
        RAISE EXCEPTION 'Invalid method for %: must be one of [%]',
            TG_TABLE_NAME, array_to_string(TG_ARGV, ', ');
    END IF;

    RETURN NEW;
END;
$$ LANGUAGE plpgsql;        

CREATE TRIGGER trg_check_withdrawal
BEFORE INSERT OR UPDATE OF payment_method_id ON withdrawal_transactions
FOR EACH ROW
EXECUTE FUNCTION check_payment_method('cash', 'atm', 'bank_teller');

CREATE TRIGGER trg_check_deposit
BEFORE INSERT OR UPDATE OF payment_method_id ON deposit_transactions
FOR EACH ROW
EXECUTE FUNCTION check_payment_method('cash', 'atm', 'bank_teller', 'check');

CREATE TRIGGER trg_check_transfer
BEFORE INSERT OR UPDATE OF payment_method_id ON transfer_transactions
FOR EACH ROW
EXECUTE FUNCTION check_payment_method('bank');

CREATE TRIGGER trg_check_fee
BEFORE INSERT OR UPDATE OF payment_method_id ON fee_transactions
FOR EACH ROW
EXECUTE FUNCTION check_payment_method('system');

CREATE TRIGGER trg_check_interest_credit
BEFORE INSERT OR UPDATE OF payment_method_id ON interest_credit_transactions
FOR EACH ROW
EXECUTE FUNCTION check_payment_method('system');

CREATE TRIGGER trg_check_interest_charge
BEFORE INSERT OR UPDATE OF payment_method_id ON interest_charge_transactions
FOR EACH ROW
EXECUTE FUNCTION check_payment_method('system');

CREATE FUNCTION check_time()
RETURNS trigger AS $$
DECLARE
    last_time TIMESTAMP;
    operation_id BIGINT;
BEGIN
    SELECT tt.id INTO operation_id
    FROM transaction_types tt
    WHERE tt.transaction_type = 'fee';

    IF operation_id = NEW.transaction_type_id THEN
        SELECT MAX(t.transaction_date) INTO last_time
        FROM transactions t
        WHERE t.account_id = NEW.account_id AND t.transaction_type_id = operation_id;

        IF last_time IS NOT NULL AND last_time + INTERVAL '1 month' > CURRENT_TIMESTAMP THEN 
            RAISE EXCEPTION 'Fee cannot be charged yet';
        END IF;
    END IF;

    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_check_time
BEFORE INSERT ON transactions
FOR EACH ROW
EXECUTE FUNCTION check_time();

CREATE FUNCTION check_account_type()
RETURNS trigger AS $$
DECLARE
    account_is TEXT;
BEGIN
    SELECT ats.account_type INTO account_is
    FROM transactions t
    JOIN accounts a ON a.id =  t.account_id
    JOIN accounts_types ats ON ats.id = a.accounts_type_id
    WHERE t.id = NEW.transaction_id;

    IF account_is IS NULL OR (account_is = ANY (TG_ARGV)) THEN
        RAISE EXCEPTION 'This account cannot process this transaction to this account type';
    END IF;

    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_account_to_transfer
BEFORE INSERT ON transfer_transactions
FOR EACH ROW
EXECUTE FUNCTION check_account_type('fixed_deposit_account');

CREATE TRIGGER trg_account_to_fee
BEFORE INSERT ON fee_transactions
FOR EACH ROW
EXECUTE FUNCTION check_account_type('payroll_account', 'fixed_deposit_account');

CREATE TRIGGER trg_account_to_interest_credit
BEFORE INSERT ON interest_credit_transactions
FOR EACH ROW
EXECUTE FUNCTION check_account_type('checking_account', 'payroll_account');

CREATE TRIGGER trg_interest_accrual
BEFORE INSERT ON interest_accrual_transactions
FOR EACH ROW
EXECUTE FUNCTION check_account_type('checking_account', 'payroll_account', 'fixed_deposit_account');

CREATE FUNCTION check_deposit()
RETURNS trigger AS $$
DECLARE 
    is_fixed_deposit_account TEXT;
    account BIGINT;
    type_is BIGINT;
    transaction_find BIGINT;
BEGIN
    SELECT t.account_id INTO account
    FROM transactions t
    WHERE t.id = NEW.transaction_id;

    SELECT ats.account_type INTO is_fixed_deposit_account
    FROM accounts a
    JOIN accounts_types ats ON ats.id = a.accounts_type_id
    WHERE a.id = account;

    IF is_fixed_deposit_account IS NOT NULL AND is_fixed_deposit_account = 'fixed_deposit_account' THEN
        SELECT tt.id INTO type_is
        FROM transaction_types tt
        WHERE tt.transaction_type = 'deposit';

        SELECT t.id INTO transaction_find
        FROM transactions t
        WHERE t.account_id = account AND t.transaction_type_id = type_is;

        IF transaction_find IS NOT NULL THEN
            RAISE EXCEPTION 'This account cannot process other deposit';
        END IF;
    END IF;

    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_check_deposit
BEFORE INSERT ON deposit_transactions
FOR EACH ROW
EXECUTE FUNCTION check_deposit();

CREATE FUNCTION withdrawal_is_available()
RETURNS trigger AS $$
DECLARE     
    stipulated_date TIMESTAMP;
    balance_account NUMERIC(19, 4);
    interest_to_receivable NUMERIC(19, 4);
BEGIN
    SELECT fda.maturity_date INTO stipulated_date
    FROM transactions t
    JOIN fixed_deposit_account fda ON fda.account_id = t.account_id
    WHERE t.id = NEW.transaction_id;

    IF stipulated_date > CURRENT_TIMESTAMP THEN
        RAISE EXCEPTION 'The stipulated date is not available yet';
    END IF;

    SELECT a.balance INTO balance_account
    FROM transactions t
    JOIN accounts a ON a.id = t.account_id
    WHERE t.id = NEW.transaction_id;

    SELECT fda.interest_receivable INTO interest_to_receivable
    FROM transactions t
    JOIN fixed_deposit_account fda ON fda.account_id = t.account_id
    WHERE t.id = NEW.transaction_id;

    IF balance_account + interest_to_receivable != NEW.amount THEN
        RAISE EXCEPTION 'You must withdraw all the money from the account';
    END IF;

    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_withdrawal_is_available
BEFORE INSERT ON withdrawal_transactions
FOR EACH ROW 
EXECUTE FUNCTION withdrawal_is_available();