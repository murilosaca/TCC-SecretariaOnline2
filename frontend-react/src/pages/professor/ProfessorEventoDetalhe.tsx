import { Link, useParams } from 'react-router-dom'
import { useQuery } from '@tanstack/react-query'
import { eventosApi } from '../../api/eventos'
import { useActions } from '../../hooks/useActions'

export function ProfessorEventoDetalhe() {
  const { id = '' } = useParams()
  const detalhe = useQuery({
    queryKey: ['eventos', id],
    queryFn: () => eventosApi.obter(id),
    enabled: Boolean(id),
  })
  const actions = useActions(detalhe.data?._links)

  return (
    <section className="page">
      <header className="page-head">
        <div>
          <h1>{detalhe.data?.titulo ?? 'Evento'}</h1>
          <p className="muted">Detalhe do evento hospedado (RF-F3-002-a).</p>
        </div>
        <Link to="/professor/eventos" className="ghost-link">
          Voltar
        </Link>
      </header>

      {detalhe.isError && (
        <div className="banner danger" role="alert">
          Não foi possível carregar o evento.{' '}
          <button type="button" onClick={() => detalhe.refetch()}>
            Tentar de novo
          </button>
        </div>
      )}
      {detalhe.isLoading && (
        <p className="muted" aria-busy="true">
          Carregando evento…
        </p>
      )}
      {detalhe.data && (
        <div className="attendance-card card">
          <dl className="resumo">
            <div>
              <dt>Estado</dt>
              <dd>{detalhe.data.estado}</dd>
            </div>
            <div>
              <dt>Modo</dt>
              <dd>{detalhe.data.attendanceMode}</dd>
            </div>
            <div>
              <dt>Agenda</dt>
              <dd>
                {new Date(detalhe.data.inicioEm).toLocaleString('pt-BR')} —{' '}
                {new Date(detalhe.data.fimEm).toLocaleString('pt-BR')}
              </dd>
            </div>
            <div>
              <dt>CH</dt>
              <dd>{detalhe.data.cargaHoraria}h</dd>
            </div>
          </dl>
          {actions.can('host-session') && (
            <p>
              <Link to={`/professor/eventos/${id}/operacao`} className="button-link">
                Painel de operação
              </Link>
            </p>
          )}
        </div>
      )}
    </section>
  )
}
