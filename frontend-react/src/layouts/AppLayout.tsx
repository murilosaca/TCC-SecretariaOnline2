import { NavLink, Outlet } from 'react-router-dom'
import { useAuth } from '../auth/AuthContext'
import { useActions } from '../hooks/useActions'

const NAV_ITENS: { rel: string; label: string }[] = [
  { rel: 'inicio', label: 'Início' },
  { rel: 'egresso-inicio', label: 'Início' },
  { rel: 'solicitacoes', label: 'Solicitações' },
  { rel: 'deliberar', label: 'Deliberar' },
  { rel: 'eventos', label: 'Eventos' },
  { rel: 'formativas', label: 'Formativas' },
  { rel: 'certificados', label: 'Certificados' },
  { rel: 'estagios', label: 'Estágios' },
  { rel: 'estagios-revisao', label: 'Revisão de estágios' },
  { rel: 'comissoes-coe', label: 'Pool COE' },
  { rel: 'tccs', label: 'TCCs' },
  { rel: 'tccs-revisao', label: 'Revisão de TCCs' },
  { rel: 'revisao-caaf', label: 'Revisão CAAF' },
  { rel: 'eventos-professor', label: 'Eventos (prof.)' },
  { rel: 'cursos', label: 'Cursos' },
  { rel: 'configurar-curso', label: 'Configurar curso' },
  { rel: 'disciplinas', label: 'Disciplinas' },
  { rel: 'alunos', label: 'Alunos' },
  { rel: 'calendarios', label: 'Calendários' },
  { rel: 'contato', label: 'Contato' },
]

export function AppLayout() {
  const { mustChangePassword, logout, status, links } = useAuth()
  const actions = useActions(links)
  const home = actions.href('egresso-inicio') ?? actions.href('inicio') ?? '/inicio'

  return (
    <div className="shell">
      <header className="topbar">
        <NavLink to={mustChangePassword ? '/primeiro-acesso' : home} className="brand">
          SO2 · SEPT/UFPR
        </NavLink>
        {!mustChangePassword && (
          <nav>
            {NAV_ITENS.filter((item) => actions.can(item.rel)).map((item) => (
              <NavLink key={item.rel} to={actions.href(item.rel) ?? '/'}>
                {item.label}
              </NavLink>
            ))}
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
