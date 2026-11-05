import { Route, Routes } from "react-router-dom";
import Login from "./pages/Login";
import SignIn from "./pages/auth/identification/SignIn";
import VerificationToNew from "./pages/auth/identification/VerificationToNew";
import NewUser from "./pages/auth/data/NewUser";
import NewAccount from "./pages/auth/data/NewAccount";
import SelectAccount from "./pages/auth/SelectAccount";
import Me from "./pages/account/Me";

function App() {
  return (
    <Routes>
      <Route
        path="/"
        element={<Login />}
      />

      {/*Identification*/}
      <Route
        path="/sign-in"
        element={<SignIn />}
      />
      <Route
        path="/register-user"
        element={<VerificationToNew />}
      />

      {/*data*/}
      <Route
        path="/create-user"
        element={<NewUser />}
      />
      <Route
        path="/create-new-account"
        element={<NewAccount />}
      />

      {/*account*/}
      <Route
        path="/select-account"
        element={<SelectAccount />}
      />
      <Route
        path="/me"
        element={<Me />}
      />
    </Routes>
  )
}

export default App;