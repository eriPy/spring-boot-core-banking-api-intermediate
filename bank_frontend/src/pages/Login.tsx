import { Link } from "react-router-dom";

function Login() {
    return (
        <main>
            <header>
                <h1>
                    Access
                </h1>
            </header>
            <div>
                <Link to="/sign-in">
                    <p>
                        Sign In
                    </p>
                </Link>
                <Link to="/register-user">
                    <p>
                        Register new user
                    </p>
                </Link>
            </div>
        </main>
    )
}

export default Login;