import { Link, Outlet } from 'react-router-dom'

export function AuthLayout() {
  return (
    <div className="auth-shell">
      <header className="auth-brand">
        <Link to="/login">SO2 · SEPT/UFPR</Link>
      </header>
      <main className="auth-card">
        <Outlet />
      </main>
      <footer className="auth-footer">
        <Link to="/contato">Contato da secretaria</Link>
      </footer>
    </div>
  )
}
