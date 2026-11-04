import { Link, Outlet } from 'react-router-dom'
import { LogoSept } from '../assets/brand/LogoSept'
import { LogoSo2 } from '../assets/brand/LogoSo2'
import { LogoUfpr } from '../assets/brand/LogoUfpr'

export function AuthLayout() {
  return (
    <div className="auth-shell">
      <div className="auth-stage">
        <div className="auth-flank" aria-hidden="false">
          <LogoUfpr className="auth-flank-logo" />
        </div>

        <main className="auth-card">
          <div className="auth-card-brand">
            <LogoSo2 className="auth-so2" />
            <p className="auth-product">Secretaria Online 2</p>
          </div>
          <Outlet />
        </main>

        <div className="auth-flank" aria-hidden="false">
          <LogoSept className="auth-flank-logo" />
        </div>
      </div>

      <footer className="auth-footer">
        <span>SEPT / UFPR</span>
        <span aria-hidden="true">·</span>
        <Link to="/contato">Contato</Link>
        <span aria-hidden="true">·</span>
        <Link to="/publico/verificar-protocolo">Verificar protocolo</Link>
        <span aria-hidden="true">·</span>
        <Link to="/publico/verificar-certificado/demo">Verificar certificado</Link>
      </footer>
    </div>
  )
}
