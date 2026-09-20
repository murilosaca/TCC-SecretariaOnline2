import { Link, useLocation, useNavigate, useParams, useSearchParams } from 'react-router-dom'
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { ApiError } from '../../api/client'
import { solicitacoesApi } from '../../api/solicitacoes'
import { useAuth } from '../../auth/AuthContext'
import { PainelDeliberacao } from '../../components/PainelDeliberacao'
import { fieldsFromSchema } from '../../lib/formSchema'

export function DeliberarSolicitacao() {
  const { id = '' } = useParams()
  const navigate = useNavigate()
  const location = useLocation()
  const [params] = useSearchParams()
  const token = params.get('token')?.trim() || undefined
  const { status } = useAuth()
  const queryClient = useQueryClient()
  const returnUrl = `${location.pathname}${location.search}`
  const precisaLogin = status === 'anonymous' && Boolean(token)

  const detalhe = useQuery({
    queryKey: ['solicitacao', id, token ?? ''],
    queryFn: () => solicitacoesApi.obter(id, token),
    enabled: Boolean(id) && status === 'authenticated',
  })

  const transicionar = useMutation({
    mutationFn: ({ action, parecer }: { action: string; parecer: string }) =>
      solicitacoesApi.transicionar(id, action, parecer, token),
    onSuccess: () => {
      void queryClient.invalidateQueries({ queryKey: ['solicitacoes'] })
      void queryClient.invalidateQueries({ queryKey: ['solicitacao', id] })
      navigate('/solicitacoes?to=me', {
        replace: true,
        state: { confirmacao: 'Deliberação registrada.' },
      })
    },
  })

  const item = detalhe.data
  const campos = fieldsFromSchema(item?.formSchema)
  const forbidden = detalhe.isError && detalhe.error instanceof ApiError && detalhe.error.status === 403
  const tokenInvalido = detalhe.isError && detalhe.error instanceof ApiError && detalhe.error.status === 401

  if (status === 'loading') {
    return (
      <section className="page detalhe">
        <p className="muted" aria-busy="true">
          Verificando sessão…
        </p>
      </section>
    )
  }

  if (precisaLogin) {
    return (
      <section className="page detalhe">
        <div className="banner info" role="status">
          Entre com sua conta institucional para deliberar esta solicitação.{' '}
          <Link to="/login" state={{ from: returnUrl }}>
            Fazer login
          </Link>
        </div>
      </section>
    )
  }

  return (
    <section className="page detalhe">
      <p>
        <Link to="/solicitacoes?to=me" className="ghost-link">
          ← Fila de deliberação
        </Link>
      </p>

      {detalhe.isLoading && (
        <p className="muted" aria-busy="true">
          Carregando solicitação…
        </p>
      )}
      {forbidden && (
        <p className="empty" role="status">
          Deliberação indisponível para esta sessão.
        </p>
      )}
      {tokenInvalido && (
        <div className="banner danger" role="alert">
          Link inválido ou expirado.
        </div>
      )}
      {detalhe.isError && !forbidden && !tokenInvalido && (
        <div className="banner danger" role="alert">
          Solicitação não encontrada ou indisponível.{' '}
          <button type="button" onClick={() => detalhe.refetch()}>
            Tentar de novo
          </button>
        </div>
      )}

      {item && (
        <>
          <header className="page-head">
            <div>
              <h1>{item.protocolo}</h1>
              <p className="muted">
                {item.tipoNome}
                {item.solicitanteNome ? ` · ${item.solicitanteNome}` : ''}
              </p>
            </div>
            <span className={`badge estado-${item.estado.toLowerCase()}`}>{item.estado}</span>
          </header>

          {transicionar.isError && (
            <div className="banner danger" role="alert">
              {transicionar.error instanceof ApiError
                ? transicionar.error.message
                : 'Não foi possível registrar a deliberação.'}
            </div>
          )}

          <div className="detalhe-grid">
            <article className="panel">
              <h2>Dados</h2>
              <dl className="resumo">
                {(campos.length > 0
                  ? campos
                  : Object.keys(item.payload).map((name) => ({ name, title: name }))
                ).map((campo) => (
                  <div key={campo.name}>
                    <dt>{campo.title}</dt>
                    <dd>
                      {item.payload[campo.name] == null || item.payload[campo.name] === ''
                        ? '—'
                        : String(item.payload[campo.name])}
                    </dd>
                  </div>
                ))}
              </dl>
            </article>
            <aside className="panel">
              <h2>Metadados</h2>
              <p>
                <strong>Prazo:</strong>{' '}
                <span className={item.prazoVencido ? 'sla-danger' : undefined}>
                  {new Date(item.prazoEm).toLocaleString('pt-BR')}
                </span>
              </p>
              <p>
                <strong>SLA:</strong> {item.sla}
              </p>
              <p>
                <strong>Aberta em:</strong> {new Date(item.createdAt).toLocaleString('pt-BR')}
              </p>
            </aside>
          </div>

          <section className="panel">
            <h2>Linha do tempo</h2>
            {item.eventos.length === 0 && <p className="empty">Nenhum evento registrado.</p>}
            {item.eventos.length > 0 && (
              <ol className="timeline">
                {item.eventos.map((evento) => (
                  <li key={evento.id}>
                    <strong>{evento.tipo}</strong>
                    <span className="muted">
                      {' '}
                      {evento.estadoDe ? `${evento.estadoDe} → ` : ''}
                      {evento.estadoPara}
                    </span>
                    <div className="muted">{new Date(evento.createdAt).toLocaleString('pt-BR')}</div>
                    {evento.parecer && <p>{evento.parecer}</p>}
                  </li>
                ))}
              </ol>
            )}
          </section>

          <PainelDeliberacao
            links={item._links}
            pending={transicionar.isPending}
            onDeliberar={(action, parecer) => transicionar.mutate({ action, parecer })}
          />
        </>
      )}
    </section>
  )
}
