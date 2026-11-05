import type React from "react";
import { useState } from "react";
import { useNavigate } from "react-router-dom";
import { evaluateIdCard, resetStrings } from "../../../utils/evaluate_fields";
import { conditionAdv, showAdv } from "../../../utils/advs";
import { useAccountStore } from "../../../utils/store";
import { createJson } from "../../../utils/send_data";

function VerificationToNew() {
    const navigate = useNavigate();
    const URL = import.meta.env.VITE_API_URL;

    const [idCardNumber, setIdCardNumber] = useState("");
    const [email, setEmail] = useState("");
    const [accountType, setAccountType] = useState<
        "savings_account" | "checking_account" | "payroll_account" | "fixed_deposit_account"
    >("savings_account");
    const [personType, setPersonType] = useState<
        "student" | "unemployed" | "employed" | "professional" | "businessman"
    >("student");

    const setAccountTypeStore = useAccountStore((state) => state.setAccountType);
    const setPersonTypeStore = useAccountStore((state) => state.setPersonType);

    const [advUser, setAdvUser] = useState(false);
    const [advInconsistence, setInconsistence] = useState(false);
    const [advCredentials, setAdvCredentials] = useState(false);
    const [adv429, setAdv429] = useState(false);

    const sendDataToCreateNewUser = async(e: React.FormEvent<HTMLFormElement>) => {
        e.preventDefault();
        if (conditionAdv(!evaluateIdCard(idCardNumber), setAdvCredentials)) return;
        const isInconsistence: boolean = accountType === "payroll_account" && 
            (personType === "student" || personType === "unemployed");
        if (conditionAdv(isInconsistence, setInconsistence)) return;
        const res = await fetch(`${URL}/auth/register`, createJson("POST", 
            {idCardNumber, email, accountType, personType}));
        if (conditionAdv(res.status === 429, setAdv429)) return;
        if (!res.ok) {
            showAdv(setAdvUser);
            return;
        }
        setAccountTypeStore(accountType);
        setPersonTypeStore(personType);
        resetStrings(setIdCardNumber, setEmail);
        setAccountType("savings_account");
        setPersonType("student");
        navigate("/create-user");
    }

    return (
        <main>
            <header>
                <h1>
                    Register User
                </h1>
            </header>
            <form onSubmit={sendDataToCreateNewUser}>
                <div style={{ marginBottom: '1.5rem', display: 'flex', flexDirection: 'column' }}>
                    <label htmlFor="id-card-number-field">
                        Enter your ID card number
                    </label>
                    <input
                        type="text"
                        id="id-card-number-field"
                        value={idCardNumber}
                        onChange={(e) => setIdCardNumber(e.target.value)}
                        placeholder="00000000-0"
                    />
                </div>
                <div style={{ marginBottom: '1.5rem', display: 'flex', flexDirection: 'column' }}>
                    <label htmlFor="email-field">
                        Enter your email address
                    </label>
                    <input
                        type="email"
                        id="email-field"
                        value={email}
                        onChange={(e) => setEmail(e.target.value)}
                        placeholder="enter an email address"
                    />
                </div>
                <div style={{ marginBottom: '1.5rem', display: 'flex', flexDirection: 'column' }}>
                    <label htmlFor="account-type-field">
                        Select the account type you want to create
                    </label>
                    <select
                        id="account-type-field"
                        value={accountType}
                        onChange={(e) => setAccountType(e.target.value as any)}
                    >
                        <option value="savings_account">
                            Savings account
                        </option>
                        <option value="checking_account">
                            Checking account
                        </option>
                        <option value="payroll_account">
                            Payroll account
                        </option>
                        <option value="fixed_deposit_account">
                            Fixed deposit account
                        </option>
                    </select>
                </div>
                <div style={{ marginBottom: '1.5rem', display: 'flex', flexDirection: 'column' }}>
                    <label htmlFor="person-type-field">
                        Select your customer type
                    </label>
                    <select
                        id="person-type-field"
                        value={personType}
                        onChange={(e) => setPersonType(e.target.value as any)}
                    >
                        <option value="student"
                        >
                            student
                        </option>
                        <option value="unemployed">
                            unemployed
                        </option>
                        <option value="employed">
                            employed
                        </option>
                        <option value="professional">
                            professional
                        </option>
                        <option value="businessman">
                            businessman
                        </option>
                    </select>
                </div>
                {advUser && 
                <p>
                    This user is already registered
                </p>}
                {advInconsistence &&
                <p>
                    This customer type cannot use this account type
                </p>}
                {advCredentials &&
                <p>
                    Invalid credentials
                </p>}
                {adv429 &&
                <p>
                    Please wait
                </p>}
                <button type="submit">
                    Register
                </button>
            </form>
        </main>
    )
}

export default VerificationToNew;