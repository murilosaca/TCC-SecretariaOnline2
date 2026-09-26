import { FormEvent, useEffect, useState } from 'react'
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { adminApi } from '../../api/admin'
import { ApiError } from '../../api/client'
import { useActions } from '../../hooks/useActions'
import type { AdminUsuario } from '../../models/iam'

const vazio = { nome: '', emailInstitucional: '', emailPessoal: '', grr: '' }

export function Usuarios() {
  const queryClient = useQueryClient()
  const [termo, setTermo] = useState('')
  const [debounced, setDebounced] = useState('')
  const [form, setForm] = useState(vazio)
  const [editandoId, setEditandoId] = useState<string | null>(null)
  const [erro, setErro] = useState<string | null>(null)
  const [info, setInfo] = useState<string | null>(null)
  const [confirmReset, setConfirmReset] = useState<AdminUsuario | null>(null)
  const [confirmDesativar, setConfirmDesativar] = useState<AdminUsuario | null>(null)

  useEffect(() => {
    const t = window.setTimeout(() => setDebounced(termo.trim()), 300)
    return () => window.clearTimeout(t)
  }, [termo])

  const lista = useQuery({
    queryKey: ['admin-usuarios', debounced],
    queryFn: () => adminApi.listarUsuarios(debounced || undefined),
  })
  const colecao = useActions(lista.data?._links)
  const mostrarForm = editandoId != null || colecao.can('criar')
  const forbidden = lista.isError && lista.error instanceof ApiError && lista.error.status === 403

  const salvar = useMutation({
    mutationFn: () => {
      const body = {
        nome: form.nome,
        emailInstitucional: form.emailInstitucional,
        emailPessoal: form.emailPessoal || null,
        grr: form.grr || null,
      }
      return editandoId
        ? adminApi.atualizarUsuario(editandoId, body)
        : adminApi.criarUsuario(body)
    },
    onSuccess: () => {
      setForm(vazio)
      setEditandoId(null)
      setErro(null)
      setInfo(
        editandoId
          ? 'Usuário atualizado.'
          : 'Usuário criado. O acesso inicial será enviado por e-mail (Outbox); a senha não é exibida.',
      )
      queryClient.invalidateQueries({ queryKey: ['admin-usuarios'] })
    },
    onError: (falha) => {
      setErro(falha instanceof ApiError ? falha.message : 'Falha ao salvar o usuário.')
    },
  })

  const desativar = useMutation({
    mutationFn: (id: string) => adminApi.desativarUsuario(id),
    onSuccess: () => {
      setConfirmDesativar(null)
      setInfo('Usuário desativado.')
      queryClient.invalidateQueries({ queryKey: ['admin-usuarios'] })
    },
    onError: (falha) => {
      setErro(falha instanceof ApiError ? falha.message : 'Falha ao desativar.')
    },
  })

  const resetSenha = useMutation({
    mutationFn: (usuario: AdminUsuario) => adminApi.resetSenha(usuario.id),
    onSuccess: (_data, usuario) => {
      setConfirmReset(null)
      setInfo(`Link de redefinição enviado para ${usuario.emailInstitucional}.`)
      queryClient.invalidateQueries({ queryKey: ['admin-usuarios'] })
    },
    onError: (falha) => {
      setErro(falha instanceof ApiError ? falha.message : 'Falha ao disparar o reset.')
    },
  })

  function onSubmit(event: FormEvent) {
    event.preventDefault()
    setErro(null)
    salvar.mutate()
  }

  function editar(usuario: AdminUsuario) {
    setEditandoId(usuario.id)
    setErro(null)
    setForm({
      nome: usuario.nome,
      emailInstitucional: usuario.emailInstitucional,
      emailPessoal: usuario.emailPessoal ?? '',
      grr: usuario.grr ?? '',
    })
  }

  return (
    <section className="page">
      <header className="page-head">
        <h1>Usuários</h1>
        <p>F7.1 / F7.8 — criar, editar, desativar e reset por link. Ações só via `_links`.</p>
      </header>

      {forbidden && (
        <p className="empty" role="status">
          Você não tem permissão para gerenciar usuários.
        </p>
      )}
      {(erro || (lista.isError && !forbidden)) && (
        <div className="banner danger" role="alert">
          {erro ?? 'Não foi possível carregar os usuários.'}{' '}
          {lista.isError && !forbidden && (
            <button type="button" onClick={() => lista.refetch()}>
              Tentar de novo
            </button>
          )}
        </div>
      )}
      {info && (
        <div className="banner info" role="status">
          {info}
        </div>
      )}

      {!forbidden && (
        <label>
          Buscar
          <input
            value={termo}
            onChange={(e) => setTermo(e.target.value)}
            placeholder="Nome, e-mail ou GRR"
          />
        </label>
      )}

      {mostrarForm && (
        <form className="panel" onSubmit={onSubmit}>
          <h2>{editandoId ? 'Editar usuário' : 'Novo usuário'}</h2>
          <p className="muted">
            A senha é gerada pelo sistema e nunca aparece nesta tela. Perfis/matriz FGAC ficam na fatia
            32.
          </p>
          <div className="grid">
            <label>
              Nome
              <input
                value={form.nome}
                onChange={(e) => setForm({ ...form, nome: e.target.value })}
                required
                maxLength={200}
              />
            </label>
            <label>
              E-mail institucional
              <input
                type="email"
                value={form.emailInstitucional}
                onChange={(e) => setForm({ ...form, emailInstitucional: e.target.value })}
                required
              />
            </label>
            <label>
              E-mail pessoal
              <input
                type="email"
                value={form.emailPessoal}
                onChange={(e) => setForm({ ...form, emailPessoal: e.target.value })}
              />
            </label>
            <label>
              GRR
              <input
                value={form.grr}
                onChange={(e) => setForm({ ...form, grr: e.target.value })}
                placeholder="GRR + 8 dígitos"
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
      )}

      {lista.isLoading && <p className="muted">Carregando usuários…</p>}
      {lista.data && lista.data.content.length === 0 && (
        <p className="empty">Nenhum usuário encontrado.</p>
      )}
      {lista.data && lista.data.content.length > 0 && (
        <table>
          <thead>
            <tr>
              <th>Nome</th>
              <th>E-mail</th>
              <th>GRR</th>
              <th>Situação</th>
              <th>Atualizado</th>
              <th></th>
            </tr>
          </thead>
          <tbody>
            {lista.data.content.map((usuario) => (
              <UsuarioLinha
                key={usuario.id}
                usuario={usuario}
                onEditar={editar}
                onDesativar={() => setConfirmDesativar(usuario)}
                onReset={() => setConfirmReset(usuario)}
              />
            ))}
          </tbody>
        </table>
      )}

      {confirmDesativar && (
        <div className="banner warning" role="alertdialog" aria-labelledby="desativar-titulo">
          <p id="desativar-titulo">
            Desativar <strong>{confirmDesativar.nome}</strong>? Sessões ativas serão encerradas.
          </p>
          <button
            type="button"
            className="danger"
            disabled={desativar.isPending}
            onClick={() => desativar.mutate(confirmDesativar.id)}
          >
            Confirmar desativação
          </button>
          <button type="button" className="ghost" onClick={() => setConfirmDesativar(null)}>
            Cancelar
          </button>
        </div>
      )}

      {confirmReset && (
        <div className="banner warning" role="alertdialog" aria-labelledby="reset-titulo">
          <p id="reset-titulo">
            Enviar link de redefinição para <strong>{confirmReset.nome}</strong> (
            {confirmReset.emailInstitucional})? O link e a senha não serão exibidos aqui.
          </p>
          <button
            type="button"
            disabled={resetSenha.isPending}
            onClick={() => resetSenha.mutate(confirmReset)}
          >
            Confirmar envio
          </button>
          <button type="button" className="ghost" onClick={() => setConfirmReset(null)}>
            Cancelar
          </button>
        </div>
      )}
    </section>
  )
}

function UsuarioLinha({
  usuario,
  onEditar,
  onDesativar,
  onReset,
}: {
  usuario: AdminUsuario
  onEditar: (u: AdminUsuario) => void
  onDesativar: () => void
  onReset: () => void
}) {
  const actions = useActions(usuario._links)
  return (
    <tr>
      <td>{usuario.nome}</td>
      <td>{usuario.emailInstitucional}</td>
      <td>{usuario.grr ?? '—'}</td>
      <td>{usuario.ativo ? 'Ativo' : 'Inativo'}</td>
      <td>{new Date(usuario.updatedAt).toLocaleString('pt-BR')}</td>
      <td className="actions">
        {actions.can('atualizar') && (
          <button type="button" className="ghost" onClick={() => onEditar(usuario)}>
            Editar
          </button>
        )}
        {actions.can('desativar') && (
          <button type="button" className="danger" onClick={onDesativar}>
            Desativar
          </button>
        )}
        {actions.can('reset-senha') && (
          <button type="button" className="ghost" onClick={onReset}>
            Reset senha
          </button>
        )}
      </td>
    </tr>
  )
}
