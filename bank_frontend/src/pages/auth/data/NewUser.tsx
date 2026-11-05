import React, { useState } from "react";
import { useNavigate } from "react-router-dom";
import { evaluateString, resetNumber, resetStrings } from "../../../utils/evaluate_fields";
import { conditionAdv } from "../../../utils/advs";
import { createJson } from "../../../utils/send_data";
import { useAccountStore, useUserStore } from "../../../utils/store";

function NewUser() {
    const navigate = useNavigate();
    const URL = import.meta.env.VITE_API_URL;

    const [accountHolder, setAccountHolder] = useState("");
    const [password, setPassword] = useState("");
    const [monthlyEarnings, setMonthlyEarnings] = useState(0.01);

    const [advSize, setAdvSize] = useState(false);
    const [advValue, setAdvValue] = useState(false);
    const [advField, setAdvField] = useState(false);
    const [adv429, setAdv429] = useState(false);

    const setTokenStore = useUserStore((state) => state.setToken);
    const setAccountHolderStore = useUserStore((state) => state.setAccountHolder);
    const setEmailStore = useUserStore((state) => state.setEmail);
    const setAccountCardStore = useUserStore((state) => state.setAccountCard);
    const setPersonTypeStore = useUserStore((state) => state.setPersonType);
    const setMonthlyEarningsStore = useUserStore((state) => state.setMonthlyEarnings);
    const setAccountTypesStore = useUserStore((state) => state.setAccountTypes);

    const clearAccountStore = useAccountStore((state) => state.clearStore);

    const createUserAndAccount = async(e: React.FormEvent<HTMLFormElement>) => {
        e.preventDefault();
        if (conditionAdv(evaluateString(accountHolder, password), setAdvField)) return;
        if (conditionAdv(monthlyEarnings <= 0, setAdvValue)) return;
        if (conditionAdv(password.length < 8, setAdvSize)) return;
        const res = await fetch(`${URL}/auth/sign-up`, createJson("POST"));
        if (conditionAdv(res.status === 429, setAdv429)) return;
        if (res.status !== 201) {

        }
        const req = await res.json();
        setTokenStore(req.body.token);
        setAccountHolderStore(accountHolder);
        setEmailStore(req.body.email);
        setAccountCardStore(req.body.accounts[0]);
        setMonthlyEarningsStore(monthlyEarnings);
        setPersonTypeStore(useAccountStore((state) => state.personType));                           
        setAccountTypesStore([useAccountStore.getState().accountType]);
        resetStrings(setAccountHolder, setPassword);
        resetNumber(setMonthlyEarnings);
        clearAccountStore();
        navigate("/me");
    }

    return (
        <main>
            <header>
                <h1>
                    Create account
                </h1>
            </header>
            <form onSubmit={createUserAndAccount}>
                <div style={{ marginBottom: '1.5rem', display: 'flex', flexDirection: 'column' }}>
                    <label htmlFor="name-field">
                        Enter the user's name
                    </label>
                    <input
                        type="text"
                        id="name-field"
                        value={accountHolder}
                        onChange={(e) => setAccountHolder(e.target.value)}
                        placeholder="enter a user's name"
                    />
                </div>
                <div style={{ marginBottom: '1.5rem', display: 'flex', flexDirection: 'column' }}>
                    <label htmlFor="password-field">
                        Enter the password
                    </label>
                    <input
                        type="password"
                        id="password-field"
                        value={password}
                        onChange={(e) => setPassword(e.target.value)}
                        placeholder="enter a password"
                    />
                </div>
                <div style={{ marginBottom: '1.5rem', display: 'flex', flexDirection: 'column' }}>
                    <label htmlFor="monthly-earnings-field">
                        Enter your monthly earnings
                    </label>
                    <input
                        type="number"
                        step="0.01"
                        id="monthly-earnings-field"
                        value={monthlyEarnings}
                        onChange={(e) => setMonthlyEarnings(Number(e.target.value))}
                    />
                </div>
                {advSize &&
                <p>
                    The password must be at least 8 characters long
                </p>}
                {advValue && 
                <p>
                    Monthly earnings must be at least 0.01
                </p>}
                {advField &&
                <p>
                    All fields are required
                </p>}
                {adv429 &&
                <p>
                    Please wait
                </p>}
                <button type="submit">
                    Sign Up
                </button>
            </form>
        </main>
    )
}

export default NewUser;