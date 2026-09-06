import { NavLink, Outlet } from 'react-router-dom'

export function AppLayout() {
  return (
    <div className="shell">
      <header className="topbar">
        <NavLink to="/inicio" className="brand">
          SO2 · SEPT/UFPR
        </NavLink>
        <nav>
          <NavLink to="/inicio">Início</NavLink>
          <NavLink to="/secretaria/cursos">Cursos</NavLink>
          <NavLink to="/secretaria/disciplinas">Disciplinas</NavLink>
          <NavLink to="/secretaria/alunos">Alunos</NavLink>
          <NavLink to="/secretaria/calendarios">Calendários</NavLink>
          <NavLink to="/contato">Contato</NavLink>
        </nav>
      </header>
      <main className="content">
        <Outlet />
      </main>
    </div>
  )
}
