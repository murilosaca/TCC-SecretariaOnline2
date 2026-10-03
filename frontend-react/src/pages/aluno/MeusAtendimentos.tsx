import { useSearchParams } from 'react-router-dom'
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { atendimentosApi } from '../../api/atendimentos'
import { ApiError } from '../../api/client'
import { useActions } from '../../hooks/useActions'
import type { Atendimento } from '../../models/atendimento'

export function MeusAtendimentos() {
  const [params, setParams] = useSearchParams()
  const pendente = params.get('status') === 'PENDENTE_CIENCIA'
  const queryClient = useQueryClient()

  const lista = useQuery({
    queryKey: ['atendimentos', 'me', pendente ? 'PENDENTE_CIENCIA' : 'todos'],
    queryFn: () => atendimentosApi.listarMeus(pendente ? 'PENDENTE_CIENCIA' : undefined),
  })

  const ciencia = useMutation({
    mutationFn: (href: string) => atendimentosApi.darCiencia(href),
    onSuccess: () => queryClient.invalidateQueries({ queryKey: ['atendimentos', 'me'] }),
  })

  const forbidden = lista.isError && lista.error instanceof ApiError && lista.error.status === 403
  const vazioPendente = Boolean(lista.data && lista.data.content.length === 0 && pendente)

  return (
    <section className="page">
      <header className="page-head">
        <div>
          <h1>Meus atendimentos</h1>
          <p className="muted">Atendimentos registrados pela secretaria. Ciência só aparece quando o link existir.</p>
        </div>
      </header>

      <div className="filter-bar">
        <label>
          <input
            type="checkbox"
            checked={pendente}
            onChange={(event) => {
              const next = new URLSearchParams(params)
              if (event.target.checked) {
                next.set('status', 'PENDENTE_CIENCIA')
              } else {
                next.delete('status')
              }
              setParams(next)
            }}
          />{' '}
          Pendente ciência
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
      {vazioPendente && (
        <p className="empty" role="status">
          Nenhum atendimento pendente.
        </p>
      )}
      {lista.data && lista.data.content.length === 0 && !pendente && (
        <p className="empty" role="status">
          Nenhum atendimento registrado.
        </p>
      )}
      {lista.data && lista.data.content.length > 0 && (
        <table>
          <thead>
            <tr>
              <th scope="col">Data</th>
              <th scope="col">Assunto</th>
              <th scope="col">Status</th>
              <th scope="col">Ação</th>
            </tr>
          </thead>
          <tbody>
            {lista.data.content.map((item) => (
              <Linha key={item.id} item={item} onCiencia={(href) => ciencia.mutate(href)} pending={ciencia.isPending} />
            ))}
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
        {actions.can('acknowledge') && (
          <button type="button" disabled={pending} onClick={() => onCiencia(actions.href('acknowledge') ?? '')}>
            Estou ciente
          </button>
        )}
      </td>
    </tr>
  )
}
