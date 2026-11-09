import { useEffect, useRef, useState } from 'react'
import { NavLink, Outlet, useLocation } from 'react-router-dom'
import { useAuth } from '../auth/AuthContext'
import { CommandPalette } from '../components/CommandPalette'
import { useActions } from '../hooks/useActions'

type NavItem = { rel: string; label: string }

type NavGrupo = {
  id: string
  /** Sem título = itens soltos no topo (ex.: Início). */
  titulo?: string
  itens: NavItem[]
}

const NAV_GRUPOS: NavGrupo[] = [
  {
    id: 'principal',
    itens: [
      { rel: 'inicio', label: 'Início' },
      { rel: 'egresso-inicio', label: 'Início' },
    ],
  },
  {
    id: 'academico',
    titulo: 'Acadêmico',
    itens: [
      { rel: 'certificados', label: 'Certificados' },
      { rel: 'estagios', label: 'Estágios' },
      { rel: 'eventos', label: 'Eventos' },
      { rel: 'formativas', label: 'Formativas' },
      { rel: 'solicitacoes', label: 'Solicitações' },
      { rel: 'tccs', label: 'TCCs' },
    ],
  },
  {
    id: 'comunicacao',
    titulo: 'Comunicação',
    itens: [
      { rel: 'comunicacao', label: 'Comunicação' },
      { rel: 'meus-atendimentos', label: 'Meus atendimentos' },
      { rel: 'publicar-comunicado', label: 'Publicar comunicado' },
    ],
  },
  {
    id: 'atendimentos',
    titulo: 'Atendimentos',
    itens: [
      { rel: 'atendimentos', label: 'Atendimentos presenciais' },
      { rel: 'autorizacoes-imagem', label: 'Autorizações de imagem' },
      { rel: 'deliberar', label: 'Solicitações' },
      { rel: 'fila-solicitacoes', label: 'Fila de solicitações' },
      { rel: 'nova-interna', label: 'Nova interna' },
      { rel: 'atrasados', label: 'Solicitações atrasadas' },
    ],
  },
  {
    id: 'trabalho',
    titulo: 'Trabalho',
    itens: [
      { rel: 'eventos-professor', label: 'Eventos' },
      { rel: 'comissoes-caaf', label: 'Pool CAAF' },
      { rel: 'comissoes-coe', label: 'Atribuição de estágios' },
      { rel: 'revisao-caaf', label: 'Revisão CAAF' },
      { rel: 'estagios-revisao', label: 'Revisão de estágios' },
      { rel: 'tccs-revisao', label: 'Revisão de TCCs' },
    ],
  },
  {
    id: 'gestao',
    titulo: 'Gestão',
    itens: [
      { rel: 'alunos', label: 'Alunos' },
      { rel: 'estagios-secretaria', label: 'Cadastro de estágios' },
      { rel: 'tccs-secretaria', label: 'Cadastro de TCCs' },
      { rel: 'calendarios', label: 'Calendário acadêmico' },
      { rel: 'cursos', label: 'Cursos' },
      { rel: 'diplomas', label: 'Diplomas' },
      { rel: 'disciplinas', label: 'Disciplinas' },
      { rel: 'egressos', label: 'Egressos' },
      { rel: 'estatisticas', label: 'Estatísticas' },
      { rel: 'eventos-secretaria', label: 'Eventos' },
      { rel: 'exportacoes', label: 'Exportações' },
      { rel: 'importacoes', label: 'Importações' },
    ],
  },
  {
    id: 'coordenacao',
    titulo: 'Coordenação',
    itens: [
      { rel: 'configurar-curso', label: 'Configurar curso' },
      { rel: 'relatorios', label: 'Relatórios' },
    ],
  },
  {
    id: 'administracao',
    titulo: 'Administração',
    itens: [
      { rel: 'audit-log', label: 'Auditoria' },
      { rel: 'autoridades', label: 'Autoridades' },
      { rel: 'jobs', label: 'Jobs' },
      { rel: 'perfis', label: 'Perfis' },
      { rel: 'templates-comunicacao', label: 'Templates' },
      { rel: 'tipos-solicitacao', label: 'Tipos de solicitação' },
      { rel: 'usuarios', label: 'Usuários' },
    ],
  },
  {
    id: 'configuracoes',
    titulo: 'Configurações',
    itens: [
      { rel: 'contato', label: 'Contato' },
      { rel: 'perfil-notificacoes', label: 'Notificações' },
      { rel: 'perfil', label: 'Perfil' },
      { rel: 'perfil-seguranca', label: 'Segurança' },
      { rel: 'suporte', label: 'Suporte' },
    ],
  },
]

