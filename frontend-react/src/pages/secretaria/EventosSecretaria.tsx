import { Link, useSearchParams } from 'react-router-dom'
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { academicoApi } from '../../api/academico'
import { ApiError } from '../../api/client'
import { eventosApi } from '../../api/eventos'
import { useActions } from '../../hooks/useActions'
import type { Evento } from '../../models/evento'

export function EventosSecretaria() {
  const [params, setParams] = useSearchParams()
  const cursoId = params.get('curso') ?? ''
  const estado = params.get('estado') ?? ''
  const queryClient = useQueryClient()

  const cursos = useQuery({
    queryKey: ['cursos', 'eventos-secretaria'],
    queryFn: () => academicoApi.listarCursos(0, 100),
  })
  const lista = useQuery({
    queryKey: ['eventos', 'secretaria', cursoId, estado],
    queryFn: () => eventosApi.listarDoEscopo({ cursoId: cursoId || undefined, estado: estado || undefined }),
  })
  const actions = useActions(lista.data?._links)
  const forbidden = lista.isError && lista.error instanceof ApiError && lista.error.status === 403

  const excluir = useMutation({
    mutationFn: (id: string) => eventosApi.excluir(id),
    onSuccess: () => queryClient.invalidateQueries({ queryKey: ['eventos', 'secretaria'] }),
  })

  function atualizarFiltro(chave: string, valor: string) {
    const next = new URLSearchParams(params)
    if (valor) {
      next.set(chave, valor)
    } else {
      next.delete(chave)
    }
    setParams(next)
  }

  return (
    <section className="page">
      <header className="page-head">
        <div>
          <h1>Eventos da secretaria</h1>
          <p className="muted">Mesmo motor /events. Encerrar continua sem certificado.</p>
        </div>
        {actions.can('novoEvento') && (
          <Link to="/secretaria/eventos/nova" className="button-link">
            Novo evento
          </Link>
        )}
      </header>

      <div className="filter-bar">
        <label>
          Estado
          <select value={estado} onChange={(event) => atualizarFiltro('estado', event.target.value)}>
            <option value="">Todos</option>
            <option value="AGENDADO">AGENDADO</option>
            <option value="EM_ANDAMENTO">EM_ANDAMENTO</option>
            <option value="CONCLUIDO">CONCLUIDO</option>
          </select>
        </label>
        <label>
          Curso
          <select value={cursoId} onChange={(event) => atualizarFiltro('curso', event.target.value)}>
            <option value="">Todos</option>
            {(cursos.data?.content ?? []).map((curso) => (
              <option key={curso.id} value={curso.id}>
                {curso.sigla}
              </option>
            ))}
          </select>
        </label>
      </div>

      {forbidden && (
        <p className="empty" role="status">
          Você não tem permissão para gerenciar eventos da secretaria.
        </p>
      )}
      {lista.isError && !forbidden && (
        <div className="banner danger" role="alert">
          Não foi possível carregar os eventos.{' '}
          <button type="button" onClick={() => lista.refetch()}>
            Tentar de novo
          </button>
        </div>
      )}
      {excluir.isError && (
        <div className="banner danger" role="alert">
          {excluir.error instanceof ApiError ? excluir.error.message : 'Não foi possível excluir o evento.'}
        </div>
      )}
      {lista.isLoading && (
        <p className="muted" aria-busy="true">
          Carregando eventos…
        </p>
      )}
      {lista.data && lista.data.content.length === 0 && (
        <p className="empty" role="status">
          Nenhum evento nos cursos vinculados.
        </p>
      )}
      {lista.data && lista.data.content.length > 0 && (
        <table>
          <thead>
            <tr>
              <th scope="col">Título</th>
              <th scope="col">Período</th>
              <th scope="col">Modo</th>
              <th scope="col">Estado</th>
              <th scope="col">Curso</th>
              <th scope="col">Ações</th>
            </tr>
          </thead>
          <tbody>
            {lista.data.content.map((item) => (
              <Linha key={item.id} item={item} onExcluir={() => excluir.mutate(item.id)} pending={excluir.isPending} />
            ))}
          </tbody>
        </table>
      )}
    </section>
  )
}

function Linha({ item, onExcluir, pending }: { item: Evento; onExcluir: () => void; pending: boolean }) {
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
      <td>{item.cursoSigla ?? '—'}</td>
      <td className="actions">
        {actions.can('host-session') && (
          <Link to={`/secretaria/eventos/${item.id}/operacao`} className="ghost-link">
            Operação
          </Link>
        )}
        {actions.can('editar') && (
          <Link to={`/secretaria/eventos/${item.id}`} className="ghost-link">
            Editar
          </Link>
        )}
        {actions.can('excluir') && (
          <button type="button" className="ghost" disabled={pending} onClick={onExcluir}>
            Excluir
          </button>
        )}
      </td>
    </tr>
  )
}
