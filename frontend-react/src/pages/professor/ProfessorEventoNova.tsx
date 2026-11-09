import { FormEvent, useState } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import { useMutation, useQuery } from '@tanstack/react-query'
import { ApiError } from '../../api/client'
import { eventosApi } from '../../api/eventos'
import { isDualMode, type AttendanceMode } from '../../models/evento'

const MODOS: { valor: AttendanceMode; rotulo: string }[] = [
  { valor: 'SECRET_SINGLE', rotulo: 'SECRET_SINGLE — PIN, só entrada' },
  { valor: 'SECRET_DUAL', rotulo: 'SECRET_DUAL — PIN, entrada e saída' },
  { valor: 'QR_SINGLE', rotulo: 'QR_SINGLE — token QR, só entrada' },
  { valor: 'QR_DUAL', rotulo: 'QR_DUAL — token QR, entrada e saída' },
]

function paraIso(valor: string): string {
  return new Date(valor).toISOString()
}

export function ProfessorEventoNova() {
  const navigate = useNavigate()
  const [titulo, setTitulo] = useState('')
  const [cursoId, setCursoId] = useState('')
  const [inicioEm, setInicioEm] = useState('')
  const [fimEm, setFimEm] = useState('')
  const [cargaHoraria, setCargaHoraria] = useState(4)
  const [attendanceMode, setAttendanceMode] = useState<AttendanceMode>('SECRET_SINGLE')
  const [entradaInicio, setEntradaInicio] = useState('')
  const [entradaFim, setEntradaFim] = useState('')
  const [saidaInicio, setSaidaInicio] = useState('')
  const [saidaFim, setSaidaFim] = useState('')
  const dual = isDualMode(attendanceMode)

  const cursos = useQuery({
    queryKey: ['eventos', 'cursos-alvo'],
    queryFn: () => eventosApi.cursosAlvo(),
  })

  const criar = useMutation({
    mutationFn: () =>
      eventosApi.criar({
        titulo: titulo.trim(),
        inicioEm: paraIso(inicioEm),
        fimEm: paraIso(fimEm),
        cargaHoraria,
        attendanceMode,
        cursoId,
        janelaEntrada: { inicio: paraIso(entradaInicio), fim: paraIso(entradaFim) },
        janelaSaida: dual ? { inicio: paraIso(saidaInicio), fim: paraIso(saidaFim) } : undefined,
      }),
    onSuccess: (evento) => navigate(`/professor/eventos/${evento.id}`, { replace: true }),
  })

  function onSubmit(event: FormEvent) {
    event.preventDefault()
    criar.mutate()
  }

  return (
    <section className="page">
      <header className="page-head">
        <div>
          <h1>Novo evento</h1>
          <p className="muted">
            Proof of Stay v4.1. O PIN ou o token QR só aparecem na host-session depois de abrir a janela.
          </p>
        </div>
        <Link to="/professor/eventos" className="ghost-link">
          Voltar
        </Link>
      </header>

      {criar.isError && (
        <div className="banner danger" role="alert">
          {criar.error instanceof ApiError ? criar.error.message : 'Não foi possível criar o evento.'}
        </div>
      )}
      {cursos.isPending && (
        <p className="muted" aria-busy="true">
          Carregando cursos…
        </p>
      )}
      {cursos.isError && (
        <div className="banner danger" role="alert">
          Não foi possível carregar os cursos do público-alvo.{' '}
          <button type="button" onClick={() => cursos.refetch()}>
            Tentar de novo
          </button>
        </div>
      )}
      {cursos.isSuccess && cursos.data.length === 0 && (
        <p className="empty" role="status">
          Nenhum curso ativo para o público-alvo.
        </p>
      )}

      <form className="attendance-card card" onSubmit={onSubmit}>
        <label>
          Título
          <input value={titulo} onChange={(event) => setTitulo(event.target.value)} required maxLength={200} />
        </label>
        <label>
          Curso do público-alvo
          <select required value={cursoId} onChange={(event) => setCursoId(event.target.value)}>
            <option value="">Selecione</option>
            {(cursos.data ?? []).map((curso) => (
              <option key={curso.id} value={curso.id}>
                {curso.sigla} — {curso.nome}
              </option>
            ))}
          </select>
        </label>
        <label>
          Início
          <input type="datetime-local" value={inicioEm} onChange={(event) => setInicioEm(event.target.value)} required />
        </label>
        <label>
          Fim
          <input type="datetime-local" value={fimEm} onChange={(event) => setFimEm(event.target.value)} required />
        </label>
        <label>
          Carga horária
          <input
            type="number"
            min={1}
            value={cargaHoraria}
            onChange={(event) => setCargaHoraria(Number(event.target.value))}
            required
          />
        </label>
        <label>
          Modo de presença
          <select
            value={attendanceMode}
            onChange={(event) => setAttendanceMode(event.target.value as AttendanceMode)}
          >
            {MODOS.map((modo) => (
              <option key={modo.valor} value={modo.valor}>
                {modo.rotulo}
              </option>
            ))}
          </select>
        </label>
        <label>
          Janela de entrada — início
          <input
            type="datetime-local"
            value={entradaInicio}
            onChange={(event) => setEntradaInicio(event.target.value)}
            required
          />
        </label>
        <label>
          Janela de entrada — fim
          <input
            type="datetime-local"
            value={entradaFim}
            onChange={(event) => setEntradaFim(event.target.value)}
            required
          />
        </label>
        {dual && (
          <>
            <label>
              Janela de saída — início
              <input
                type="datetime-local"
                value={saidaInicio}
                onChange={(event) => setSaidaInicio(event.target.value)}
                required
              />
            </label>
            <label>
              Janela de saída — fim
              <input
                type="datetime-local"
                value={saidaFim}
                onChange={(event) => setSaidaFim(event.target.value)}
                required
              />
            </label>
          </>
        )}
        <button type="submit" disabled={criar.isPending || !cursos.isSuccess || cursos.data.length === 0}>
          {criar.isPending ? 'Salvando…' : 'Salvar'}
        </button>
      </form>
    </section>
  )
}
