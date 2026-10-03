import { useMemo, useState } from 'react'
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { autoridadesApi } from '../../api/perfis'
import { ApiError } from '../../api/client'

export function Autoridades() {
  const client = useQueryClient()
  const lista = useQuery({ queryKey: ['admin-autoridades'], queryFn: () => autoridadesApi.listar() })
  const [matriz, setMatriz] = useState<Record<string, string[]> | null>(null)
  const [descricao, setDescricao] = useState<Record<string, string>>({})
  const grade = matriz ?? lista.data?.matriz ?? {}

  const salvar = useMutation({
    mutationFn: () =>
      autoridadesApi.salvarMatriz(
        (lista.data?.perfis ?? []).map((perfil) => ({
          id: perfil.id,
          authorities: grade[perfil.id] ?? [],
        })),
      ),
    onSuccess: (resposta) => {
      setMatriz(resposta.matriz)
      client.setQueryData(['admin-autoridades'], resposta)
    },
  })

  const editar = useMutation({
    mutationFn: (nome: string) => autoridadesApi.descrever(nome, descricao[nome] ?? ''),
    onSuccess: () => client.invalidateQueries({ queryKey: ['admin-autoridades'] }),
  })

  const colunas = lista.data?.perfis ?? []
  const linhas = useMemo(() => lista.data?.authorities ?? [], [lista.data])

  function alternar(perfilId: string, authority: string) {
    const atual = new Set(grade[perfilId] ?? [])
    if (atual.has(authority)) atual.delete(authority)
    else atual.add(authority)
    setMatriz({ ...grade, [perfilId]: [...atual] })
  }

  return (
    <section className="page" aria-labelledby="autoridades-titulo">
      <header className="page-head">
        <div>
          <h1 id="autoridades-titulo">Autoridades</h1>
          <p className="muted">O nome de authority de sistema não é editável. Só a descrição.</p>
        </div>
        <button type="button" onClick={() => salvar.mutate()} disabled={salvar.isPending || !lista.data}>
          Salvar matriz
        </button>
      </header>
      {salvar.isError && (
        <p className="banner danger" role="alert">
          {salvar.error instanceof ApiError ? salvar.error.message : 'Falha ao gravar a matriz.'}
        </p>
      )}
      {lista.isLoading && <p className="muted">Carregando authorities…</p>}
      {lista.isError && <p className="empty">Não foi possível carregar a matriz.</p>}
      {lista.data && (
        <table>
          <thead>
            <tr>
              <th scope="col">Nome</th>
              <th scope="col">Módulo</th>
              <th scope="col">Descrição</th>
              {colunas.map((perfil) => (
                <th scope="col" key={perfil.id}>
                  {perfil.nome}
                </th>
              ))}
            </tr>
          </thead>
          <tbody>
            {linhas.map((authority) => (
              <tr key={authority.nome}>
                <td>
                  <input value={authority.nome} readOnly={authority.readOnlyName} aria-label={`Nome ${authority.nome}`} />
                </td>
                <td>{authority.modulo}</td>
                <td>
                  <input
                    aria-label={`Descrição ${authority.nome}`}
                    value={descricao[authority.nome] ?? authority.descricao}
                    onChange={(event) => setDescricao({ ...descricao, [authority.nome]: event.target.value })}
                    onBlur={() => {
                      if ((descricao[authority.nome] ?? authority.descricao) !== authority.descricao) {
                        editar.mutate(authority.nome)
                      }
                    }}
                  />
                </td>
                {colunas.map((perfil) => (
                  <td key={perfil.id}>
                    <input
                      type="checkbox"
                      aria-label={`${perfil.nome} ${authority.nome}`}
                      checked={(grade[perfil.id] ?? []).includes(authority.nome)}
                      onChange={() => alternar(perfil.id, authority.nome)}
                    />
                  </td>
                ))}
              </tr>
            ))}
          </tbody>
        </table>
      )}
    </section>
  )
}