/**
 * Pontuação de match path+query. 0 = não casa.
 * Query mais específica vence (ex.: /solicitacoes?to=me > /solicitacoes),
 * evitando abrir Gestão ao ir em Atendimentos → Solicitações (?to=me).
 */
function pontuacaoHref(href: string, pathname: string, search: string): number {
  const [path, query = ''] = href.split('?')
  if (path !== pathname) return 0
  if (!query) return 1
  const desejados = new URLSearchParams(query)
  const atuais = new URLSearchParams(search)
  for (const [chave, valor] of desejados) {
    if (atuais.get(chave) !== valor) return 0
  }
  return 1 + [...desejados.keys()].length
}

export function AppLayout() {
  const { mustChangePassword, logout, status, links } = useAuth()
  const actions = useActions(links)
  const location = useLocation()
  const home = actions.href('egresso-inicio') ?? actions.href('inicio') ?? '/inicio'
  const [paleta, setPaleta] = useState(false)
  const [menuAberto, setMenuAberto] = useState(false)
  const [gruposAbertos, setGruposAbertos] = useState<Record<string, boolean>>({})
  const focoAnterior = useRef<HTMLElement | null>(null)

  const gruposVisiveis = NAV_GRUPOS.map((grupo) => ({
    ...grupo,
    itens: grupo.itens
      .filter((item) => actions.can(item.rel))
      .sort((a, b) => a.label.localeCompare(b.label, 'pt-BR')),
  })).filter((grupo) => grupo.itens.length > 0)

  const matchesNav = gruposVisiveis.flatMap((grupo) =>
    grupo.itens.flatMap((item) => {
      const href = actions.href(item.rel)
      if (!href) return []
      const score = pontuacaoHref(href, location.pathname, location.search)
      return score > 0 ? [{ grupoId: grupo.id, rel: item.rel, score }] : []
    }),
  )
  const melhorScore = matchesNav.reduce((max, m) => Math.max(max, m.score), 0)
  const relsAtivos = new Set(
    melhorScore > 0 ? matchesNav.filter((m) => m.score === melhorScore).map((m) => m.rel) : [],
  )
  const gruposComRotaAtiva = new Set(
    melhorScore > 0 ? matchesNav.filter((m) => m.score === melhorScore).map((m) => m.grupoId) : [],
  )
  const idsGruposAtivosKey = [...gruposComRotaAtiva].sort().join('|')

  // Mantém aberto só o grupo do match mais específico (não fecha os que o usuário abriu).
  useEffect(() => {
    if (!idsGruposAtivosKey) return
    const ids = idsGruposAtivosKey.split('|')

    setGruposAbertos((atual) => {
      let mudou = false
      const proximo = { ...atual }
      for (const id of ids) {
        if (!proximo[id]) {
          proximo[id] = true
          mudou = true
        }
      }
      return mudou ? proximo : atual
    })
  }, [idsGruposAtivosKey])

  useEffect(() => {
    function atalho(event: KeyboardEvent) {
      if ((event.ctrlKey || event.metaKey) && event.key.toLowerCase() === 'k') {
        event.preventDefault()
        focoAnterior.current = document.activeElement as HTMLElement
        setPaleta(true)
      }
    }
    window.addEventListener('keydown', atalho)
    return () => window.removeEventListener('keydown', atalho)
  }, [])

  function abrirPaleta() {
    focoAnterior.current = document.activeElement as HTMLElement
    setPaleta(true)
  }

  function fecharPaleta() {
    setPaleta(false)
    focoAnterior.current?.focus()
  }

  function alternarGrupo(id: string) {
    setGruposAbertos((atual) => ({ ...atual, [id]: !atual[id] }))
  }

  return (
    <div className={`shell${menuAberto ? ' shell-nav-open' : ''}`}>
      <aside className="app-sidebar" aria-label="Navegação principal">
        <NavLink
          to={mustChangePassword ? '/primeiro-acesso' : home}
          className="app-sidebar-brand"
          aria-label="SO2 · SEPT/UFPR"
          onClick={() => setMenuAberto(false)}
        >
          <span className="app-sidebar-mark" aria-hidden="true">
            S2
          </span>
          <span className="app-sidebar-title">
            Secretaria Online
            <small>SEPT / UFPR</small>
          </span>
        </NavLink>

        {!mustChangePassword && (
          <nav id="app-sidebar-nav" className="app-sidebar-nav">
            {gruposVisiveis.map((grupo) => {
              if (!grupo.titulo) {
                return (
                  <div key={grupo.id} className="app-sidebar-group">
                    <div className="app-sidebar-group-items">
                      {grupo.itens.map((item) => (
                        <NavLink
                          key={item.rel}
                          to={actions.href(item.rel) ?? '/'}
                          end
                          // Função: evita o `active` automático do RR (prefixo / query).
                          className={() => (relsAtivos.has(item.rel) ? 'active' : undefined)}
                          aria-current={relsAtivos.has(item.rel) ? 'page' : undefined}
                          onClick={() => setMenuAberto(false)}
                        >
                          {item.label}
                        </NavLink>
                      ))}
                    </div>
                  </div>
                )
              }

              const aberto = !!gruposAbertos[grupo.id]
              const painelId = `nav-grupo-painel-${grupo.id}`
              const temAtivo = gruposComRotaAtiva.has(grupo.id)

              return (
                <div
                  key={grupo.id}
                  className={`app-sidebar-group${temAtivo ? ' is-active-group' : ''}${aberto ? ' is-open' : ''}`}
                >
                  <button
                    type="button"
                    className="app-sidebar-group-toggle"
                    aria-expanded={aberto}
                    aria-controls={painelId}
                    onClick={() => alternarGrupo(grupo.id)}
                  >
                    <span>{grupo.titulo}</span>
                    <span className="app-sidebar-group-chevron" aria-hidden="true" />
                  </button>
                  <div
                    id={painelId}
                    className="app-sidebar-group-items"
                    role="group"
                    aria-label={grupo.titulo}
                    hidden={!aberto}
                  >
                    {grupo.itens.map((item) => (
                      <NavLink
                        key={item.rel}
                        to={actions.href(item.rel) ?? '/'}
                        end
                        className={() => (relsAtivos.has(item.rel) ? 'active' : undefined)}
                        aria-current={relsAtivos.has(item.rel) ? 'page' : undefined}
                        onClick={() => setMenuAberto(false)}
                        tabIndex={aberto ? undefined : -1}
                      >
                        {item.label}
                      </NavLink>
                    ))}
                  </div>
                </div>
              )
            })}
          </nav>
        )}

        {status === 'authenticated' && (
          <div className="app-sidebar-foot">
            <button type="button" className="ghost light" onClick={() => void logout()}>
              Sair
            </button>
          </div>
        )}
      </aside>

      {menuAberto && (
        <button
          type="button"
          className="app-sidebar-backdrop"
          aria-label="Fechar menu"
          onClick={() => setMenuAberto(false)}
        />
      )}

      <div className="app-main">
        {!mustChangePassword && (
          <header className="app-topbar">
            <div className="app-topbar-side">
              <button
                type="button"
                className="app-menu-toggle ghost"
                aria-expanded={menuAberto}
                aria-controls="app-sidebar-nav"
                onClick={() => setMenuAberto((aberto) => !aberto)}
              >
                Menu
              </button>
            </div>
            {status === 'authenticated' && (
              <input
                type="search"
                className="topbar-busca"
                placeholder="Buscar protocolo, evento… (Ctrl+K)"
                aria-label="Abrir busca global"
                readOnly
                onFocus={abrirPaleta}
              />
            )}
            <div className="app-topbar-side" aria-hidden="true" />
          </header>
        )}
        <main className="content">
          <Outlet />
        </main>
      </div>

      {!mustChangePassword && <CommandPalette aberto={paleta} onFechar={fecharPaleta} />}
    </div>
  )
}
