import type React from "react";
import { useState } from "react";
import { useNavigate } from "react-router-dom";
import { conditionAdv } from "../../../utils/advs";
import { resetStrings } from "../../../utils/evaluate_fields";

function SignIn() {
    const navigate = useNavigate();
    const URL = import.meta.env.VITE_API_URL;

    const [email, setEmail] = useState("");
    const [password, setPassword] = useState("");

    const [advInconsistence, setAdvInconsistence] = useState(false);

    const verifyUser = (e: React.FormEvent<HTMLFormElement>) => {
        e.preventDefault();
        if (conditionAdv(password.length == 0, setAdvInconsistence)) return;
        resetStrings(setEmail, setPassword);
        navigate("/me");
    }

    return (
        <main>
            <header>
                <h1>
                    Sign In
                </h1>
            </header>
            <form onSubmit={verifyUser}>
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
                    <label htmlFor="password-field">
                        Enter your password
                    </label>
                    <input
                        type="password"
                        id="password-field"
                        value={password}
                        onChange={(e) => setPassword(e.target.value)}
                        placeholder="enter the password"
                    />
                </div>
                {advInconsistence && 
                <p>
                    Invalid Credentials
                </p>}
                <button type="submit">
                    Sign In
                </button>
            </form>
        </main>
    )
}

export default SignIn;