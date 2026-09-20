import { FormEvent, useState } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import { useMutation } from '@tanstack/react-query'
import { ApiError } from '../../api/client'
import { eventosApi } from '../../api/eventos'
import type { AttendanceMode } from '../../models/evento'

const MODOS: { valor: AttendanceMode; rotulo: string }[] = [
  { valor: 'SECRET_SINGLE', rotulo: 'SECRET_SINGLE — PIN, só entrada' },
  { valor: 'SECRET_DUAL', rotulo: 'SECRET_DUAL — PIN, entrada e saída' },
  { valor: 'QR_SINGLE', rotulo: 'QR_SINGLE — token QR, só entrada' },
  { valor: 'QR_DUAL', rotulo: 'QR_DUAL — token QR, entrada e saída' },
]

export function ProfessorEventoNova() {
  const navigate = useNavigate()
  const [titulo, setTitulo] = useState('')
  const [inicioEm, setInicioEm] = useState('')
  const [fimEm, setFimEm] = useState('')
  const [cargaHoraria, setCargaHoraria] = useState(4)
  const [attendanceMode, setAttendanceMode] = useState<AttendanceMode>('SECRET_SINGLE')

  const criar = useMutation({
    mutationFn: () =>
      eventosApi.criar({
        titulo: titulo.trim(),
        inicioEm: new Date(inicioEm).toISOString(),
        fimEm: new Date(fimEm).toISOString(),
        cargaHoraria,
        attendanceMode,
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

      <form className="attendance-card card" onSubmit={onSubmit}>
        <label>
          Título
          <input value={titulo} onChange={(event) => setTitulo(event.target.value)} required maxLength={200} />
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
        {attendanceMode.endsWith('_DUAL') && (
          <p className="muted">
            Janelas de entrada e saída são abertas ao vivo no painel de operação (15 min cada). Horários
            pré-agendados no formulário não entram nesta fatia.
          </p>
        )}
        <button type="submit" disabled={criar.isPending}>
          {criar.isPending ? 'Salvando…' : 'Salvar'}
        </button>
      </form>
    </section>
  )
}
