import { useState } from 'react'
import { Link, useLocation } from 'react-router-dom'
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { ApiError } from '../../api/client'
import { comunicacoesApi, type FiltroComunicacao } from '../../api/comunicacoes'
import { useAuth } from '../../auth/AuthContext'
import { useActions } from '../../hooks/useActions'
import type { BadgesComunicacao, ComunicacaoItem, TipoComunicacao } from '../../models/comunicacao'
import { MarkdownPreview } from './MarkdownPreview'

const ABAS: { id: string; label: string; badge: keyof BadgesComunicacao }[] = [
  { id: 'TODOS', label: 'Todos', badge: 'todos' },
  { id: 'INSTITUCIONAL', label: 'Institucional', badge: 'institucional' },
  { id: 'TURMA', label: 'Turma', badge: 'turma' },
  { id: 'INBOX', label: 'Inbox', badge: 'inbox' },
]

const ROTULO_TIPO: Record<TipoComunicacao, string> = {
  INSTITUCIONAL: 'Institucional',
  TURMA: 'Turma',
  INBOX: 'Inbox',
}

function IconeTipo({ tipo }: { tipo: TipoComunicacao }) {
  if (tipo === 'INSTITUCIONAL') {
    return (
      <svg viewBox="0 0 24 24" width="16" height="16" fill="none" stroke="currentColor" strokeWidth="2" aria-hidden="true">
        <path d="M3 21h18" />
        <path d="M5 21V7l7-4 7 4v14" />
        <path d="M9 21v-6h6v6" />
        <path d="M9 10h.01M15 10h.01M9 14h.01M15 14h.01" />
      </svg>
    )
  }
  if (tipo === 'TURMA') {
    return (
      <svg viewBox="0 0 24 24" width="16" height="16" fill="none" stroke="currentColor" strokeWidth="2" aria-hidden="true">
        <path d="M16 21v-2a4 4 0 0 0-4-4H6a4 4 0 0 0-4 4v2" />
        <circle cx="9" cy="7" r="4" />
        <path d="M22 21v-2a4 4 0 0 0-3-3.87" />
        <path d="M16 3.13a4 4 0 0 1 0 7.75" />
      </svg>
    )
  }
  return (
    <svg viewBox="0 0 24 24" width="16" height="16" fill="none" stroke="currentColor" strokeWidth="2" aria-hidden="true">
      <path d="M22 12H2" />
      <path d="M5.45 5.11 2 12v6a2 2 0 0 0 2 2h16a2 2 0 0 0 2-2v-6l-3.45-6.89A2 2 0 0 0 16.76 4H7.24a2 2 0 0 0-1.79 1.11Z" />
      <path d="M6 16h.01M10 16h4" />
    </svg>
  )
}

