import { Link } from 'react-router-dom'
import { useQuery } from '@tanstack/react-query'
import { ApiError } from '../../api/client'
import { eventosApi } from '../../api/eventos'
import { useActions } from '../../hooks/useActions'
import type { Evento } from '../../models/evento'

export function ProfessorEventos() {
  const lista = useQuery({
    queryKey: ['eventos', 'mine'],
    queryFn: () => eventosApi.listarDoAnfitriao(),
    retry: false,
  })
  const actions = useActions(lista.data?._links)
  const semPermissao = lista.isError && lista.error instanceof ApiError && lista.error.status === 403

  return (
    <section className="page">
      <header className="page-head">
        <div>
          <h1>Eventos que hospedo</h1>
          <p className="muted">Gerencie a oficina e a janela de presença (RF-F3-002).</p>
        </div>
        {actions.can('novoEvento') && (
          <Link to="/professor/eventos/nova" className="button-link">
            Novo evento
          </Link>
        )}
      </header>

      {semPermissao && (
        <p className="empty" role="status">
          Você não tem permissão para gerenciar eventos.
        </p>
      )}
      {lista.isError && !semPermissao && (
        <div className="banner danger" role="alert">
          Não foi possível carregar os eventos.{' '}
          <button type="button" onClick={() => lista.refetch()}>
            Tentar de novo
          </button>
        </div>
      )}
      {lista.isLoading && (
        <p className="muted" aria-busy="true">
          Carregando eventos…
        </p>
      )}
      {lista.data && lista.data.content.length === 0 && (
        <p className="empty" role="status">
          Nenhum evento hospedado no momento.
        </p>
      )}
      {lista.data && lista.data.content.length > 0 && (
        <table>
          <thead>
            <tr>
              <th scope="col">Título</th>
              <th scope="col">Agenda</th>
              <th scope="col">Modo</th>
              <th scope="col">Estado</th>
              <th scope="col">CH</th>
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

function Linha({ item }: { item: Evento }) {
  const actions = useActions(item._links)
  return (
    <tr>
      <td>{item.titulo}</td>
      <td>
        {new Date(item.inicioEm).toLocaleString('pt-BR')} — {new Date(item.fimEm).toLocaleString('pt-BR')}
      </td>
      <td>{item.attendanceMode}</td>
      <td>
        <span className={`badge estado-${item.estado.toLowerCase()}`}>{item.estado}</span>
      </td>
      <td>{item.cargaHoraria}h</td>
      <td className="actions">
        {actions.can('self') && (
          <Link to={`/professor/eventos/${item.id}`} className="ghost-link">
            Abrir
          </Link>
        )}
        {actions.can('host-session') && (
          <Link to={`/professor/eventos/${item.id}/operacao`} className="ghost-link">
            Operação
          </Link>
        )}
      </td>
    </tr>
  )
}
