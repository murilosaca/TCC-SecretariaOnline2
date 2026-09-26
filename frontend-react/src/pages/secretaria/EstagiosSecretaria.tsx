import { FormEvent, useMemo, useState } from 'react'
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { academicoApi } from '../../api/academico'
import { ApiError } from '../../api/client'
import { estagiosApi } from '../../api/estagios'
import { useActions } from '../../hooks/useActions'
import { rotuloSituacaoEstagio, vigencia } from '../../lib/estagio'
import type { Estagio } from '../../models/estagio'

const vazio = {
  alunoId: '',
  alunoRotulo: '',
  empresa: '',
  supervisor: '',
  inicio: '',
  fim: '',
}

export function EstagiosSecretaria() {
  const queryClient = useQueryClient()
  const [form, setForm] = useState(vazio)
  const [editandoId, setEditandoId] = useState<string | null>(null)
  const [termo, setTermo] = useState('')
  const [erro, setErro] = useState<string | null>(null)

  const lista = useQuery({
    queryKey: ['estagios', 'secretaria'],
    queryFn: () => estagiosApi.listarDoEscopo(),
  })
  const colecao = useActions(lista.data?._links)
  const mostrarForm = editandoId != null || colecao.can('novo')
  const forbidden = lista.isError && lista.error instanceof ApiError && lista.error.status === 403

  const alunos = useQuery({
    queryKey: ['alunos', 'estagio-secretaria', termo],
    queryFn: () => academicoApi.listarAlunos(undefined, termo || undefined, 0, 100),
    enabled: mostrarForm && editandoId == null,
  })

  const opcoes = useMemo(() => {
    const conteudo = alunos.data?.content ?? []
    if (form.alunoId && !conteudo.some((item) => item.id === form.alunoId)) {
      return [{ id: form.alunoId, grr: '', nome: form.alunoRotulo || 'Aluno do estágio' }, ...conteudo]
    }
    return conteudo
  }, [alunos.data, form.alunoId, form.alunoRotulo])

  const salvar = useMutation({
    mutationFn: () => {
      const body = {
        alunoId: form.alunoId,
        empresa: form.empresa,
        supervisor: form.supervisor,
        inicio: form.inicio,
        fim: form.fim,
      }
      return editandoId ? estagiosApi.atualizar(editandoId, body) : estagiosApi.criar(body)
    },
    onSuccess: () => {
      setForm(vazio)
      setEditandoId(null)
      setTermo('')
      setErro(null)
      queryClient.invalidateQueries({ queryKey: ['estagios', 'secretaria'] })
    },
    onError: (falha) => {
      setErro(falha instanceof ApiError ? falha.message : 'Falha ao salvar o estágio.')
    },
  })

  function onSubmit(event: FormEvent) {
    event.preventDefault()
    setErro(null)
    salvar.mutate()
  }

  function editar(item: Estagio) {
    setEditandoId(item.id)
    setErro(null)
    setForm({
      alunoId: item.idAluno,
      alunoRotulo: item.alunoNome ?? 'Aluno do estágio',
      empresa: item.empresa,
      supervisor: item.supervisor,
      inicio: item.inicio.slice(0, 10),
      fim: item.fim.slice(0, 10),
    })
  }

  function cancelar() {
    setEditandoId(null)
    setForm(vazio)
    setErro(null)
  }

  const alunosIndisponiveis = mostrarForm && editandoId == null && alunos.isError

  return (
    <section className="page">
      <header className="page-head">
        <h1>Cadastro de estágios</h1>
        <p className="muted">
          Estágios dos cursos vinculados à secretaria. O orientador nasce vazio; a comissão COE atribui depois.
        </p>
      </header>

      {forbidden && (
        <p className="empty" role="status">
          Você não tem permissão para registrar estágios.
        </p>
      )}
      {(erro || (lista.isError && !forbidden)) && (
        <div className="banner danger" role="alert">
          {erro ?? 'Não foi possível carregar os estágios.'}{' '}
          {lista.isError && !forbidden && (
            <button type="button" onClick={() => lista.refetch()}>
              Tentar de novo
            </button>
          )}
        </div>
      )}
      {alunosIndisponiveis && (
        <div className="banner danger" role="alert">
          Não foi possível carregar os alunos do escopo. O registro fica indisponível sem essa lista.
        </div>
      )}

      {mostrarForm && (
        <form className="panel" onSubmit={onSubmit}>
          <h2>{editandoId ? 'Editar estágio' : 'Novo estágio'}</h2>
          <p className="muted">O aluno é escolhido na lista do curso. O identificador não é digitado.</p>
          <div className="grid">
            {editandoId == null && (
              <label>
                Buscar aluno
                <input
                  value={termo}
                  onChange={(event) => setTermo(event.target.value)}
                  placeholder="Nome, GRR ou e-mail"
                />
              </label>
            )}
            <label>
              Aluno
              <select
                value={form.alunoId}
                onChange={(event) => setForm({ ...form, alunoId: event.target.value })}
                required
                disabled={editandoId != null || alunos.isLoading || alunosIndisponiveis}
              >
                <option value="">Selecione</option>
                {opcoes.map((aluno) => (
                  <option key={aluno.id} value={aluno.id}>
                    {aluno.grr ? `${aluno.grr} — ${aluno.nome}` : aluno.nome}
                  </option>
                ))}
              </select>
            </label>
            <label>
              Empresa
              <input
                value={form.empresa}
                onChange={(event) => setForm({ ...form, empresa: event.target.value })}
                required
                maxLength={200}
              />
            </label>
            <label>
              Supervisor
              <input
                value={form.supervisor}
                onChange={(event) => setForm({ ...form, supervisor: event.target.value })}
                required
                maxLength={200}
              />
            </label>
            <label>
              Início
              <input
                type="date"
                value={form.inicio}
                onChange={(event) => setForm({ ...form, inicio: event.target.value })}
                required
              />
            </label>
            <label>
              Fim
              <input
                type="date"
                value={form.fim}
                onChange={(event) => setForm({ ...form, fim: event.target.value })}
                required
              />
            </label>
          </div>
          {alunos.isLoading && editandoId == null && (
            <p className="muted" aria-busy="true">
              Carregando alunos…
            </p>
          )}
          {editandoId == null && alunos.data && alunos.data.page.totalElements > alunos.data.content.length && (
            <p className="muted">Há mais alunos neste escopo. Refine a busca por nome, GRR ou e-mail.</p>
          )}
          <button type="submit" disabled={salvar.isPending || alunosIndisponiveis || (editandoId == null && !form.alunoId)}>
            Salvar
          </button>
          {editandoId && (
            <button type="button" className="ghost" onClick={cancelar}>
              Cancelar
            </button>
          )}
        </form>
      )}

      {lista.isLoading && (
        <p className="muted" aria-busy="true">
          Carregando estágios…
        </p>
      )}
      {lista.data && lista.data.content.length === 0 && (
        <p className="empty" role="status">
          Nenhum estágio registrado nos cursos da secretaria.
        </p>
      )}
      {lista.data && lista.data.content.length > 0 && (
        <table>
          <thead>
            <tr>
              <th scope="col">Aluno</th>
              <th scope="col">Empresa</th>
              <th scope="col">Supervisor</th>
              <th scope="col">Vigência</th>
              <th scope="col">Situação</th>
              <th scope="col">Orientador</th>
              <th scope="col">Ações</th>
            </tr>
          </thead>
          <tbody>
            {lista.data.content.map((item) => (
              <Linha key={item.id} item={item} onEditar={editar} />
            ))}
          </tbody>
        </table>
      )}
    </section>
  )
}

function Linha({ item, onEditar }: { item: Estagio; onEditar: (item: Estagio) => void }) {
  const actions = useActions(item._links)
  return (
    <tr>
      <td>{item.alunoNome ?? '—'}</td>
      <td>{item.empresa}</td>
      <td>{item.supervisor}</td>
      <td>{vigencia(item.inicio, item.fim)}</td>
      <td>
        <span className={`badge estado-${item.situacao.toLowerCase()}`}>{rotuloSituacaoEstagio(item.situacao)}</span>
      </td>
      <td>{item.idOrientador ? item.orientadorRotulo ?? 'Atribuído' : 'Sem orientador'}</td>
      <td className="actions">
        {actions.can('editar') && (
          <button type="button" className="ghost" onClick={() => onEditar(item)}>
            Editar
          </button>
        )}
      </td>
    </tr>
  )
}