export function Comunicacao() {
  const location = useLocation()
  const { links } = useAuth()
  const actions = useActions(links)
  const aviso = (location.state as { aviso?: string } | null)?.aviso
  const [aba, setAba] = useState('TODOS')
  const [lido, setLido] = useState('')
  const [tipo, setTipo] = useState('')
  const filtro: FiltroComunicacao = { aba, lido: lido || undefined, tipo: tipo || undefined }
  const lista = useQuery({
    queryKey: ['communications', filtro],
    queryFn: () => comunicacoesApi.listar(filtro),
    refetchInterval: 60_000,
  })

  return (
    <section className="page">
      <header className="page-head">
        <div className="page-head-lead">
          <div className="page-head-title-row">
            <h1>Comunicação</h1>
            {actions.can('publicar-comunicado') && (
              <div className="dashboard-atalhos">
                <Link
                  to={actions.href('publicar-comunicado') ?? '/comunicacao/publicar'}
                  className="button-link"
                >
                  Publicar comunicado
                </Link>
              </div>
            )}
          </div>
          <p className="muted">Avisos institucionais, mensagens da turma e inbox de ações.</p>
        </div>
      </header>

      {aviso && (
        <div className="banner success" role="status">
          {aviso}
        </div>
      )}

      <div className="tabs tabs-scroll" role="tablist" aria-label="Tipos de comunicação">
        {ABAS.map((item) => (
          <button
            key={item.id}
            type="button"
            role="tab"
            aria-selected={aba === item.id}
            onClick={() => setAba(item.id)}
          >
            {item.label}
            <span className="tab-badge">{lista.data?.badges[item.badge] ?? 0}</span>
          </button>
        ))}
      </div>

      <div className="filter-bar filter-bar-compact">
        <label>
          Lido
          <select value={lido} onChange={(event) => setLido(event.target.value)}>
            <option value="">Todos</option>
            <option value="false">Não lido</option>
            <option value="true">Lido</option>
          </select>
        </label>
        <label>
          Tipo
          <select value={tipo} onChange={(event) => setTipo(event.target.value)}>
            <option value="">Todos</option>
            <option value="INSTITUCIONAL">Institucional</option>
            <option value="TURMA">Turma</option>
            <option value="INBOX">Inbox</option>
          </select>
        </label>
      </div>

      {lista.isLoading && (
        <p className="muted" aria-busy="true">
          Carregando comunicações…
        </p>
      )}
      {lista.isError && (
        <div className="banner danger" role="alert">
          {lista.error instanceof ApiError && lista.error.status === 403
            ? 'Você não tem permissão para ver as comunicações.'
            : 'Não foi possível carregar as comunicações.'}{' '}
          <button type="button" onClick={() => lista.refetch()}>
            Tentar de novo
          </button>
        </div>
      )}
      {lista.data && (
        <ul className="comunicacao-lista">
          {lista.data.content.length === 0 ? (
            <li className="comunicacao-item tabela-vazio" role="status">
              Nenhuma comunicação encontrada.
            </li>
          ) : (
            lista.data.content.map((item) => <Linha key={item.id} item={item} />)
          )}
        </ul>
      )}
    </section>
  )
}

function Linha({ item }: { item: ComunicacaoItem }) {
  const queryClient = useQueryClient()
  const [aberto, setAberto] = useState(false)
  const [lidaLocal, setLidaLocal] = useState(item.lida)
  const [erro, setErro] = useState<string | null>(null)
  const links = lidaLocal ? semMarcar(item._links) : item._links
  const actions = useActions(links)
  const marcar = useMutation({
    mutationFn: (href: string) => comunicacoesApi.marcarLida(href),
    onSuccess: () => {
      void queryClient.invalidateQueries({ queryKey: ['communications'] })
    },
  })

  async function abrir() {
    setAberto((valor) => !valor)
    const href = actions.href('marcar-lido')
    if (!href) {
      return
    }
    setLidaLocal(true)
    setErro(null)
    try {
      await marcar.mutateAsync(href)
    } catch {
      setLidaLocal(false)
      setErro('Não foi possível marcar como lida.')
    }
  }

  const hrefAcao = actions.href('acao')
  const data = new Date(item.data).toLocaleString('pt-BR')

  return (
    <li className={lidaLocal ? 'comunicacao-item' : 'comunicacao-item nao-lida'}>
      <button type="button" className="comunicacao-row" aria-expanded={aberto} onClick={() => void abrir()}>
        <span className="tipo-icone" aria-hidden="true">
          <IconeTipo tipo={item.tipo} />
        </span>
        <span className="sr-only">{ROTULO_TIPO[item.tipo]}</span>
        <span className="comunicacao-titulo">{item.titulo}</span>
        <time dateTime={item.data}>{data}</time>
        <span className={`badge prioridade-${item.prioridade.toLowerCase()}`}>{item.prioridade}</span>
        {!lidaLocal && <span className="unread-dot" aria-hidden="true" />}
      </button>
      {aberto && <MarkdownPreview texto={item.corpo} />}
      {hrefAcao && (
        <Link className={item.tipo === 'INBOX' ? 'cta-pulsante' : 'cta-acao'} to={hrefAcao}>
          Ver ação
        </Link>
      )}
      {erro && (
        <p className="field-error" role="alert">
          {erro}
        </p>
      )}
    </li>
  )
}

function semMarcar(links?: Record<string, string>) {
  if (!links) {
    return links
  }
  const copia = { ...links }
  delete copia['marcar-lido']
  return copia
}
