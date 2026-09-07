import { useState } from 'react'
import { Link, useParams } from 'react-router-dom'
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { ApiError } from '../../api/client'
import { eventosApi } from '../../api/eventos'
import { HostActionBar } from '../../components/HostActionBar'
import type { HostSessao } from '../../models/evento'

export function ProfessorEventoOperacao() {
  const { id = '' } = useParams()
  const queryClient = useQueryClient()
  const [erro, setErro] = useState<string | null>(null)

  const sessao = useQuery({
    queryKey: ['eventos', id, 'host-session'],
    queryFn: () => eventosApi.sessaoHost(id),
    enabled: Boolean(id),
    refetchInterval: 5000,
  })

  const abrir = useMutation({
    mutationFn: () => eventosApi.abrirJanelaEntrada(id),
    onSuccess: (atual) => {
      setErro(null)
      queryClient.setQueryData(['eventos', id, 'host-session'], atual)
    },
    onError: (falha) => setErro(falha instanceof ApiError ? falha.message : 'Não foi possível abrir a janela.'),
  })

  const encerrar = useMutation({
    mutationFn: () => eventosApi.encerrar(id),
    onSuccess: (atual) => {
      setErro(null)
      queryClient.setQueryData(['eventos', id, 'host-session'], atual)
    },
    onError: (falha) => setErro(falha instanceof ApiError ? falha.message : 'Não foi possível encerrar o evento.'),
  })

  return (
    <section className="page">
      <header className="page-head">
        <div>
          <h1>{sessao.data?.titulo ?? 'Operação do evento'}</h1>
          <p className="muted">Painel ao vivo · SECRET_SINGLE (RF-F3-002-b).</p>
        </div>
        <Link to={`/professor/eventos/${id}`} className="ghost-link">
          Voltar ao evento
        </Link>
      </header>

      {sessao.isError && (
        <div className="banner danger" role="alert">
          {sessao.error instanceof ApiError && sessao.error.status === 403
            ? 'Você não pode operar este evento.'
            : 'Não foi possível carregar a sessão de operação.'}{' '}
          <button type="button" onClick={() => sessao.refetch()}>
            Tentar de novo
          </button>
        </div>
      )}
      {sessao.isLoading && (
        <p className="muted" aria-busy="true">
          Carregando painel…
        </p>
      )}
      {sessao.data && (
        <Painel
          sessao={sessao.data}
          erro={erro}
          pending={abrir.isPending || encerrar.isPending}
          onAbrir={() => abrir.mutate()}
          onEncerrar={() => encerrar.mutate()}
        />
      )}
    </section>
  )
}

function Painel({
  sessao,
  erro,
  pending,
  onAbrir,
  onEncerrar,
}: {
  sessao: HostSessao
  erro: string | null
  pending: boolean
  onAbrir: () => void
  onEncerrar: () => void
}) {
  return (
    <div className="attendance-card card">
      <p className={`badge estado-${sessao.estado.toLowerCase()}`}>{sessao.estado}</p>
      {sessao.janelaAtiva && sessao.janelaExpira && (
        <p className="countdown" aria-live="polite">
          {formatarCountdown(sessao.janelaExpira)}
        </p>
      )}
      {sessao.pin ? (
        <p className="pin-display" aria-label="PIN de presença">
          {sessao.pin}
        </p>
      ) : (
        <p className="empty" role="status">
          {sessao.estado === 'CONCLUIDO'
            ? 'Evento encerrado. A janela de validação foi fechada.'
            : 'Abra a janela de entrada para exibir o PIN.'}
        </p>
      )}
      <p className="muted">Presentes: {sessao.presentes}</p>
      {erro && (
        <p className="field-error" role="alert">
          {erro}
        </p>
      )}
      <HostActionBar
        links={sessao._links}
        pending={pending}
        onAbrirJanela={onAbrir}
        onEncerrar={onEncerrar}
      />
    </div>
  )
}

function formatarCountdown(iso: string): string {
  const ms = Math.max(0, new Date(iso).getTime() - Date.now())
  const total = Math.floor(ms / 1000)
  return `${String(Math.floor(total / 60)).padStart(2, '0')}:${String(total % 60).padStart(2, '0')}`
}
