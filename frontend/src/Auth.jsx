
import { useState } from "react";
import { api } from "./api";
import "./Auth.css";

export default function Auth({ onLogin }) {
  const [mode, setMode] = useState("login");
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [confirmPassword, setConfirmPassword] = useState("");
  const [error, setError] = useState("");
  const [loading, setLoading] = useState(false);

  async function handleSubmit(event) {
    event.preventDefault();
    setError("");

    if (mode === "register" && password !== confirmPassword) {
      setError("Your passwords do not match.");
      return;
    }

    if (password.length < 8) {
      setError("Password must contain at least 8 characters.");
      return;
    }

    setLoading(true);

    try {
      if (mode === "register") {
        await api.register(email.trim(), password);
      }

      const result = await api.login(email.trim(), password);

      if (!result?.accessToken) {
        throw new Error("Login succeeded without an access token.");
      }

      onLogin(result);
    } catch (err) {
      setError(
        err.message ||
          (mode === "register"
            ? "Unable to create your account."
            : "Unable to log in.")
      );
    } finally {
      setLoading(false);
    }
  }

  function switchMode(nextMode) {
    setMode(nextMode);
    setError("");
    setConfirmPassword("");
  }

  return (
    <main className="auth-page">
      <div className="auth-glow auth-glow-one" />
      <div className="auth-glow auth-glow-two" />

      <section className="auth-card">
        <a className="auth-brand" href="#home">
          <span className="auth-logo">F</span>
          <span>focus<span>forge</span></span>
        </a>

        <div className="auth-kicker">
          <span /> YOUR SPACE TO FOCUS
        </div>

        <h1>{mode === "login" ? "Welcome back." : "Start building."}</h1>
        <p className="auth-description">
          {mode === "login"
            ? "Your goals are waiting. Let's get back to them."
            : "Create your account and make room for what matters."}
        </p>

        <div className="auth-tabs">
          <button
            type="button"
            className={mode === "login" ? "auth-tab active" : "auth-tab"}
            onClick={() => switchMode("login")}
          >
            Log in
          </button>
          <button
            type="button"
            className={mode === "register" ? "auth-tab active" : "auth-tab"}
            onClick={() => switchMode("register")}
          >
            Create account
          </button>
        </div>

        <form className="auth-form" onSubmit={handleSubmit}>
          <label htmlFor="auth-email">Email address</label>
          <input
            id="auth-email"
            type="email"
            autoComplete="email"
            placeholder="you@example.com"
            value={email}
            onChange={(event) => setEmail(event.target.value)}
            required
          />

          <label htmlFor="auth-password">Password</label>
          <input
            id="auth-password"
            type="password"
            autoComplete={
              mode === "login" ? "current-password" : "new-password"
            }
            placeholder="At least 8 characters"
            value={password}
            onChange={(event) => setPassword(event.target.value)}
            minLength={8}
            maxLength={72}
            required
          />

          {mode === "register" && (
            <>
              <label htmlFor="auth-confirm">Confirm password</label>
              <input
                id="auth-confirm"
                type="password"
                autoComplete="new-password"
                placeholder="Enter your password again"
                value={confirmPassword}
                onChange={(event) => setConfirmPassword(event.target.value)}
                minLength={8}
                maxLength={72}
                required
              />
            </>
          )}

          {error && (
            <div className="auth-error" role="alert">
              <span>!</span> {error}
            </div>
          )}

          <button className="auth-submit" type="submit" disabled={loading}>
            {loading
              ? "Please wait..."
              : mode === "login"
                ? "Log in to FocusForge"
                : "Create my account"}
            {!loading && <span>↗</span>}
          </button>
        </form>

        <div className="auth-security">
          <span>◈</span>
          Your tasks stay private to your account.
        </div>

        <div className="auth-footer">
          <span>FOCUSFORGE / 01</span>
          <span>BUILT FOR WHAT MATTERS ✳</span>
        </div>
      </section>
    </main>
  );
}
