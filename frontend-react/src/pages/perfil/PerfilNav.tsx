import { NavLink } from 'react-router-dom'
import { useAuth } from '../../auth/AuthContext'
import { useActions } from '../../hooks/useActions'
import type { ReactNode } from 'react'

const ITENS: { rel: string; label: string; icone: ReactNode }[] = [
  {
    rel: 'perfil-notificacoes',
    label: 'Notificações',
    icone: (
      <svg viewBox="0 0 24 24" width="16" height="16" fill="none" stroke="currentColor" strokeWidth="2" aria-hidden="true">
        <path d="M6 8a6 6 0 0 1 12 0c0 7 3 9 3 9H3s3-2 3-9" />
        <path d="M10.3 21a1.94 1.94 0 0 0 3.4 0" />
      </svg>
    ),
  },
  {
    rel: 'perfil',
    label: 'Perfil',
    icone: (
      <svg viewBox="0 0 24 24" width="16" height="16" fill="none" stroke="currentColor" strokeWidth="2" aria-hidden="true">
        <path d="M19 21v-2a4 4 0 0 0-4-4H9a4 4 0 0 0-4 4v2" />
        <circle cx="12" cy="7" r="4" />
      </svg>
    ),
  },
  {
    rel: 'perfil-seguranca',
    label: 'Segurança',
    icone: (
      <svg viewBox="0 0 24 24" width="16" height="16" fill="none" stroke="currentColor" strokeWidth="2" aria-hidden="true">
        <rect x="3" y="11" width="18" height="11" rx="2" />
        <path d="M7 11V7a5 5 0 0 1 10 0v4" />
      </svg>
    ),
  },
]

export function PerfilNav() {
  const { links } = useAuth()
  const actions = useActions(links)
  const visiveis = ITENS.filter((item) => actions.can(item.rel))
  if (visiveis.length === 0) {
    return null
  }
  return (
    <nav className="perfil-subnav" aria-label="Seções do perfil">
      {visiveis.map((item) => (
        <NavLink
          key={item.rel}
          to={actions.href(item.rel) ?? '/'}
          className={({ isActive }) => (isActive ? 'perfil-subnav-btn is-active' : 'perfil-subnav-btn')}
          end={item.rel === 'perfil'}
        >
          <span className="perfil-subnav-icone">{item.icone}</span>
          {item.label}
        </NavLink>
      ))}
    </nav>
  )
}
