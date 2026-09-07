import { FormEvent, useState } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import { useMutation } from '@tanstack/react-query'
import { ApiError } from '../../api/client'
import { eventosApi } from '../../api/eventos'

export function ProfessorEventoNova() {
  const navigate = useNavigate()
  const [titulo, setTitulo] = useState('')
  const [inicioEm, setInicioEm] = useState('')
  const [fimEm, setFimEm] = useState('')
  const [cargaHoraria, setCargaHoraria] = useState(4)

  const criar = useMutation({
    mutationFn: () =>
      eventosApi.criar({
        titulo: titulo.trim(),
        inicioEm: new Date(inicioEm).toISOString(),
        fimEm: new Date(fimEm).toISOString(),
        cargaHoraria,
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
          <p className="muted">SECRET_SINGLE neste sprint. O PIN não aparece após criar.</p>
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
        <button type="submit" disabled={criar.isPending}>
          {criar.isPending ? 'Salvando…' : 'Salvar'}
        </button>
      </form>
    </section>
  )
}
