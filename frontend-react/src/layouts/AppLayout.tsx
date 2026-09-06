import { NavLink, Outlet } from 'react-router-dom'
import { useAuth } from '../auth/AuthContext'

export function AppLayout() {
  const { mustChangePassword, logout, status } = useAuth()

  return (
    <div className="shell">
      <header className="topbar">
        <NavLink to={mustChangePassword ? '/primeiro-acesso' : '/inicio'} className="brand">
          SO2 · SEPT/UFPR
        </NavLink>
        {!mustChangePassword && (
          <nav>
            <NavLink to="/inicio">Início</NavLink>
            <NavLink to="/secretaria/cursos">Cursos</NavLink>
            <NavLink to="/secretaria/disciplinas">Disciplinas</NavLink>
            <NavLink to="/secretaria/alunos">Alunos</NavLink>
            <NavLink to="/secretaria/calendarios">Calendários</NavLink>
            <NavLink to="/contato">Contato</NavLink>
          </nav>
        )}
        {status === 'authenticated' && (
          <button type="button" className="ghost light" onClick={() => void logout()}>
            Sair
          </button>
        )}
      </header>
      <main className="content">
        <Outlet />
      </main>
    </div>
  )
}
