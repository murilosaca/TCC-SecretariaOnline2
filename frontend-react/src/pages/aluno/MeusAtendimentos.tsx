import { useSearchParams } from 'react-router-dom'
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { atendimentosApi } from '../../api/atendimentos'
import { ApiError } from '../../api/client'
import { useActions } from '../../hooks/useActions'
import type { Atendimento } from '../../models/atendimento'

const STATUS_OPCOES = [
  { value: '', label: 'Todos' },
  { value: 'PENDENTE_CIENCIA', label: 'Pendente ciência' },
  { value: 'CIENCIA_DADA', label: 'Ciente' },
] as const

export function MeusAtendimentos() {
  const [params, setParams] = useSearchParams()
  const status = params.get('status') ?? ''
  const queryClient = useQueryClient()

  const lista = useQuery({
    queryKey: ['atendimentos', 'me', status || 'todos'],
    queryFn: () => atendimentosApi.listarMeus(status || undefined),
  })

  const ciencia = useMutation({
    mutationFn: (href: string) => atendimentosApi.darCiencia(href),
    onSuccess: () => queryClient.invalidateQueries({ queryKey: ['atendimentos', 'me'] }),
  })

  const forbidden = lista.isError && lista.error instanceof ApiError && lista.error.status === 403

  return (
    <section className="page">
      <header className="page-head">
        <div>
          <h1>Meus atendimentos</h1>
          <p className="muted">Atendimentos feitos presencialmente, registrados pela secretaria.</p>
        </div>
      </header>

      <div className="filter-bar filter-bar-compact">
        <label>
          Status
          <select
            value={status}
            aria-label="Filtrar por status"
            onChange={(event) => {
              const next = new URLSearchParams(params)
              if (event.target.value) {
                next.set('status', event.target.value)
              } else {
                next.delete('status')
              }
              setParams(next)
            }}
          >
            {STATUS_OPCOES.map((opcao) => (
              <option key={opcao.value || 'todos'} value={opcao.value}>
                {opcao.label}
              </option>
            ))}
          </select>
        </label>
      </div>

      {forbidden && (
        <p className="empty" role="status">
          Você não tem permissão para ver atendimentos.
        </p>
      )}
      {lista.isError && !forbidden && (
        <div className="banner danger" role="alert">
          Não foi possível carregar os atendimentos.{' '}
          <button type="button" onClick={() => lista.refetch()}>
            Tentar de novo
          </button>
        </div>
      )}
      {lista.isLoading && (
        <p className="muted" aria-busy="true">
          Carregando atendimentos…
        </p>
      )}
      {lista.data && (
        <table className="meus-atendimentos-tabela">
          <thead>
            <tr>
              <th scope="col">Data</th>
              <th scope="col">Assunto</th>
              <th scope="col">Status</th>
              <th scope="col">Ação</th>
            </tr>
          </thead>
          <tbody>
            {lista.data.content.length === 0 ? (
              <tr>
                <td colSpan={4} className="tabela-vazio" role="status">
                  {status === 'PENDENTE_CIENCIA'
                    ? 'Nenhum atendimento pendente.'
                    : 'Nenhum atendimento registrado.'}
                </td>
              </tr>
            ) : (
              lista.data.content.map((item) => (
                <Linha
                  key={item.id}
                  item={item}
                  onCiencia={(href) => ciencia.mutate(href)}
                  pending={ciencia.isPending}
                />
              ))
            )}
          </tbody>
        </table>
      )}
    </section>
  )
}

function Linha({
  item,
  onCiencia,
  pending,
}: {
  item: Atendimento
  onCiencia: (href: string) => void
  pending: boolean
}) {
  const actions = useActions(item._links)
  const pendente = item.estado === 'PENDENTE_CIENCIA'
  return (
    <tr>
      <td>{new Date(item.registradoEm).toLocaleDateString('pt-BR')}</td>
      <td>{item.assunto}</td>
      <td>
        <span className={`badge ${pendente ? 'warning' : 'success'}`}>
          {pendente ? 'Pendente ciência' : 'Ciente'}
        </span>
      </td>
      <td className="actions">
        {actions.can('acknowledge') ? (
          <button type="button" disabled={pending} onClick={() => onCiencia(actions.href('acknowledge') ?? '')}>
            Estou ciente
          </button>
        ) : (
          <span className="muted" aria-hidden="true">
            —
          </span>
        )}
      </td>
    </tr>
  )
}
