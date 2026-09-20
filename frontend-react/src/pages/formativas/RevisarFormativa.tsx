import { Link, useNavigate, useParams } from 'react-router-dom'
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { ApiError } from '../../api/client'
import { formativasApi } from '../../api/formativas'
import { PainelRevisaoFormativa } from '../../components/PainelRevisaoFormativa'
import { rotuloEstadoFormativa, rotuloOrigemFormativa } from '../../lib/formativa'

export function RevisarFormativa() {
  const { id = '' } = useParams()
  const navigate = useNavigate()
  const queryClient = useQueryClient()

  const detalhe = useQuery({
    queryKey: ['formativa', id],
    queryFn: () => formativasApi.obter(id),
    enabled: Boolean(id),
  })

  const revisar = useMutation({
    mutationFn: ({ action, parecer }: { action: 'APROVAR' | 'INDEFERIR'; parecer: string }) =>
      action === 'APROVAR'
        ? formativasApi.aprovar(id, parecer)
        : formativasApi.indeferir(id, parecer),
    onSuccess: (atual) => {
      queryClient.setQueryData(['formativa', id], atual)
      void queryClient.invalidateQueries({ queryKey: ['formativas'] })
      void queryClient.invalidateQueries({ queryKey: ['bff', 'dashboard', 'aluno'] })
      navigate('/formativas?to=me', {
        replace: true,
        state: { confirmacao: atual.estado === 'APROVADA' ? 'Formativa aprovada.' : 'Formativa indeferida.' },
      })
    },
  })

  const item = detalhe.data
  const forbidden = detalhe.isError && detalhe.error instanceof ApiError && detalhe.error.status === 403

  return (
    <section className="page detalhe">
      <p>
        <Link to="/formativas?to=me" className="ghost-link">
          ← Fila de revisão
        </Link>
      </p>

      {detalhe.isLoading && (
        <p className="muted" aria-busy="true">
          Carregando formativa…
        </p>
      )}
      {forbidden && (
        <p className="empty" role="status">
          Revisão indisponível para esta sessão.
        </p>
      )}
      {detalhe.isError && !forbidden && (
        <div className="banner danger" role="alert">
          Formativa não encontrada ou indisponível.{' '}
          <button type="button" onClick={() => detalhe.refetch()}>
            Tentar de novo
          </button>
        </div>
      )}

      {item && (
        <>
          <header className="page-head">
            <div>
              <h1>{item.titulo}</h1>
              <p className="muted">
                {item.alunoNome ? `${item.alunoNome} · ` : ''}
                {rotuloOrigemFormativa(item.origem)} · {item.cargaHoraria}h
              </p>
            </div>
            <span className={`badge estado-${item.estado.toLowerCase()}`}>
              {rotuloEstadoFormativa(item.estado)}
            </span>
          </header>

          {revisar.isError && (
            <div className="banner danger" role="alert">
              {revisar.error instanceof ApiError
                ? revisar.error.message
                : 'Não foi possível registrar o parecer.'}
            </div>
          )}

          <article className="panel">
            <h2>Metadados</h2>
            <dl className="resumo">
              <div>
                <dt>Aluno</dt>
                <dd>{item.alunoNome || '—'}</dd>
              </div>
              <div>
                <dt>Horas</dt>
                <dd>{item.cargaHoraria}h</dd>
              </div>
              <div>
                <dt>Origem</dt>
                <dd>{rotuloOrigemFormativa(item.origem)}</dd>
              </div>
              <div>
                <dt>Registrada em</dt>
                <dd>{new Date(item.createdAt).toLocaleString('pt-BR')}</dd>
              </div>
            </dl>
          </article>

          {item.parecer && (
            <article className="panel">
              <h2>Parecer</h2>
              <p>{item.parecer}</p>
            </article>
          )}

          <PainelRevisaoFormativa
            links={item._links}
            pending={revisar.isPending}
            onRevisar={(action, parecer) => revisar.mutate({ action, parecer })}
          />
        </>
      )}
    </section>
  )
}
