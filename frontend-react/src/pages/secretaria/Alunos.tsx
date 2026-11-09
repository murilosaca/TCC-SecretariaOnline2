import { FormEvent, useState } from 'react'
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { academicoApi } from '../../api/academico'
import { ApiError } from '../../api/client'
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
  const [resetAluno, setResetAluno] = useState<Aluno | null>(null)
  const [matriculaAluno, setMatriculaAluno] = useState<Aluno | null>(null)

  const cursos = useQuery({ queryKey: ['cursos', 0, 100], queryFn: () => academicoApi.listarCursos(0, 100) })
  const lista = useQuery({
    queryKey: ['alunos', termo],
    queryFn: () => academicoApi.listarAlunos(undefined, termo || undefined),
  })
  const colecao = useActions(lista.data?._links)
  const mostrarForm = editandoId != null || colecao.can('criar')
  const forbidden = lista.isError && lista.error instanceof ApiError && lista.error.status === 403
  const cursosIndisponiveis = cursos.isError
  const grrInvalido = Boolean(erro?.toLowerCase().includes('grr'))

  const salvar = useMutation({
    mutationFn: () =>
      editandoId ? academicoApi.atualizarAluno(editandoId, form) : academicoApi.criarAluno(form),
    onSuccess: () => {
      setForm(vazio)
      setEditandoId(null)
      setErro(null)
      queryClient.invalidateQueries({ queryKey: ['alunos'] })
    },
    onError: (error) => {
      setErro(error instanceof ApiError ? error.message : 'Falha ao salvar o aluno. Confira o GRR e o e-mail.')
    },
  })

  const excluir = useMutation({
    mutationFn: (id: string) => academicoApi.excluirAluno(id),
    onSuccess: () => queryClient.invalidateQueries({ queryKey: ['alunos'] }),
    onError: () => setErro('Falha ao excluir o aluno.'),
  })

  const resetSenha = useMutation({
    mutationFn: (aluno: Aluno) => academicoApi.resetSenhaAluno(aluno._links?.['reset-password'] ?? ''),
    onSuccess: () => {
      setResetAluno(null)
      setErro(null)
    },
    onError: (error) => {
      setErro(error instanceof ApiError ? error.message : 'Falha ao enviar o link de redefinição.')
    },
  })

  function onSubmit(event: FormEvent) {
    event.preventDefault()
    setErro(null)
    salvar.mutate()
  }

  function editar(aluno: Aluno) {
    setEditandoId(aluno.id)
    setErro(null)
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
        <p>Cadastro acadêmico com GRR. A senha de acesso não é exibida para a secretaria.</p>
      </header>
      {forbidden && (
        <p className="empty" role="status">
          Você não tem permissão para gerenciar alunos.
        </p>
      )}
      {cursosIndisponiveis && (
        <div className="banner danger" role="alert">
          Não foi possível carregar os cursos. Sem eles o cadastro de alunos fica indisponível
          (nesta fatia as capabilities de cadastro acadêmico andam juntas).
        </div>
      )}
      {(erro || (lista.isError && !forbidden)) && (
        <div className="banner danger" role="alert">
          {erro ?? 'Não foi possível carregar os alunos.'}
        </div>
      )}
      {mostrarForm && (
      <form className="panel" onSubmit={onSubmit}>
        <fieldset disabled={cursosIndisponiveis}>
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
            <input
              value={form.grr}
              onChange={(e) => setForm({ ...form, grr: e.target.value })}
              placeholder="GRR20241234"
              aria-invalid={grrInvalido || undefined}
              required
            />
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
        <button type="submit" disabled={salvar.isPending || cursosIndisponiveis}>
          Salvar
        </button>
        </fieldset>
      </form>
      )}
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
              <th>Ações</th>
            </tr>
          </thead>
          <tbody>
            {lista.data.content.map((aluno) => (
              <AlunoLinha
                key={aluno.id}
                aluno={aluno}
                onEditar={editar}
                onExcluir={() => excluir.mutate(aluno.id)}
                onReset={() => setResetAluno(aluno)}
                onMatricula={() => setMatriculaAluno(aluno)}
              />
            ))}
          </tbody>
        </table>
      )}
      {resetAluno && (
        <div className="banner warning" role="alertdialog" aria-labelledby="reset-aluno-titulo">
          <p id="reset-aluno-titulo">
            Enviar link de redefinição para <strong>{resetAluno.nome}</strong>? A senha não será exibida.
          </p>
          <button type="button" disabled={resetSenha.isPending} onClick={() => resetSenha.mutate(resetAluno)}>
            Confirmar envio
          </button>
          <button type="button" className="ghost" onClick={() => setResetAluno(null)}>
            Cancelar
          </button>
        </div>
      )}
      {matriculaAluno && (
        <DrawerMatricula aluno={matriculaAluno} onClose={() => setMatriculaAluno(null)} />
      )}
    </section>
  )
}

