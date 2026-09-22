import { Navigate, Outlet, useLocation } from 'react-router-dom'
import { inicioDaSessao, rotaBloqueada } from './portal'
import { useAuth } from './AuthContext'

export function RequireAuth() {
  const { status, mustChangePassword, links } = useAuth()
  const location = useLocation()

  if (status === 'loading') {
    return (
      <div className="page" aria-busy="true">
        <p className="muted">Verificando sessão…</p>
      </div>
    )
  }
  if (status === 'anonymous') {
    const token = new URLSearchParams(location.search).get('token')
    const deliberarComToken =
      Boolean(token) && /\/solicitacoes\/[^/]+\/deliberar$/.test(location.pathname)
    if (!deliberarComToken) {
      return (
        <Navigate
          to="/login"
          replace
          state={{ from: `${location.pathname}${location.search}` }}
        />
      )
    }
  }
  if (mustChangePassword && location.pathname !== '/primeiro-acesso') {
    return <Navigate to="/primeiro-acesso" replace />
  }
  if (!mustChangePassword && location.pathname === '/primeiro-acesso') {
    return <Navigate to={inicioDaSessao(links)} replace />
  }
  return <Outlet />
}

export function PortalGate() {
  const { links, mustChangePassword } = useAuth()
  const location = useLocation()
  if (!mustChangePassword && rotaBloqueada(location.pathname, links)) {
    return <Navigate to="/erro/403" replace />
  }
  return <Outlet />
}

export function RedirectIfAuthenticated() {
  const { status, mustChangePassword, links } = useAuth()
  if (status === 'loading') {
    return (
      <div className="page" aria-busy="true">
        <p className="muted">Carregando…</p>
      </div>
    )
  }
  if (status === 'authenticated') {
    return <Navigate to={mustChangePassword ? '/primeiro-acesso' : inicioDaSessao(links)} replace />
  }
  return <Outlet />
}
