import { FormEvent, useState } from 'react'
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { academicoApi } from '../../api/academico'
import { useActions } from '../../hooks/useActions'
import type { Aluno, AlunoSituacao } from '../../models/academico'

const situacoes: AlunoSituacao[] = ['MATRICULADO', 'TRANCADO', 'FORMANDO', 'EGRESSO', 'DESLIGADO']
const vazio = {
  nome: '',
  nomeSocial: '',
  grr: '',
  emailInstitucional: '',
  emailPessoal: '',
  telefone: '',
  idCurso: '',
  situacao: 'MATRICULADO' as AlunoSituacao,
}

export function Alunos() {
  const queryClient = useQueryClient()
  const [form, setForm] = useState(vazio)
  const [editandoId, setEditandoId] = useState<string | null>(null)
  const [termo, setTermo] = useState('')
  const [erro, setErro] = useState<string | null>(null)

  const cursos = useQuery({ queryKey: ['cursos'], queryFn: () => academicoApi.listarCursos(0, 100) })
  const lista = useQuery({
    queryKey: ['alunos', termo],
    queryFn: () => academicoApi.listarAlunos(undefined, termo || undefined),
  })

  const salvar = useMutation({
    mutationFn: () =>
      editandoId ? academicoApi.atualizarAluno(editandoId, form) : academicoApi.criarAluno(form),
    onSuccess: () => {
      setForm(vazio)
      setEditandoId(null)
      queryClient.invalidateQueries({ queryKey: ['alunos'] })
    },
    onError: () => setErro('Falha ao salvar o aluno. Confira o GRR (GRR + 8 dígitos) e o e-mail.'),
  })

  const excluir = useMutation({
    mutationFn: (id: string) => academicoApi.excluirAluno(id),
    onSuccess: () => queryClient.invalidateQueries({ queryKey: ['alunos'] }),
    onError: () => setErro('Falha ao excluir o aluno.'),
  })

  function onSubmit(event: FormEvent) {
    event.preventDefault()
    salvar.mutate()
  }

  function editar(aluno: Aluno) {
    setEditandoId(aluno.id)
    setForm({
      nome: aluno.nome,
      nomeSocial: aluno.nomeSocial ?? '',
      grr: aluno.grr,
      emailInstitucional: aluno.emailInstitucional,
      emailPessoal: aluno.emailPessoal ?? '',
      telefone: aluno.telefone ?? '',
      idCurso: aluno.idCurso,
      situacao: aluno.situacao,
    })
  }

  return (
    <section className="page">
      <header className="page-head">
        <h1>Alunos</h1>
        <p>Cadastro acadêmico com GRR. O campo idade do legado foi descartado (RF-F5-003).</p>
      </header>
      {(erro || lista.isError) && (
        <div className="banner danger" role="alert">
          {erro ?? 'Não foi possível carregar os alunos.'}
        </div>
      )}
      <form className="panel" onSubmit={onSubmit}>
        <h2>{editandoId ? 'Editar aluno' : 'Novo aluno'}</h2>
        <div className="grid">
          <label>
            Nome
            <input value={form.nome} onChange={(e) => setForm({ ...form, nome: e.target.value })} required />
          </label>
          <label>
            Nome social
            <input value={form.nomeSocial} onChange={(e) => setForm({ ...form, nomeSocial: e.target.value })} />
          </label>
          <label>
            GRR
            <input value={form.grr} onChange={(e) => setForm({ ...form, grr: e.target.value })} placeholder="GRR20241234" required />
          </label>
          <label>
            E-mail institucional
            <input
              value={form.emailInstitucional}
              onChange={(e) => setForm({ ...form, emailInstitucional: e.target.value })}
              required
            />
          </label>
          <label>
            E-mail pessoal
            <input value={form.emailPessoal} onChange={(e) => setForm({ ...form, emailPessoal: e.target.value })} />
          </label>
          <label>
            Telefone
            <input value={form.telefone} onChange={(e) => setForm({ ...form, telefone: e.target.value })} />
          </label>
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
            Situação
            <select
              value={form.situacao}
              onChange={(e) => setForm({ ...form, situacao: e.target.value as AlunoSituacao })}
            >
              {situacoes.map((item) => (
                <option key={item} value={item}>
                  {item}
                </option>
              ))}
            </select>
          </label>
        </div>
        <button type="submit" disabled={salvar.isPending}>
          Salvar
        </button>
      </form>
      <label className="filter">
        Busca
        <input value={termo} onChange={(e) => setTermo(e.target.value)} placeholder="Nome, GRR ou e-mail" />
      </label>
      {lista.isLoading && <p className="muted">Carregando alunos…</p>}
      {lista.data && lista.data.content.length === 0 && <p className="empty">Nenhum aluno encontrado.</p>}
      {lista.data && lista.data.content.length > 0 && (
        <table>
          <thead>
            <tr>
              <th>GRR</th>
              <th>Nome</th>
              <th>E-mail</th>
              <th>Situação</th>
              <th></th>
            </tr>
          </thead>
          <tbody>
            {lista.data.content.map((aluno) => (
              <AlunoLinha
                key={aluno.id}
                aluno={aluno}
                onEditar={editar}
                onExcluir={() => excluir.mutate(aluno.id)}
              />
            ))}
          </tbody>
        </table>
      )}
    </section>
  )
}

function AlunoLinha({
  aluno,
  onEditar,
  onExcluir,
}: {
  aluno: Aluno
  onEditar: (aluno: Aluno) => void
  onExcluir: () => void
}) {
  const actions = useActions(aluno._links)
  return (
    <tr>
      <td>{aluno.grr}</td>
      <td>{aluno.nomeSocial || aluno.nome}</td>
      <td>{aluno.emailInstitucional}</td>
      <td>{aluno.situacao}</td>
      <td className="actions">
        {actions.can('atualizar') && (
          <button type="button" className="ghost" onClick={() => onEditar(aluno)}>
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
