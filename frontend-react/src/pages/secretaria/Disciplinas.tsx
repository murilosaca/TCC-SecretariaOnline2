import { FormEvent, useState } from 'react'
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { academicoApi } from '../../api/academico'
import { useActions } from '../../hooks/useActions'
import type { Disciplina } from '../../models/academico'

const vazio = {
  idCurso: '',
  codigo: '',
  nome: '',
  periodo: 1,
  cargaHorariaTotal: 60,
  creditos: 4,
}

export function Disciplinas() {
  const queryClient = useQueryClient()
  const [form, setForm] = useState(vazio)
  const [editandoId, setEditandoId] = useState<string | null>(null)
  const [erro, setErro] = useState<string | null>(null)

  const cursos = useQuery({ queryKey: ['cursos'], queryFn: () => academicoApi.listarCursos(0, 100) })
  const lista = useQuery({ queryKey: ['disciplinas'], queryFn: () => academicoApi.listarDisciplinas() })

  const salvar = useMutation({
    mutationFn: () =>
      editandoId ? academicoApi.atualizarDisciplina(editandoId, form) : academicoApi.criarDisciplina(form),
    onSuccess: () => {
      setForm(vazio)
      setEditandoId(null)
      queryClient.invalidateQueries({ queryKey: ['disciplinas'] })
    },
    onError: () => setErro('Falha ao salvar a disciplina.'),
  })

  const excluir = useMutation({
    mutationFn: (id: string) => academicoApi.excluirDisciplina(id),
    onSuccess: () => queryClient.invalidateQueries({ queryKey: ['disciplinas'] }),
    onError: () => setErro('Falha ao excluir a disciplina.'),
  })

  function onSubmit(event: FormEvent) {
    event.preventDefault()
    salvar.mutate()
  }

  function editar(disciplina: Disciplina) {
    setEditandoId(disciplina.id)
    setForm({
      idCurso: disciplina.idCurso,
      codigo: disciplina.codigo,
      nome: disciplina.nome,
      periodo: disciplina.periodo,
      cargaHorariaTotal: disciplina.cargaHorariaTotal,
      creditos: disciplina.creditos,
    })
  }

  return (
    <section className="page">
      <header className="page-head">
        <h1>Disciplinas</h1>
        <p>Código único por curso (RF-F5-004-b).</p>
      </header>
      {(erro || lista.isError) && (
        <div className="banner danger" role="alert">
          {erro ?? 'Não foi possível carregar as disciplinas.'}
        </div>
      )}
      <form className="panel" onSubmit={onSubmit}>
        <h2>{editandoId ? 'Editar disciplina' : 'Nova disciplina'}</h2>
        <div className="grid">
          <label>
            Curso
            <select value={form.idCurso} onChange={(e) => setForm({ ...form, idCurso: e.target.value })} required>
              <option value="">Selecione</option>
              {cursos.data?.content.map((curso) => (
                <option key={curso.id} value={curso.id}>
                  {curso.sigla} — {curso.nome}
                </option>
              ))}
            </select>
          </label>
          <label>
            Código
            <input value={form.codigo} onChange={(e) => setForm({ ...form, codigo: e.target.value })} required />
          </label>
          <label>
            Nome
            <input value={form.nome} onChange={(e) => setForm({ ...form, nome: e.target.value })} required />
          </label>
          <label>
            Período
            <input
              type="number"
              value={form.periodo}
              onChange={(e) => setForm({ ...form, periodo: Number(e.target.value) })}
            />
          </label>
          <label>
            Carga horária
            <input
              type="number"
              value={form.cargaHorariaTotal}
              onChange={(e) => setForm({ ...form, cargaHorariaTotal: Number(e.target.value) })}
            />
          </label>
          <label>
            Créditos
            <input
              type="number"
              value={form.creditos}
              onChange={(e) => setForm({ ...form, creditos: Number(e.target.value) })}
            />
          </label>
        </div>
        <button type="submit" disabled={salvar.isPending}>
          Salvar
        </button>
      </form>
      {lista.isLoading && <p className="muted">Carregando disciplinas…</p>}
      {lista.data && lista.data.content.length === 0 && <p className="empty">Nenhuma disciplina cadastrada.</p>}
      {lista.data && lista.data.content.length > 0 && (
        <table>
          <thead>
            <tr>
              <th>Código</th>
              <th>Nome</th>
              <th>Período</th>
              <th>CH</th>
              <th></th>
            </tr>
          </thead>
          <tbody>
            {lista.data.content.map((disciplina) => (
              <DisciplinaLinha
                key={disciplina.id}
                disciplina={disciplina}
                onEditar={editar}
                onExcluir={() => excluir.mutate(disciplina.id)}
              />
            ))}
          </tbody>
        </table>
      )}
    </section>
  )
}

function DisciplinaLinha({
  disciplina,
  onEditar,
  onExcluir,
}: {
  disciplina: Disciplina
  onEditar: (disciplina: Disciplina) => void
  onExcluir: () => void
}) {
  const actions = useActions(disciplina._links)
  return (
    <tr>
      <td>{disciplina.codigo}</td>
      <td>{disciplina.nome}</td>
      <td>{disciplina.periodo}</td>
      <td>{disciplina.cargaHorariaTotal}</td>
      <td className="actions">
        {actions.can('atualizar') && (
          <button type="button" className="ghost" onClick={() => onEditar(disciplina)}>
            Editar
          </button>
        )}
        {actions.can('excluir') && (
          <button type="button" className="danger" onClick={onExcluir}>
            Excluir
          </button>
        )}
      </td>
    </tr>
  )
}
