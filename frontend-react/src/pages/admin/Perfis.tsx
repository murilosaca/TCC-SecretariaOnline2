import { FormEvent, useState } from 'react'
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { ApiError } from '../../api/client'
import { perfisApi, type PerfilAcesso } from '../../api/perfis'
import { useActions } from '../../hooks/useActions'

export function Perfis() {
  const [q, setQ] = useState('')
  const [filtro, setFiltro] = useState('')
  const [nome, setNome] = useState('')
  const [descricao, setDescricao] = useState('')
  const client = useQueryClient()
  const lista = useQuery({
    queryKey: ['admin-perfis', filtro],
    queryFn: () => perfisApi.listar(filtro || undefined),
  })
  const criar = useMutation({
    mutationFn: () => perfisApi.criar({ nome, descricao, authorities: [] }),
    onSuccess: () => {
      setNome('')
      setDescricao('')
      client.invalidateQueries({ queryKey: ['admin-perfis'] })
    },
  })
  const excluir = useMutation({
    mutationFn: (path: string) => perfisApi.excluir(path),
    onSuccess: () => client.invalidateQueries({ queryKey: ['admin-perfis'] }),
  })
  const colecao = useActions(lista.data?._links)

  function buscar(event: FormEvent) {
    event.preventDefault()
    setFiltro(q.trim())
  }

  return (
    <section className="page" aria-labelledby="perfis-titulo">
      <header className="page-head">
        <div>
          <h1 id="perfis-titulo">Perfis</h1>
          <p className="muted">Perfis de sistema não trazem Excluir.</p>
        </div>
      </header>
      <form className="filters" onSubmit={buscar}>
        <label htmlFor="perfil-busca">Busca</label>
        <input id="perfil-busca" value={q} onChange={(event) => setQ(event.target.value)} />
        <button type="submit">Filtrar</button>
      </form>
      {colecao.can('criar') && (
        <form
          className="filters"
          onSubmit={(event) => {
            event.preventDefault()
            criar.mutate()
          }}
        >
          <label htmlFor="perfil-nome">Novo perfil</label>
          <input id="perfil-nome" value={nome} onChange={(event) => setNome(event.target.value)} />
          <label htmlFor="perfil-desc">Descrição</label>
          <input id="perfil-desc" value={descricao} onChange={(event) => setDescricao(event.target.value)} />
          <button type="submit">Novo</button>
        </form>
      )}
      {criar.isError && (
        <p className="banner danger" role="alert">
          {criar.error instanceof ApiError ? criar.error.message : 'Falha ao criar o perfil.'}
        </p>
      )}
      {excluir.isError && (
        <p className="banner danger" role="alert">
          {excluir.error instanceof ApiError ? excluir.error.message : 'Falha ao excluir o perfil.'}
        </p>
      )}
      {lista.isLoading && <p className="muted">Carregando perfis…</p>}
      {lista.isError && <p className="empty">Não foi possível carregar os perfis.</p>}
      {lista.data && lista.data.content.length === 0 && <p className="empty">Nenhum perfil encontrado.</p>}
      <table>
        <thead>
          <tr>
            <th scope="col">Nome</th>
            <th scope="col">Descrição</th>
            <th scope="col">Authorities</th>
            <th scope="col">Usuários</th>
            <th scope="col">Tipo</th>
            <th scope="col">Ações</th>
          </tr>
        </thead>
        <tbody>
          {(lista.data?.content ?? []).map((perfil) => (
            <Linha key={perfil.id} perfil={perfil} onExcluir={(path) => excluir.mutate(path)} />
          ))}
        </tbody>
      </table>
    </section>
  )
}

function Linha({ perfil, onExcluir }: { perfil: PerfilAcesso; onExcluir: (path: string) => void }) {
  const actions = useActions(perfil._links)
  return (
    <tr>
      <td>{perfil.nome}</td>
      <td>{perfil.descricao ?? '—'}</td>
      <td>{perfil.quantidadeAuthorities}</td>
      <td>{perfil.quantidadeUsuarios}</td>
      <td>
        <span className="badge">{perfil.tipo === 'SYSTEM' ? 'Sistema' : 'Customizado'}</span>
      </td>
      <td>
        {actions.can('excluir') && (
          <button type="button" className="danger" onClick={() => onExcluir(actions.href('excluir')!)}>
            Excluir
          </button>
        )}
      </td>
    </tr>
  )
}
