import { useState } from 'react'
import { Link, useLocation } from 'react-router-dom'
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { ApiError } from '../../api/client'
import { comunicacoesApi, type FiltroComunicacao } from '../../api/comunicacoes'
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

export function Comunicacao() {
  const location = useLocation()
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
        <div>
          <h1>Comunicação</h1>
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

      <div className="grid">
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
      {lista.data && lista.data.content.length === 0 && (
        <p className="empty" role="status">
          Nenhuma comunicação encontrada.
        </p>
      )}
      {lista.data && lista.data.content.length > 0 && (
        <ul className="comunicacao-lista">
          {lista.data.content.map((item) => (
            <Linha key={item.id} item={item} />
          ))}
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
          {ROTULO_TIPO[item.tipo].slice(0, 1)}
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
