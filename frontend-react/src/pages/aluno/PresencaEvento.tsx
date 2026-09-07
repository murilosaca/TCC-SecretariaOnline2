import { useState } from 'react'
import { Link, useParams } from 'react-router-dom'
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { ApiError } from '../../api/client'
import { eventosApi } from '../../api/eventos'
import { AttendanceWidget } from '../../components/AttendanceWidget'
import { useActions } from '../../hooks/useActions'
import { obterDeviceUuid } from '../../lib/deviceUuid'
import type { SessaoPresenca } from '../../models/evento'

export function PresencaEvento() {
  const { id = '' } = useParams()
  const queryClient = useQueryClient()
  const [erroConfirmacao, setErroConfirmacao] = useState<string | null>(null)

  const sessao = useQuery({
    queryKey: ['eventos', id, 'sessao'],
    queryFn: () => eventosApi.sessao(id),
    enabled: Boolean(id),
  })

  const confirmar = useMutation({
    mutationFn: ({ pin, fase }: { pin: string; fase: string }) =>
      eventosApi.confirmar(id, { pin, deviceUuid: obterDeviceUuid(), fase }),
    onSuccess: (atualizada) => {
      setErroConfirmacao(null)
      queryClient.setQueryData(['eventos', id, 'sessao'], atualizada)
      void queryClient.invalidateQueries({ queryKey: ['eventos', 'me'] })
    },
    onError: (erro) => {
      if (erro instanceof ApiError) {
        setErroConfirmacao(erro.message)
        return
      }
      setErroConfirmacao('Não foi possível confirmar a presença.')
    },
  })

  return (
    <section className="page">
      <header className="page-head">
        <div>
          <h1>{sessao.data?.titulo ?? 'Presença no evento'}</h1>
          <p className="muted">Proof of Stay · validação web (RF-F1-009).</p>
        </div>
        <Link to="/eventos" className="ghost-link">
          Voltar aos eventos
        </Link>
      </header>

      {sessao.isError && (
        <div className="banner danger" role="alert">
          Não foi possível carregar a sessão de presença.{' '}
          <button type="button" onClick={() => sessao.refetch()}>
            Tentar de novo
          </button>
        </div>
      )}

      {sessao.isLoading && (
        <p className="muted" aria-busy="true">
          Carregando sessão…
        </p>
      )}

      {sessao.data && <ConteudoSessao sessao={sessao.data} erro={erroConfirmacao} pending={confirmar.isPending} onConfirm={(pin, fase) => confirmar.mutate({ pin, fase })} />}
    </section>
  )
}

function ConteudoSessao({
  sessao,
  erro,
  pending,
  onConfirm,
}: {
  sessao: SessaoPresenca
  erro: string | null
  pending: boolean
  onConfirm: (pin: string, fase: string) => void
}) {
  const actions = useActions(sessao._links)
  const podeConfirmar = actions.can('confirmar-entrada') || actions.can('confirmar-saida')

  if (sessao.situacaoPresenca === 'COMPLETA') {
    return (
      <div className="attendance-card card">
        <div className="banner success" role="status">
          Presença confirmada.
        </div>
      </div>
    )
  }

  if (!sessao.janelaAtiva || !podeConfirmar) {
    return (
      <div className="attendance-card card">
        <p className="empty" role="status">
          A janela de validação encerrou.
        </p>
      </div>
    )
  }

  return (
    <div className="attendance-card card">
      <p className="muted">Informe o PIN divulgado no evento. O botão só aparece com janela ativa.</p>
      <AttendanceWidget
        links={sessao._links}
        janelaExpira={sessao.janelaExpira}
        faseDisponivel={sessao.faseDisponivel}
        pending={pending}
        error={erro}
        onConfirm={onConfirm}
      />
    </div>
  )
}
