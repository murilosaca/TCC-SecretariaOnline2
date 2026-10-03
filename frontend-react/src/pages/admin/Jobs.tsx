import { useState } from 'react'
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { jobsApi, type OutboxEvento } from '../../api/jobs'
import { ApiError } from '../../api/client'
import { useActions } from '../../hooks/useActions'

const ABAS = ['PENDING', 'PROCESSING', 'SENT', 'FAILED', 'DEAD'] as const

export function Jobs() {
  const [status, setStatus] = useState<(typeof ABAS)[number]>('FAILED')
  const [tipo, setTipo] = useState('')
  const [filtro, setFiltro] = useState('')
  const client = useQueryClient()
  const lista = useQuery({
    queryKey: ['admin-outbox', status, filtro],
    queryFn: () => jobsApi.listar(status, filtro || undefined),
  })
  const reentregar = useMutation({
    mutationFn: (path: string) => jobsApi.reentregar(path),
    onSuccess: () => client.invalidateQueries({ queryKey: ['admin-outbox'] }),
  })

  return (
    <section className="page" aria-labelledby="jobs-titulo">
      <header className="page-head">
        <div>
          <h1 id="jobs-titulo">Jobs e Outbox</h1>
          <p className="muted">Reentrega só aparece quando a resposta traz `_links.retry`.</p>
        </div>
      </header>
      {lista.data?.alertaLatencia && (
        <div className="banner warning" role="status">
          {lista.data.alertaLatencia}
        </div>
      )}
      <div className="cards" aria-label="Jobs neste processo">
        {(lista.data?.jobs ?? []).map((job) => (
          <article key={job.nome} className="card">
            <h2>{job.nome}</h2>
            <p>Frequência: {job.frequencia}</p>
            <p>Último run: {job.ultimoRun ?? '—'}</p>
            <p>Próximo run: {job.proximoRun ?? '—'}</p>
            <p>
              <span className={badgeJob(job.status)}>{job.status}</span>
            </p>
          </article>
        ))}
      </div>
      <div className="tabs" role="tablist" aria-label="Status do Outbox">
        {ABAS.map((aba) => (
          <button
            key={aba}
            type="button"
            role="tab"
            aria-selected={status === aba}
            onClick={() => setStatus(aba)}
          >
            {aba}
          </button>
        ))}
      </div>
      <form
        className="filters"
        onSubmit={(event) => {
          event.preventDefault()
          setFiltro(tipo.trim())
        }}
      >
        <label htmlFor="outbox-tipo">Tipo</label>
        <input id="outbox-tipo" value={tipo} onChange={(event) => setTipo(event.target.value)} />
        <button type="submit">Filtrar</button>
      </form>
      {lista.isLoading && <p className="muted">Carregando eventos…</p>}
      {lista.isError && (
        <p className="empty" role="alert">
          {lista.error instanceof ApiError && lista.error.status === 403
            ? 'Você não tem permissão para observar o Outbox.'
            : 'Não foi possível carregar o Outbox.'}
        </p>
      )}
      {lista.data && lista.data.content.length === 0 && <p className="empty">Nenhum evento neste status.</p>}
      <table>
        <thead>
          <tr>
            <th scope="col">ID</th>
            <th scope="col">Tipo</th>
            <th scope="col">Payload</th>
            <th scope="col">Tentativas</th>
            <th scope="col">Criado em</th>
            <th scope="col">Ações</th>
          </tr>
        </thead>
        <tbody>
          {(lista.data?.content ?? []).map((evento) => (
            <Linha key={evento.id} evento={evento} onRetry={(path) => reentregar.mutate(path)} />
          ))}
        </tbody>
      </table>
    </section>
  )
}

function Linha({ evento, onRetry }: { evento: OutboxEvento; onRetry: (path: string) => void }) {
  const actions = useActions(evento._links)
  const danger = evento.status === 'FAILED'
  return (
    <tr className={danger ? 'danger' : undefined}>
      <td>{evento.id}</td>
      <td>{evento.tipo}</td>
      <td>{evento.payloadResumo}</td>
      <td>{evento.tentativas}</td>
      <td>{evento.createdAt}</td>
      <td>
        <span className={evento.status === 'DEAD' ? 'badge neutral' : danger ? 'badge danger' : 'badge'}>
          {evento.status}
        </span>
        {actions.can('retry') && (
          <button type="button" onClick={() => onRetry(actions.href('retry')!)}>
            Reentregar
          </button>
        )}
      </td>
    </tr>
  )
}

function badgeJob(status: string): string {
  if (status === 'FALHOU') return 'badge danger'
  if (status === 'ATRASADO') return 'badge warning'
  return 'badge'
}
