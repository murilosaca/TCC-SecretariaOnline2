import { Link, useParams } from 'react-router-dom'
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { ApiError } from '../../api/client'
import { formativasApi } from '../../api/formativas'
import { ConfirmacaoFormativaWidget } from '../../components/ConfirmacaoFormativaWidget'

export function FormativaDetalhe() {
  const { id = '' } = useParams()
  const queryClient = useQueryClient()

  const detalhe = useQuery({
    queryKey: ['formativa', id],
    queryFn: () => formativasApi.obter(id),
    enabled: Boolean(id),
  })

  const confirmar = useMutation({
    mutationFn: () => formativasApi.confirmar(id),
    onSuccess: (atual) => {
      queryClient.setQueryData(['formativa', id], atual)
      void queryClient.invalidateQueries({ queryKey: ['formativas', 'me'] })
      void queryClient.invalidateQueries({ queryKey: ['bff', 'dashboard', 'aluno'] })
    },
  })

  const cancelar = useMutation({
    mutationFn: () => formativasApi.cancelar(id),
    onSuccess: (atual) => {
      queryClient.setQueryData(['formativa', id], atual)
      void queryClient.invalidateQueries({ queryKey: ['formativas', 'me'] })
      void queryClient.invalidateQueries({ queryKey: ['bff', 'dashboard', 'aluno'] })
    },
  })

  const item = detalhe.data
  const pending = confirmar.isPending || cancelar.isPending
  const erroAcao = confirmar.error ?? cancelar.error

  return (
    <section className="page detalhe">
      <p>
        <Link to="/formativas" className="ghost-link">
          ← Formativas
        </Link>
      </p>

      {detalhe.isLoading && (
        <p className="muted" aria-busy="true">
          Carregando formativa…
        </p>
      )}
      {detalhe.isError && (
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
                {item.origem === 'PRESENCA_VALIDADA' ? 'Presença validada' : item.origem} · {item.cargaHoraria}h
              </p>
            </div>
            <span className={`badge estado-${item.estado.toLowerCase()}`}>{rotuloEstado(item.estado)}</span>
          </header>

          {erroAcao && (
            <div className="banner danger" role="alert">
              {erroAcao instanceof ApiError ? erroAcao.message : 'Não foi possível concluir a ação.'}
            </div>
          )}

          <ConfirmacaoFormativaWidget
            links={item._links}
            pending={pending}
            onConfirm={() => confirmar.mutate()}
            onCancel={() => cancelar.mutate()}
          />

          <article className="panel">
            <h2>Metadados</h2>
            <dl className="resumo">
              <div>
                <dt>Estado</dt>
                <dd>{rotuloEstado(item.estado)}</dd>
              </div>
              <div>
                <dt>Registrada em</dt>
                <dd>{new Date(item.createdAt).toLocaleString('pt-BR')}</dd>
              </div>
            </dl>
          </article>
        </>
      )}
    </section>
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
