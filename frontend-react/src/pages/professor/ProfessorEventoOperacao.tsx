import { useEffect, useState } from 'react'
import { Link, useParams } from 'react-router-dom'
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { ApiError } from '../../api/client'
import { eventosApi } from '../../api/eventos'
import { HostActionBar } from '../../components/HostActionBar'
import { QrDisplay } from '../../components/QrDisplay'
import { isQrMode } from '../../models/evento'
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

  function aoAtualizar(atual: HostSessao) {
    setErro(null)
    queryClient.setQueryData(['eventos', id, 'host-session'], atual)
  }

  const abrir = useMutation({
    mutationFn: () => eventosApi.abrirJanelaEntrada(id),
    onSuccess: aoAtualizar,
    onError: (falha) => setErro(falha instanceof ApiError ? falha.message : 'Não foi possível abrir a janela.'),
  })

  const abrirSaida = useMutation({
    mutationFn: () => eventosApi.abrirJanelaSaida(id),
    onSuccess: aoAtualizar,
    onError: (falha) => setErro(falha instanceof ApiError ? falha.message : 'Não foi possível abrir a janela de saída.'),
  })

  const renovar = useMutation({
    mutationFn: () => eventosApi.renovarQr(id),
    onSuccess: aoAtualizar,
    onError: (falha) => setErro(falha instanceof ApiError ? falha.message : 'Não foi possível renovar o QR.'),
  })

  const encerrar = useMutation({
    mutationFn: () => eventosApi.encerrar(id),
    onSuccess: aoAtualizar,
    onError: (falha) => setErro(falha instanceof ApiError ? falha.message : 'Não foi possível encerrar o evento.'),
  })

  useEffect(() => {
    const expira = sessao.data?.tokenExpira
    const podeRenovar = Boolean(sessao.data?._links?.['renovar-qr'])
    if (!id || !expira || !podeRenovar) {
      return
    }
    const restante = new Date(expira).getTime() - Date.now()
    const timer = window.setTimeout(() => {
      void eventosApi.renovarQr(id).then(aoAtualizar).catch((falha) => {
        setErro(falha instanceof ApiError ? falha.message : 'Não foi possível renovar o QR.')
      })
    }, Math.max(500, restante))
    return () => window.clearTimeout(timer)
  }, [id, sessao.data?.tokenExpira, sessao.data?._links?.['renovar-qr']])

  const pending = abrir.isPending || abrirSaida.isPending || renovar.isPending || encerrar.isPending

  return (
    <section className="page">
      <header className="page-head">
        <div>
          <h1>{sessao.data?.titulo ?? 'Operação do evento'}</h1>
          <p className="muted">Painel ao vivo · {sessao.data?.attendanceMode ?? 'Proof of Stay'} (RF-F3-002-b).</p>
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
          pending={pending}
          onAbrir={() => abrir.mutate()}
          onAbrirSaida={() => abrirSaida.mutate()}
          onRenovar={() => renovar.mutate()}
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
  onAbrirSaida,
  onRenovar,
  onEncerrar,
}: {
  sessao: HostSessao
  erro: string | null
  pending: boolean
  onAbrir: () => void
  onAbrirSaida: () => void
  onRenovar: () => void
  onEncerrar: () => void
}) {
  const qr = isQrMode(sessao.attendanceMode)
  return (
    <div className="attendance-card card">
      <p className={`badge estado-${sessao.estado.toLowerCase()}`}>{sessao.estado}</p>
      <p className="muted">{sessao.attendanceMode}</p>
      {sessao.janelaAtiva && sessao.janelaExpira && (
        <p className="countdown" aria-live="polite">
          {formatarCountdown(sessao.janelaExpira)}
        </p>
      )}
      {qr && sessao.token ? (
        <QrDisplay token={sessao.token} />
      ) : sessao.pin ? (
        <p className="pin-display" aria-label="PIN de presença">
          {sessao.pin}
        </p>
      ) : (
        <p className="empty" role="status">
          {sessao.estado === 'CONCLUIDO'
            ? 'Evento encerrado. A janela de validação foi fechada.'
            : qr
              ? 'Abra a janela para exibir o QR.'
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
        onAbrirJanelaSaida={onAbrirSaida}
        onRenovarQr={onRenovar}
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
