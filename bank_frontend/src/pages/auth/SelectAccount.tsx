import { Link, useNavigate } from "react-router-dom";

function SelectAccount() {
    const navigate = useNavigate();
    const URL = import.meta.env.VITE_API_URL;

    return (
        <main>
            <header>
                <h1>

                </h1>
            </header>
            <section>
    
            </section>
            <div>
                <Link to="/create-new-account">
                    <p>
                        Do you want to create another account?
                    </p>
                </Link>
            </div>
        </main>
    )
}

export default SelectAccount;