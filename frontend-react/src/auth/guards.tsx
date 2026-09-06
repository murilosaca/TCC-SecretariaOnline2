import { Navigate, Outlet, useLocation } from 'react-router-dom'
import { useAuth } from './AuthContext'

export function RequireAuth() {
  const { status, mustChangePassword } = useAuth()
  const location = useLocation()

  if (status === 'loading') {
    return (
      <div className="page" aria-busy="true">
        <p className="muted">Verificando sessão…</p>
      </div>
    )
  }
  if (status === 'anonymous') {
    return <Navigate to="/login" replace state={{ from: location.pathname }} />
  }
  if (mustChangePassword && location.pathname !== '/primeiro-acesso') {
    return <Navigate to="/primeiro-acesso" replace />
  }
  if (!mustChangePassword && location.pathname === '/primeiro-acesso') {
    return <Navigate to="/inicio" replace />
  }
  return <Outlet />
}

export function RedirectIfAuthenticated() {
  const { status, mustChangePassword } = useAuth()
  if (status === 'loading') {
    return (
      <div className="page" aria-busy="true">
        <p className="muted">Carregando…</p>
      </div>
    )
  }
  if (status === 'authenticated') {
    return <Navigate to={mustChangePassword ? '/primeiro-acesso' : '/inicio'} replace />
  }
  return <Outlet />
}
