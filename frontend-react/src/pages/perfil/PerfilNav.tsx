import { NavLink } from 'react-router-dom'
import { useAuth } from '../../auth/AuthContext'
import { useActions } from '../../hooks/useActions'

const ITENS = [
  { rel: 'perfil', label: 'Dados' },
  { rel: 'perfil-seguranca', label: 'Segurança' },
  { rel: 'perfil-notificacoes', label: 'Notificações' },
]

export function PerfilNav() {
  const { links } = useAuth()
  const actions = useActions(links)
  const visiveis = ITENS.filter((item) => actions.can(item.rel))
  if (visiveis.length === 0) {
    return null
  }
  return (
    <nav className="subnav" aria-label="Seções do perfil">
      {visiveis.map((item) => (
        <NavLink key={item.rel} to={actions.href(item.rel) ?? '/'}>
          {item.label}
        </NavLink>
      ))}
    </nav>
  )
}
