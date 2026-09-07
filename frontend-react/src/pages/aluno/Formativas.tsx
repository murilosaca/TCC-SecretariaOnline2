import { Link } from 'react-router-dom'
import { useQuery } from '@tanstack/react-query'
import { formativasApi } from '../../api/formativas'
import { useActions } from '../../hooks/useActions'
import type { Formativa } from '../../models/formativa'

export function Formativas() {
  const lista = useQuery({
    queryKey: ['formativas', 'me'],
    queryFn: () => formativasApi.listarMinhas(),
  })

  return (
    <section className="page">
      <header className="page-head">
        <div>
          <h1>Atividades formativas</h1>
          <p className="muted">Confirme horas geradas a partir de presença validada (RF-F1-006).</p>
        </div>
      </header>

      {lista.isError && (
        <div className="banner danger" role="alert">
          Não foi possível carregar as formativas.{' '}
          <button type="button" onClick={() => lista.refetch()}>
            Tentar de novo
          </button>
        </div>
      )}

      {lista.isLoading && (
        <p className="muted" aria-busy="true">
          Carregando formativas…
        </p>
      )}
      {lista.data && lista.data.content.length === 0 && (
        <p className="empty" role="status">
          Nenhuma atividade formativa no momento. Confirme presença em um evento para gerar uma
          pendência.
        </p>
      )}
      {lista.data && lista.data.content.length > 0 && (
        <table>
          <thead>
            <tr>
              <th scope="col">Atividade</th>
              <th scope="col">Horas</th>
              <th scope="col">Origem</th>
              <th scope="col">Estado</th>
              <th scope="col">Data</th>
              <th scope="col">Ações</th>
            </tr>
          </thead>
          <tbody>
            {lista.data.content.map((item) => (
              <Linha key={item.id} item={item} />
            ))}
          </tbody>
        </table>
      )}
    </section>
  )
}

function Linha({ item }: { item: Formativa }) {
  const actions = useActions(item._links)
  const data = new Date(item.createdAt).toLocaleDateString('pt-BR')
  return (
    <tr>
      <td>{item.titulo}</td>
      <td>{item.cargaHoraria}h</td>
      <td>{rotuloOrigem(item.origem)}</td>
      <td>
        <span className={`badge estado-${item.estado.toLowerCase()}`}>{rotuloEstado(item.estado)}</span>
      </td>
      <td>{data}</td>
      <td className="actions">
        {actions.can('self') && (
          <Link to={`/formativas/${item.id}`} className="ghost-link">
            Abrir
          </Link>
        )}
      </td>
    </tr>
  )
}

function rotuloEstado(estado: string): string {
  if (estado === 'PENDENTE_CONFIRMACAO') {
    return 'Aguardando confirmação'
  }
  if (estado === 'AGUARDANDO_CAAF') {
    return 'Aguardando CAAF'
  }
  if (estado === 'APROVADA') {
    return 'Aprovada'
  }
  if (estado === 'INDEFERIDA') {
    return 'Indeferida'
  }
  if (estado === 'CANCELADA') {
    return 'Cancelada'
  }
  return estado
}

function rotuloOrigem(origem: string): string {
  if (origem === 'PRESENCA_VALIDADA') {
    return 'Presença validada'
  }
  return origem
}
