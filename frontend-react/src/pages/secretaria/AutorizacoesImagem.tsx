import { useState } from 'react'
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { ApiError } from '../../api/client'
import { solicitacoesApi } from '../../api/solicitacoes'
import { useActions } from '../../hooks/useActions'
import type { Solicitacao } from '../../models/solicitacao'

export function AutorizacoesImagem() {
  const queryClient = useQueryClient()
  const [selecionados, setSelecionados] = useState<string[]>([])
  const [justificativa, setJustificativa] = useState('')
  const [rejeitando, setRejeitando] = useState(false)
  const [falha, setFalha] = useState<string[] | null>(null)

  const lista = useQuery({
    queryKey: ['solicitacoes', 'autorizacoes-imagem'],
    queryFn: () => solicitacoesApi.listarAutorizacoesImagem(),
  })
  const colecao = useActions(lista.data?._links)
  const forbidden = lista.isError && lista.error instanceof ApiError && lista.error.status === 403

  const lote = useMutation({
    mutationFn: (decisao: 'DEFERIDA' | 'INDEFERIDA') =>
      solicitacoesApi.deliberarImagemLote(selecionados, decisao, decisao === 'INDEFERIDA' ? justificativa : undefined),
    onSuccess: async () => {
      setFalha(null)
      setSelecionados([])
      setJustificativa('')
      setRejeitando(false)
      await queryClient.invalidateQueries({ queryKey: ['solicitacoes', 'autorizacoes-imagem'] })
    },
    onError: async (erro) => {
      setFalha(erro instanceof ApiError ? erro.failedIds ?? [] : [])
      await queryClient.invalidateQueries({ queryKey: ['solicitacoes', 'autorizacoes-imagem'] })
    },
  })

  function alternar(id: string, marcado: boolean) {
    setSelecionados((atual) => (marcado ? [...atual, id] : atual.filter((item) => item !== id)))
  }

  return (
    <section className="page" aria-labelledby="imagem-titulo">
      <header className="page-head">
        <div>
          <h1 id="imagem-titulo">Autorizações de imagem</h1>
          <p className="muted">Revisão em lote das solicitações AUTORIZACAO_IMAGEM.</p>
        </div>
        {colecao.can('bulkDeliberate') && (
          <div className="row-actions">
            <button type="button" disabled={selecionados.length === 0 || lote.isPending} onClick={() => lote.mutate('DEFERIDA')}>
              Aprovar
            </button>
            <button type="button" disabled={selecionados.length === 0 || lote.isPending} onClick={() => setRejeitando(true)}>
              Rejeitar
            </button>
          </div>
        )}
      </header>

      {forbidden && <p className="empty">Autorizações de imagem indisponíveis para esta sessão.</p>}
      {falha && (
        <div className="banner danger" role="alert">
          O lote não foi gravado. Itens fora de ABERTA: {falha.length === 0 ? 'consulte o detalhe.' : falha.join(', ')}
        </div>
      )}
      {rejeitando && (
        <form
          className="panel"
          onSubmit={(event) => {
            event.preventDefault()
            lote.mutate('INDEFERIDA')
          }}
        >
          <label htmlFor="justificativa-imagem">Justificativa</label>
          <textarea
            id="justificativa-imagem"
            value={justificativa}
            onChange={(event) => setJustificativa(event.target.value)}
            required
          />
          <button type="submit" disabled={lote.isPending}>
            Confirmar rejeição
          </button>
        </form>
      )}
      {lista.isLoading && <p className="muted">Carregando autorizações…</p>}
      {lista.data && lista.data.content.length === 0 && <p className="empty">Nenhuma autorização de imagem.</p>}
      {lista.data && lista.data.content.length > 0 && (
        <table className="dense">
          <thead>
            <tr>
              <th>Foto</th>
              <th>Nome</th>
              <th>GRR</th>
              <th>Curso</th>
              <th>Data</th>
              <th>Status</th>
            </tr>
          </thead>
          <tbody>
            {lista.data.content.map((item) => (
              <Linha key={item.id} item={item} marcado={selecionados.includes(item.id)} onToggle={alternar} />
            ))}
          </tbody>
        </table>
      )}
    </section>
  )
}

function Linha({
  item,
  marcado,
  onToggle,
}: {
  item: Solicitacao
  marcado: boolean
  onToggle: (id: string, marcado: boolean) => void
}) {
  const acoes = useActions(item._links)
  const nome = item.solicitanteNome ?? 'Aluno'
  return (
    <tr>
      <td>
        {item.thumbnailUrl ? (
          <img className="thumb-48" src={item.thumbnailUrl} alt={nome} />
        ) : (
          <span className="thumb-48 placeholder" role="img" aria-label={nome} />
        )}
      </td>
      <td>
        {acoes.can('bulk_deliberate') && (
          <input
            type="checkbox"
            aria-label={`Selecionar ${nome}`}
            checked={marcado}
            onChange={(event) => onToggle(item.id, event.target.checked)}
          />
        )}{' '}
        {nome}
      </td>
      <td>{item.solicitanteGrr ?? '—'}</td>
      <td>{item.cursoSigla ?? item.cursoNome ?? '—'}</td>
      <td>{item.createdAt?.slice(0, 10)}</td>
      <td>{item.estado}</td>
    </tr>
  )
}