function AlunoLinha({
  aluno,
  onEditar,
  onExcluir,
  onReset,
  onMatricula,
}: {
  aluno: Aluno
  onEditar: (aluno: Aluno) => void
  onExcluir: () => void
  onReset: () => void
  onMatricula: () => void
}) {
  const actions = useActions(aluno._links)
  const semAcao = !actions.can('edit') && !actions.can('reset-password') && !actions.can('matricula')
  return (
    <tr>
      <td>{aluno.grr}</td>
      <td>{aluno.nomeSocial || aluno.nome}</td>
      <td>{aluno.emailInstitucional}</td>
      <td>{aluno.situacao}</td>
      <td className="actions">
        {actions.can('edit') && (
          <button type="button" className="ghost" onClick={() => onEditar(aluno)}>
            Editar
          </button>
        )}
        {actions.can('reset-password') && (
          <button type="button" className="ghost" onClick={onReset}>
            Reset senha
          </button>
        )}
        {actions.can('matricula') && (
          <button type="button" className="ghost" onClick={onMatricula}>
            Matricular
          </button>
        )}
        {actions.can('excluir') && (
          <button type="button" className="danger" onClick={onExcluir}>
            Excluir
          </button>
        )}
        {semAcao && <span title="Sem permissão para este curso">Sem permissão para este curso</span>}
      </td>
    </tr>
  )
}

function DrawerMatricula({ aluno, onClose }: { aluno: Aluno; onClose: () => void }) {
  const queryClient = useQueryClient()
  const [selecionadas, setSelecionadas] = useState<string[]>([])
  const [erro, setErro] = useState<string | null>(null)
  const opcoes = useQuery({
    queryKey: ['matricula-opcoes', aluno.id],
    queryFn: () => academicoApi.opcoesMatricula(aluno.id),
  })
  const matricular = useMutation({
    mutationFn: () => academicoApi.matricularAluno(aluno._links?.matricula ?? '', selecionadas),
    onSuccess: () => {
      setErro(null)
      queryClient.invalidateQueries({ queryKey: ['matricula-opcoes', aluno.id] })
      onClose()
    },
    onError: (error) => {
      setErro(error instanceof ApiError ? error.message : 'Falha ao matricular.')
    },
  })
  const semPeriodo = opcoes.data != null && !opcoes.data.periodoId
  const semDisciplina = opcoes.data != null && opcoes.data.disciplinas.length === 0

  function alternar(id: string) {
    setSelecionadas((atual) => (atual.includes(id) ? atual.filter((item) => item !== id) : [...atual, id]))
  }

  return (
    <>
      <div className="drawer-backdrop" onClick={onClose} />
      <aside className="drawer" role="dialog" aria-modal="true" aria-labelledby="matricula-titulo">
        <h2 id="matricula-titulo">Matricular {aluno.nome}</h2>
        <p>Disciplinas do curso no período vigente. A secretaria confirma o lote; sem vaga, nada é gravado.</p>
        {opcoes.isLoading && <p className="muted">Carregando disciplinas…</p>}
        {opcoes.isError && (
          <div className="banner danger" role="alert">
            Não foi possível carregar as disciplinas para matrícula.
          </div>
        )}
        {erro && (
          <div className="banner danger" role="alert">
            {erro}
          </div>
        )}
        {semPeriodo && <p className="empty">Não há período letivo vigente.</p>}
        {!semPeriodo && semDisciplina && <p className="empty">Nenhuma disciplina ativa neste curso.</p>}
        {opcoes.data?.periodoRotulo && <p>Período {opcoes.data.periodoRotulo}</p>}
        {opcoes.data && opcoes.data.disciplinas.length > 0 && (
          <ul>
            {opcoes.data.disciplinas.map((disciplina) => (
              <li key={disciplina.id}>
                <label>
                  <input
                    type="checkbox"
                    checked={selecionadas.includes(disciplina.id)}
                    onChange={() => alternar(disciplina.id)}
                  />
                  {disciplina.codigo} — {disciplina.nome} ({disciplina.vagas} vagas)
                </label>
              </li>
            ))}
          </ul>
        )}
        <button
          type="button"
          disabled={matricular.isPending || selecionadas.length === 0 || semPeriodo}
          onClick={() => matricular.mutate()}
        >
          Confirmar matrícula
        </button>
        <button type="button" className="ghost" onClick={onClose}>
          Fechar
        </button>
      </aside>
    </>
  )
}
