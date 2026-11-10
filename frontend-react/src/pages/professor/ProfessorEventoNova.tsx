import { FormEvent, useState } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import { useMutation, useQuery } from '@tanstack/react-query'
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

function paraIso(valor: string): string {
  return new Date(valor).toISOString()
}

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
  const [cursoId, setCursoId] = useState('')
  const [inicioEm, setInicioEm] = useState('')
  const [fimEm, setFimEm] = useState('')
  const [attendanceMode, setAttendanceMode] = useState<AttendanceMode>('SECRET_SINGLE')
  const cargaHoraria = cargaHorariaDeIntervalo(inicioEm, fimEm)

  const cursos = useQuery({
    queryKey: ['eventos', 'cursos-alvo'],
    queryFn: () => eventosApi.cursosAlvo(),
    retry: false,
  })

  const criar = useMutation({
    mutationFn: () => {
      if (cargaHoraria == null) {
        throw new Error('Informe início e fim válidos para calcular a carga horária.')
      }
      return eventosApi.criar({
        titulo: titulo.trim(),
        inicioEm: paraIso(inicioEm),
        fimEm: paraIso(fimEm),
        cargaHoraria,
        attendanceMode,
        cursoId,
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
            Defina agenda, público-alvo e modo de presença. As janelas de PIN/QR são abertas ao vivo no
            painel de operação.
          </p>
        </div>
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

      <div className="evento-nova-layout">
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
          <button
            type="submit"
            disabled={
              criar.isPending || cargaHoraria == null || !cursos.isSuccess || cursos.data.length === 0
            }
          >
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
