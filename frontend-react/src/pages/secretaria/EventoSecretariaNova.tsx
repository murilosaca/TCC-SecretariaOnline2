import { FormEvent, useEffect, useState } from 'react'
import { Link, useNavigate, useParams } from 'react-router-dom'
import { useMutation, useQuery } from '@tanstack/react-query'
import { academicoApi } from '../../api/academico'
import { ApiError } from '../../api/client'
import { eventosApi } from '../../api/eventos'
import type { AttendanceMode } from '../../models/evento'

const MODOS: { valor: AttendanceMode; rotulo: string }[] = [
  { valor: 'SECRET_SINGLE', rotulo: 'SECRET_SINGLE — PIN, só entrada' },
  { valor: 'SECRET_DUAL', rotulo: 'SECRET_DUAL — PIN, entrada e saída' },
  { valor: 'QR_SINGLE', rotulo: 'QR_SINGLE — token QR, só entrada' },
  { valor: 'QR_DUAL', rotulo: 'QR_DUAL — token QR, entrada e saída' },
]

export function EventoSecretariaFormulario() {
  const { id } = useParams()
  const navigate = useNavigate()
  const [titulo, setTitulo] = useState('')
  const [inicioEm, setInicioEm] = useState('')
  const [fimEm, setFimEm] = useState('')
  const [cargaHoraria, setCargaHoraria] = useState(4)
  const [attendanceMode, setAttendanceMode] = useState<AttendanceMode>('QR_SINGLE')
  const [cursoId, setCursoId] = useState('')

  const cursos = useQuery({
    queryKey: ['cursos', 'evento-form'],
    queryFn: () => academicoApi.listarCursos(0, 100),
  })
  const existente = useQuery({
    queryKey: ['eventos', id],
    queryFn: () => eventosApi.obter(id ?? ''),
    enabled: Boolean(id),
  })

  useEffect(() => {
    if (!existente.data) {
      return
    }
    setTitulo(existente.data.titulo)
    setInicioEm(existente.data.inicioEm.slice(0, 16))
    setFimEm(existente.data.fimEm.slice(0, 16))
    setCargaHoraria(existente.data.cargaHoraria)
    setAttendanceMode(existente.data.attendanceMode as AttendanceMode)
    setCursoId(existente.data.idCurso ?? '')
  }, [existente.data])

  const salvar = useMutation({
    mutationFn: () => {
      const body = {
        titulo: titulo.trim(),
        inicioEm: new Date(inicioEm).toISOString(),
        fimEm: new Date(fimEm).toISOString(),
        cargaHoraria,
        attendanceMode,
        cursoId,
      }
      return id ? eventosApi.atualizar(id, body) : eventosApi.criar(body)
    },
    onSuccess: () => navigate('/secretaria/eventos', { replace: true }),
  })

  function onSubmit(event: FormEvent) {
    event.preventDefault()
    salvar.mutate()
  }

  return (
    <section className="page">
      <header className="page-head">
        <div>
          <h1>{id ? 'Editar evento' : 'Novo evento'}</h1>
          <p className="muted">Janela ao vivo de 15 min. Sem horário pré-agendado nesta fatia.</p>
        </div>
        <Link to="/secretaria/eventos" className="ghost-link">
          Voltar
        </Link>
      </header>

      {salvar.isError && (
        <div className="banner danger" role="alert">
          {salvar.error instanceof ApiError ? salvar.error.message : 'Não foi possível salvar o evento.'}
        </div>
      )}

      <form className="attendance-card card" onSubmit={onSubmit}>
        <label>
          Título
          <input value={titulo} onChange={(event) => setTitulo(event.target.value)} required maxLength={200} />
        </label>
        <label>
          Curso
          <select required value={cursoId} onChange={(event) => setCursoId(event.target.value)}>
            <option value="">Selecione</option>
            {(cursos.data?.content ?? []).map((curso) => (
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
        <button type="submit" disabled={salvar.isPending}>
          {salvar.isPending ? 'Salvando…' : 'Salvar'}
        </button>
      </form>
    </section>
  )
}
