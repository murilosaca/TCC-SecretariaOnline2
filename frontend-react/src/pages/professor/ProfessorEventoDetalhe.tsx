import { Link, useParams } from 'react-router-dom'
import { useQuery } from '@tanstack/react-query'
import { eventosApi } from '../../api/eventos'
import { useActions } from '../../hooks/useActions'
import { rotuloCodigo } from '../../lib/rotulos'

export function ProfessorEventoDetalhe() {
  const { id = '' } = useParams()
  const detalhe = useQuery({
    queryKey: ['eventos', id],
    queryFn: () => eventosApi.obter(id),
    enabled: Boolean(id),
  })
  const actions = useActions(detalhe.data?._links)

  return (
    <section className="page detalhe">
      <header className="page-head">
        <h1>{detalhe.data?.titulo ?? 'Evento'}</h1>
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
        <article className="panel evento-detalhe-card" aria-labelledby="evento-detalhes-titulo">
          <h2 id="evento-detalhes-titulo">Detalhes</h2>
          <dl className="resumo">
            <div>
              <dt>Estado</dt>
              <dd>{rotuloCodigo(detalhe.data.estado)}</dd>
            </div>
            <div>
              <dt>Modo</dt>
              <dd>{rotuloCodigo(detalhe.data.attendanceMode)}</dd>
            </div>
            <div>
              <dt>Início</dt>
              <dd>{new Date(detalhe.data.inicioEm).toLocaleString('pt-BR')}</dd>
            </div>
            <div>
              <dt>Fim</dt>
              <dd>{new Date(detalhe.data.fimEm).toLocaleString('pt-BR')}</dd>
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
        </article>
      )}
    </section>
  )
}
