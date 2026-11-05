CREATE FUNCTION balances_increase_or_decrease()
RETURNS trigger AS $$
DECLARE
    balance_account NUMERIC(19, 4);
    transaction_amount NUMERIC(19, 4);
    sign_ NUMERIC := TG_ARGV[0]::NUMERIC;
BEGIN
    SELECT a.balance INTO balance_account
    FROM transactions t
    JOIN accounts a ON a.id = t.account_id
    WHERE t.id = NEW.transaction_id;
    
    SELECT t.amount INTO transaction_amount
    FROM transactions t
    WHERE t.id = NEW.transaction_id;

    NEW.previous_balance := balance_account;
    NEW.ending_balance   := balance_account - sign_ * transaction_amount;

    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_balances_decrease
BEFORE INSERT ON withdrawal_transactions
FOR EACH ROW
EXECUTE FUNCTION balances_increase_or_decrease('1');

CREATE TRIGGER trg_balances_increase
BEFORE INSERT ON deposit_transactions
FOR EACH ROW
EXECUTE FUNCTION balances_increase_or_decrease('-1');

CREATE TRIGGER trg_balances_decrease_to_fee
BEFORE INSERT ON fee_transactions
FOR EACH ROW
EXECUTE FUNCTION balances_increase_or_decrease('1');

CREATE TRIGGER trg_balances_increase_to_credit
BEFORE INSERT ON interest_credit_transactions
FOR EACH ROW
EXECUTE FUNCTION balances_increase_or_decrease('-1');

CREATE FUNCTION set_balance_in_transaction()
RETURNS trigger AS $$
DECLARE 
    payee_balance NUMERIC(19, 4);
    payer_balance NUMERIC(19, 4);
    transaction_amount NUMERIC(19, 4);
BEGIN
    SELECT a.balance INTO payer_balance
    FROM transactions t
    JOIN accounts a ON a.id = t.account_id
    WHERE t.id = NEW.transaction_id;

    SELECT a.balance INTO payee_balance
    FROM accounts a 
    WHERE a.id = NEW.payee_id;

    SELECT t.amount INTO transaction_amount
    FROM transactions t
    WHERE t.id = NEW.transaction_id;

    NEW.previous_balance_payer := payer_balance;
    NEW.previous_balance_payee := payee_balance;
    NEW.ending_balance_payer := payer_balance - transaction_amount;
    NEW.ending_balance_payee := payee_balance + transaction_amount;

    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_set_balance_in_transaction
BEFORE INSERT ON transfer_transactions              
FOR EACH ROW
EXECUTE FUNCTION set_balance_in_transaction();