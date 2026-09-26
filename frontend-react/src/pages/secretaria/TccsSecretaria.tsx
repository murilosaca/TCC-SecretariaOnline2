import { FormEvent, useMemo, useState } from 'react'
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { academicoApi } from '../../api/academico'
import { ApiError } from '../../api/client'
import { tccsApi } from '../../api/tccs'
import { UsuarioPicker } from '../../components/UsuarioPicker'
import { useActions } from '../../hooks/useActions'
import type { Tcc } from '../../models/tcc'

const vazio = {
  alunoId: '',
  alunoRotulo: '',
  titulo: '',
  dataDefesa: '',
  dataEntrega: '',
  orientadorId: null as string | null,
  orientadorRotulo: '',
  bancaIds: [] as string[],
  bancaRotulos: {} as Record<string, string>,
}

export function TccsSecretaria() {
  const queryClient = useQueryClient()
  const [form, setForm] = useState(vazio)
  const [editandoId, setEditandoId] = useState<string | null>(null)
  const [termo, setTermo] = useState('')
  const [erro, setErro] = useState<string | null>(null)

  const lista = useQuery({
    queryKey: ['tccs', 'secretaria'],
    queryFn: () => tccsApi.listarDoEscopo(),
  })
  const colecao = useActions(lista.data?._links)
  const mostrarForm = editandoId != null || colecao.can('novo')
  const forbidden = lista.isError && lista.error instanceof ApiError && lista.error.status === 403

  const alunos = useQuery({
    queryKey: ['alunos', 'tcc-secretaria', termo],
    queryFn: () => academicoApi.listarAlunos(undefined, termo || undefined, 0, 100),
    enabled: mostrarForm && editandoId == null,
  })

  const opcoes = useMemo(() => {
    const conteudo = alunos.data?.content ?? []
    if (form.alunoId && !conteudo.some((item) => item.id === form.alunoId)) {
      return [{ id: form.alunoId, grr: '', nome: form.alunoRotulo || 'Aluno do TCC' }, ...conteudo]
    }
    return conteudo
  }, [alunos.data, form.alunoId, form.alunoRotulo])

  const salvar = useMutation({
    mutationFn: () => {
      if (!form.orientadorId) {
        throw new ApiError(422, 'Selecione o orientador.')
      }
      const membros = [
        { idUsuario: form.orientadorId, papel: 'ORIENTADOR' },
        ...form.bancaIds
          .filter((id) => id !== form.orientadorId)
          .map((id) => ({ idUsuario: id, papel: 'BANCA' })),
      ]
      const body = {
        alunoId: form.alunoId,
        titulo: form.titulo,
        dataDefesa: form.dataDefesa,
        dataEntrega: form.dataEntrega,
        membros,
      }
      return editandoId ? tccsApi.atualizar(editandoId, body) : tccsApi.criar(body)
    },
    onSuccess: () => {
      setForm(vazio)
      setEditandoId(null)
      setTermo('')
      setErro(null)
      queryClient.invalidateQueries({ queryKey: ['tccs', 'secretaria'] })
    },
    onError: (falha) => {
      setErro(falha instanceof ApiError ? falha.message : 'Falha ao salvar o TCC.')
    },
  })

  function onSubmit(event: FormEvent) {
    event.preventDefault()
    setErro(null)
    salvar.mutate()
  }

  function editar(item: Tcc) {
    const orientador = item.membros.find((m) => m.papel === 'ORIENTADOR')
    const banca = item.membros.filter((m) => m.papel !== 'ORIENTADOR')
    setEditandoId(item.id)
    setErro(null)
    setForm({
      alunoId: item.idAluno,
      alunoRotulo: item.alunoNome ?? 'Aluno do TCC',
      titulo: item.titulo,
      dataDefesa: item.dataDefesa.slice(0, 10),
      dataEntrega: item.dataEntrega.slice(0, 10),
      orientadorId: orientador?.idUsuario ?? null,
      orientadorRotulo: orientador?.rotulo ?? item.orientadorRotulo ?? '',
      bancaIds: banca.map((m) => m.idUsuario),
      bancaRotulos: Object.fromEntries(banca.map((m) => [m.idUsuario, m.rotulo ?? m.idUsuario])),
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
        <h1>Cadastro de TCCs</h1>
        <p className="muted">
          TCCs dos cursos vinculados à secretaria. A banca é montada pelo picker de usuários; o aluno
          acompanha e envia a versão final nos fluxos já existentes.
        </p>
      </header>

      {forbidden && (
        <p className="empty" role="status">
          Você não tem permissão para registrar TCCs.
        </p>
      )}
      {(erro || (lista.isError && !forbidden)) && (
        <div className="banner danger" role="alert">
          {erro ?? 'Não foi possível carregar os TCCs.'}{' '}
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
          <h2>{editandoId ? 'Editar TCC' : 'Novo TCC'}</h2>
          <p className="muted">O aluno sai da lista do curso. Orientador e banca vêm do picker.</p>
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
              Título
              <input
                value={form.titulo}
                onChange={(event) => setForm({ ...form, titulo: event.target.value })}
                required
                minLength={3}
                maxLength={200}
              />
            </label>
            <label>
              Limite de entrega
              <input
                type="date"
                value={form.dataEntrega}
                onChange={(event) => setForm({ ...form, dataEntrega: event.target.value })}
                required
              />
            </label>
            <label>
              Data da defesa
              <input
                type="date"
                value={form.dataDefesa}
                onChange={(event) => setForm({ ...form, dataDefesa: event.target.value })}
                required
              />
            </label>
          </div>
          <UsuarioPicker
            label="Orientador"
            value={form.orientadorId}
            selectedLabel={form.orientadorRotulo}
            required
            onChange={(id, opcao) =>
              setForm({
                ...form,
                orientadorId: id,
                orientadorRotulo: opcao ? `${opcao.nome} · ${opcao.emailInstitucional}` : '',
                bancaIds: form.bancaIds.filter((item) => item !== id),
              })
            }
          />
          <UsuarioPicker
            multiple
            label="Demais membros da banca"
            values={form.bancaIds}
            selectedLabels={form.bancaRotulos}
            onChange={(ids, labels) =>
              setForm((atual) => ({
                ...atual,
                bancaIds: ids.filter((id) => id !== atual.orientadorId),
                bancaRotulos: labels,
              }))
            }
          />
          {alunos.isLoading && editandoId == null && (
            <p className="muted" aria-busy="true">
              Carregando alunos…
            </p>
          )}
          {editandoId == null && alunos.data && alunos.data.page.totalElements > alunos.data.content.length && (
            <p className="muted">Há mais alunos neste escopo. Refine a busca por nome, GRR ou e-mail.</p>
          )}
          <button
            type="submit"
            disabled={
              salvar.isPending ||
              alunosIndisponiveis ||
              (editandoId == null && !form.alunoId) ||
              !form.orientadorId
            }
          >
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
          Carregando TCCs…
        </p>
      )}
      {lista.data && lista.data.content.length === 0 && (
        <p className="empty" role="status">
          Nenhum TCC registrado nos cursos da secretaria.
        </p>
      )}
      {lista.data && lista.data.content.length > 0 && (
        <table>
          <thead>
            <tr>
              <th scope="col">Aluno</th>
              <th scope="col">Título</th>
              <th scope="col">Orientador</th>
              <th scope="col">Entrega</th>
              <th scope="col">Defesa</th>
              <th scope="col">Situação</th>
              <th scope="col">Estado</th>
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

function Linha({ item, onEditar }: { item: Tcc; onEditar: (item: Tcc) => void }) {
  const actions = useActions(item._links)
  return (
    <tr>
      <td>{item.alunoNome ?? '—'}</td>
      <td>{item.titulo}</td>
      <td>{item.orientadorRotulo ?? '—'}</td>
      <td>{item.dataEntrega.slice(0, 10)}</td>
      <td>{item.dataDefesa.slice(0, 10)}</td>
      <td>
        <span className={`badge estado-${item.situacao.toLowerCase()}`}>{item.situacao}</span>
      </td>
      <td>{item.estado}</td>
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
