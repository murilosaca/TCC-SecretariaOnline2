import { FormEvent, useEffect, useState } from 'react'
import { useQuery } from '@tanstack/react-query'
import { ApiError } from '../../api/client'
import { auditApi, type AuditLogItem } from '../../api/audit'

export function AuditLog() {
  const [ator, setAtor] = useState('')
  const [acao, setAcao] = useState('')
  const [de, setDe] = useState('')
  const [ate, setAte] = useState('')
  const [filtros, setFiltros] = useState({ ator: '', acao: '', de: '', ate: '' })
  const [aberto, setAberto] = useState<AuditLogItem | null>(null)

  const lista = useQuery({
    queryKey: ['audit-log', filtros],
    queryFn: () =>
      auditApi.listar({
        ator: filtros.ator || undefined,
        acao: filtros.acao || undefined,
        de: filtros.de || undefined,
        ate: filtros.ate || undefined,
        size: 50,
      }),
  })
  const forbidden = lista.isError && lista.error instanceof ApiError && lista.error.status === 403

  useEffect(() => {
    if (!aberto) return
    function fechar(event: KeyboardEvent) {
      if (event.key === 'Escape') setAberto(null)
    }
    window.addEventListener('keydown', fechar)
    return () => window.removeEventListener('keydown', fechar)
  }, [aberto])

  function aplicar(event: FormEvent) {
    event.preventDefault()
    setFiltros({ ator, acao, de, ate })
  }

  return (
    <section className="page" aria-labelledby="audit-titulo">
      <header className="page-head">
        <div>
          <h1 id="audit-titulo">Trilha de auditoria</h1>
          <p className="muted">Somente leitura. O intervalo padrão é o último ano.</p>
        </div>
      </header>
      <form className="filters" onSubmit={aplicar}>
        <label htmlFor="audit-ator">Ator</label>
        <input id="audit-ator" value={ator} onChange={(event) => setAtor(event.target.value)} />
        <label htmlFor="audit-acao">Ação</label>
        <input id="audit-acao" value={acao} onChange={(event) => setAcao(event.target.value)} />
        <label htmlFor="audit-de">De</label>
        <input id="audit-de" type="date" value={de} onChange={(event) => setDe(event.target.value)} />
        <label htmlFor="audit-ate">Até</label>
        <input id="audit-ate" type="date" value={ate} onChange={(event) => setAte(event.target.value)} />
        <button type="submit">Filtrar</button>
      </form>
      {forbidden && <p className="empty">Trilha indisponível para esta sessão.</p>}
      {lista.isLoading && <p className="muted">Carregando trilha…</p>}
      <table>
        <thead>
          <tr>
            <th>Quando</th>
            <th>Ator</th>
            <th>Ação</th>
            <th>Entidade</th>
          </tr>
        </thead>
        <tbody>
          {(lista.data?.content ?? []).map((item) => (
            <tr key={item.id}>
              <td>{item.timestamp}</td>
              <td>{item.atorNome ?? '—'}</td>
              <td>{item.acao}</td>
              <td>
                <button type="button" onClick={() => setAberto(item)}>
                  Ver diff
                </button>
              </td>
            </tr>
          ))}
        </tbody>
      </table>
      {aberto && (
        <aside className="drawer" role="dialog" aria-labelledby="diff-titulo">
          <h2 id="diff-titulo">Diff</h2>
          <p>
            <strong>Antes</strong>
          </p>
          <pre>{aberto.payloadAntes ?? '—'}</pre>
          <p>
            <strong>Depois</strong>
          </p>
          <pre>{aberto.payloadDepois ?? '—'}</pre>
          <button type="button" onClick={() => setAberto(null)}>
            Fechar
          </button>
        </aside>
      )}
    </section>
  )
}
