import { FormEvent, useState } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import { useMutation } from '@tanstack/react-query'
import { ApiError } from '../../api/client'
import { eventosApi } from '../../api/eventos'
import { rotuloCodigo } from '../../lib/rotulos'
import type { AttendanceMode } from '../../models/evento'

const MODOS: AttendanceMode[] = ['SECRET_SINGLE', 'SECRET_DUAL', 'QR_SINGLE', 'QR_DUAL']

const DESCRICOES_MODO: { modo: AttendanceMode; texto: string }[] = [
  {
    modo: 'SECRET_SINGLE',
    texto: 'O aluno digita um PIN apenas na entrada do evento.',
  },
  {
    modo: 'SECRET_DUAL',
    texto: 'PIN na entrada e outro na saída, para comprovar permanência.',
  },
  {
    modo: 'QR_SINGLE',
    texto: 'O aluno escaneia um QR Code apenas na entrada.',
  },
  {
    modo: 'QR_DUAL',
    texto: 'QR na entrada e outro na saída, para comprovar permanência.',
  },
]

function cargaHorariaDeIntervalo(inicioEm: string, fimEm: string): number | null {
  if (!inicioEm || !fimEm) {
    return null
  }
  const ms = new Date(fimEm).getTime() - new Date(inicioEm).getTime()
  if (!(ms > 0)) {
    return null
  }
  return Math.max(1, Math.ceil(ms / (1000 * 60 * 60)))
}

export function ProfessorEventoNova() {
  const navigate = useNavigate()
  const [titulo, setTitulo] = useState('')
  const [inicioEm, setInicioEm] = useState('')
  const [fimEm, setFimEm] = useState('')
  const [attendanceMode, setAttendanceMode] = useState<AttendanceMode>('SECRET_SINGLE')

  const cargaHoraria = cargaHorariaDeIntervalo(inicioEm, fimEm)

  const criar = useMutation({
    mutationFn: () => {
      if (cargaHoraria == null) {
        throw new Error('Informe início e fim válidos para calcular a carga horária.')
      }
      return eventosApi.criar({
        titulo: titulo.trim(),
        inicioEm: new Date(inicioEm).toISOString(),
        fimEm: new Date(fimEm).toISOString(),
        cargaHoraria,
        attendanceMode,
      })
    },
    onSuccess: (evento) => navigate(`/professor/eventos/${evento.id}`, { replace: true }),
  })

  function onSubmit(event: FormEvent) {
    event.preventDefault()
    if (cargaHoraria == null) {
      return
    }
    criar.mutate()
  }

  return (
    <section className="page">
      <p>
        <Link to="/professor/eventos" className="ghost-link">
          ← Eventos
        </Link>
      </p>
      <header className="page-head">
        <div>
          <h1>Novo evento</h1>
          <p className="muted">
            Defina agenda e modo de presença. O PIN ou o QR só aparecem no painel de operação ao abrir a
            janela.
          </p>
        </div>
      </header>

      {criar.isError && (
        <div className="banner danger" role="alert">
          {criar.error instanceof ApiError ? criar.error.message : 'Não foi possível criar o evento.'}
        </div>
      )}

      <div className="evento-nova-layout">
        <form className="attendance-card card" onSubmit={onSubmit}>
          <label>
            Título
            <input value={titulo} onChange={(event) => setTitulo(event.target.value)} required maxLength={200} />
          </label>
          <label>
            Início
            <input
              type="datetime-local"
              value={inicioEm}
              onChange={(event) => setInicioEm(event.target.value)}
              required
            />
          </label>
          <label>
            Fim
            <input
              type="datetime-local"
              value={fimEm}
              onChange={(event) => setFimEm(event.target.value)}
              required
            />
          </label>
          <div className="campo-calculado">
            <span className="campo-calculado-rotulo">Carga horária</span>
            <p className="campo-calculado-valor" aria-live="polite">
              {cargaHoraria == null
                ? 'Calculada automaticamente a partir do início e do fim.'
                : `${cargaHoraria}h`}
            </p>
          </div>
          <label>
            Modo de presença
            <select
              value={attendanceMode}
              onChange={(event) => setAttendanceMode(event.target.value as AttendanceMode)}
            >
              {MODOS.map((modo) => (
                <option key={modo} value={modo}>
                  {rotuloCodigo(modo)}
                </option>
              ))}
            </select>
          </label>
          {attendanceMode.endsWith('_DUAL') && (
            <p className="muted">
              Janelas de entrada e saída são abertas ao vivo no painel de operação (15 min cada).
            </p>
          )}
          <button type="submit" disabled={criar.isPending || cargaHoraria == null}>
            {criar.isPending ? 'Salvando…' : 'Salvar'}
          </button>
        </form>

        <aside className="panel evento-nova-ajuda" aria-labelledby="modos-presenca-titulo">
          <h2 id="modos-presenca-titulo">Modos de presença</h2>
          <p className="muted">
            Escolha conforme o nível de comprovação necessário. Modos duplos pedem validação na entrada e
            na saída (Proof of Stay).
          </p>
          <dl className="evento-modos-lista">
            {DESCRICOES_MODO.map((item) => (
              <div key={item.modo}>
                <dt>{rotuloCodigo(item.modo)}</dt>
                <dd>{item.texto}</dd>
              </div>
            ))}
          </dl>
        </aside>
      </div>
    </section>
  )
}
