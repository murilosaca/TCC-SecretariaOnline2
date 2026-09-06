import { FormEvent, useState } from 'react'
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { academicoApi } from '../../api/academico'
import { useActions } from '../../hooks/useActions'
import type { Curso } from '../../models/academico'

const vazio = { nome: '', sigla: '', codigo: '', horasFormativasMinimas: 120 }

export function Cursos() {
  const queryClient = useQueryClient()
  const [form, setForm] = useState(vazio)
  const [editandoId, setEditandoId] = useState<string | null>(null)
  const [erro, setErro] = useState<string | null>(null)

  const lista = useQuery({
    queryKey: ['cursos'],
    queryFn: () => academicoApi.listarCursos(),
  })

  const salvar = useMutation({
    mutationFn: () =>
      editandoId ? academicoApi.atualizarCurso(editandoId, form) : academicoApi.criarCurso(form),
    onSuccess: () => {
      setForm(vazio)
      setEditandoId(null)
      queryClient.invalidateQueries({ queryKey: ['cursos'] })
    },
    onError: () => setErro('Falha ao salvar o curso.'),
  })

  const excluir = useMutation({
    mutationFn: (id: string) => academicoApi.excluirCurso(id),
    onSuccess: () => queryClient.invalidateQueries({ queryKey: ['cursos'] }),
    onError: () => setErro('Falha ao excluir o curso.'),
  })

  function onSubmit(event: FormEvent) {
    event.preventDefault()
    salvar.mutate()
  }

  function editar(curso: Curso) {
    setEditandoId(curso.id)
    setForm({
      nome: curso.nome,
      sigla: curso.sigla,
      codigo: curso.codigo,
      horasFormativasMinimas: curso.horasFormativasMinimas,
    })
  }

  return (
    <section className="page">
      <header className="page-head">
        <h1>Cursos</h1>
        <p>CRUD alinhado a RF-F5-004-a. Ações somente via `_links` (useActions).</p>
      </header>
      {(erro || lista.isError) && (
        <div className="banner danger" role="alert">
          {erro ?? 'Não foi possível carregar os cursos.'}{' '}
          <button type="button" onClick={() => lista.refetch()}>
            Tentar de novo
          </button>
        </div>
      )}
      <form className="panel" onSubmit={onSubmit}>
        <h2>{editandoId ? 'Editar curso' : 'Novo curso'}</h2>
        <div className="grid">
          <label>
            Nome
            <input value={form.nome} onChange={(e) => setForm({ ...form, nome: e.target.value })} required />
          </label>
          <label>
            Sigla
            <input value={form.sigla} onChange={(e) => setForm({ ...form, sigla: e.target.value })} required />
          </label>
          <label>
            Código
            <input value={form.codigo} onChange={(e) => setForm({ ...form, codigo: e.target.value })} required />
          </label>
          <label>
            Horas formativas mínimas
            <input
              type="number"
              value={form.horasFormativasMinimas}
              onChange={(e) => setForm({ ...form, horasFormativasMinimas: Number(e.target.value) })}
            />
          </label>
        </div>
        <button type="submit" disabled={salvar.isPending}>
          Salvar
        </button>
        {editandoId && (
          <button
            type="button"
            className="ghost"
            onClick={() => {
              setEditandoId(null)
              setForm(vazio)
            }}
          >
            Cancelar
          </button>
        )}
      </form>
      {lista.isLoading && <p className="muted">Carregando cursos…</p>}
      {lista.data && lista.data.content.length === 0 && <p className="empty">Nenhum curso cadastrado.</p>}
      {lista.data && lista.data.content.length > 0 && (
        <table>
          <thead>
            <tr>
              <th>Sigla</th>
              <th>Nome</th>
              <th>Código</th>
              <th>Horas</th>
              <th></th>
            </tr>
          </thead>
          <tbody>
            {lista.data.content.map((curso) => (
              <CursoLinha
                key={curso.id}
                curso={curso}
                onEditar={editar}
                onExcluir={() => excluir.mutate(curso.id)}
              />
            ))}
          </tbody>
        </table>
      )}
    </section>
  )
}

function CursoLinha({
  curso,
  onEditar,
  onExcluir,
}: {
  curso: Curso
  onEditar: (curso: Curso) => void
  onExcluir: () => void
}) {
  const actions = useActions(curso._links)
  return (
    <tr>
      <td>{curso.sigla}</td>
      <td>{curso.nome}</td>
      <td>{curso.codigo}</td>
      <td>{curso.horasFormativasMinimas}</td>
      <td className="actions">
        {actions.can('atualizar') && (
          <button type="button" className="ghost" onClick={() => onEditar(curso)}>
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
