import { useState } from 'react'
import { Link, useParams } from 'react-router-dom'
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { ApiError } from '../../api/client'
import { formativasApi } from '../../api/formativas'
import { ConfirmacaoFormativaWidget } from '../../components/ConfirmacaoFormativaWidget'
import { useActions } from '../../hooks/useActions'
import { rotuloEstadoFormativa, rotuloOrigemFormativa } from '../../lib/formativa'

export function FormativaDetalhe() {
  const { id = '' } = useParams()
  const queryClient = useQueryClient()
  const [avisoComprovante, setAvisoComprovante] = useState<string | null>(null)
  const [abrindoComprovante, setAbrindoComprovante] = useState(false)

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
  const acoes = useActions(item?._links)
  const pending = confirmar.isPending || cancelar.isPending
  const erroAcao = confirmar.error ?? cancelar.error

  async function abrirComprovante() {
    const href = acoes.href('comprovante')
    if (!href) {
      return
    }
    setAvisoComprovante(null)
    setAbrindoComprovante(true)
    try {
      const resposta = await formativasApi.comprovante(href)
      window.open(resposta.downloadUrl, '_blank', 'noopener')
    } catch {
      setAvisoComprovante('Não foi possível abrir o comprovante.')
    } finally {
      setAbrindoComprovante(false)
    }
  }

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
            <h1>{item.titulo}</h1>
            <span className={`badge estado-${item.estado.toLowerCase()}`}>
              {rotuloEstadoFormativa(item.estado)}
            </span>
          </header>

          {(erroAcao || avisoComprovante) && (
            <div className="banner danger" role="alert">
              {avisoComprovante ??
                (erroAcao instanceof ApiError ? erroAcao.message : 'Não foi possível concluir a ação.')}
            </div>
          )}

          <ConfirmacaoFormativaWidget
            links={item._links}
            pending={pending}
            onConfirm={() => confirmar.mutate()}
            onCancel={() => cancelar.mutate()}
            onComprovante={() => void abrirComprovante()}
            comprovantePending={abrindoComprovante}
          />

          <article className="panel formativa-detalhes">
            <h2>Detalhes</h2>
            <dl className="resumo resumo-horizontal">
              <div>
                <dt>Origem</dt>
                <dd>{rotuloOrigemFormativa(item.origem)}</dd>
              </div>
              <div>
                <dt>Carga horária</dt>
                <dd>{item.cargaHoraria}h</dd>
              </div>
              <div>
                <dt>Estado</dt>
                <dd>{rotuloEstadoFormativa(item.estado)}</dd>
              </div>
              <div>
                <dt>Registrada em</dt>
                <dd>{new Date(item.createdAt).toLocaleString('pt-BR')}</dd>
              </div>
              {item.parecer && (
                <div className="formativa-detalhes-parecer">
                  <dt>Parecer da CAAF</dt>
                  <dd>{item.parecer}</dd>
                </div>
              )}
            </dl>
          </article>
        </>
      )}
    </section>
  )
}
