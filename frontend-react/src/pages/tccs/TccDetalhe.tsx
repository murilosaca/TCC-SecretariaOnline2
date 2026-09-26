import { FormEvent, useState } from 'react'
import { Link, useParams } from 'react-router-dom'
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { ApiError } from '../../api/client'
import { tccsApi } from '../../api/tccs'
import { useAuth } from '../../auth/AuthContext'
import { useActions } from '../../hooks/useActions'
import { dataBr, entregaProxima, rotuloEstadoTcc, rotuloPapelBanca, rotuloResultadoTcc } from '../../lib/tcc'

const PARECER_MIN = 20

export function TccDetalhe() {
  const { id = '' } = useParams()
  const { links } = useAuth()
  const menu = useActions(links)
  const queryClient = useQueryClient()
  const [arquivo, setArquivo] = useState<File | null>(null)
  const [nota, setNota] = useState('8.5')
  const [parecer, setParecer] = useState('')
  const [baixando, setBaixando] = useState(false)
  const [erroDownload, setErroDownload] = useState('')

  const detalhe = useQuery({
    queryKey: ['tcc', id],
    queryFn: () => tccsApi.obter(id),
    enabled: Boolean(id),
  })

  const enviar = useMutation({
    mutationFn: (file: File) => tccsApi.enviarVersaoFinal(id, file),
    onSuccess: (atual) => {
      queryClient.setQueryData(['tcc', id], atual)
      setArquivo(null)
      void queryClient.invalidateQueries({ queryKey: ['tccs'] })
    },
  })

  const avaliar = useMutation({
    mutationFn: (acao: 'APROVAR' | 'INDEFERIR' | 'SOLICITAR_CORRECOES') =>
      tccsApi.avaliar(id, acao, Number(nota), parecer),
    onSuccess: (atual) => {
      queryClient.setQueryData(['tcc', id], atual)
      setParecer('')
      void queryClient.invalidateQueries({ queryKey: ['tccs'] })
    },
  })

  const item = detalhe.data
  const actions = useActions(item?._links)
  const voltar = menu.can('tccs-revisao') && !menu.can('tccs')
    ? { href: '/tccs?to=me', label: '← Fila de revisão' }
    : { href: '/tccs', label: '← TCCs' }
  const erroAcao = enviar.error ?? avaliar.error
  const pending = enviar.isPending || avaliar.isPending
  const prazo = item ? entregaProxima(item.dataEntrega) : false

  function onEnviar(event: FormEvent) {
    event.preventDefault()
    if (arquivo) {
      enviar.mutate(arquivo)
    }
  }

  function onAvaliar(acao: 'APROVAR' | 'INDEFERIR' | 'SOLICITAR_CORRECOES') {
    if (acao !== 'APROVAR' && parecer.trim().length < PARECER_MIN) {
      return
    }
    avaliar.mutate(acao)
  }

  async function baixar() {
    const href = actions.href('download')
    if (!href || !item) {
      return
    }
    setBaixando(true)
    setErroDownload('')
    try {
      await tccsApi.baixar(href, item.nomeArquivo || `tcc-${item.id}.pdf`)
    } catch {
      setErroDownload('Não foi possível baixar o PDF.')
    } finally {
      setBaixando(false)
    }
  }

  return (
    <section className="page detalhe">
      <p>
        <Link to={voltar.href} className="ghost-link">
          {voltar.label}
        </Link>
      </p>

      {detalhe.isLoading && (
        <p className="muted" aria-busy="true">
          Carregando TCC…
        </p>
      )}
      {detalhe.isError && (
        <div className="banner danger" role="alert">
          TCC não encontrado ou indisponível.{' '}
          <button type="button" onClick={() => detalhe.refetch()}>
            Tentar de novo
          </button>
        </div>
      )}

      {item && (
        <>
          <header className="page-head">
            <div>
              <h1>{item.titulo}</h1>
              <p className="muted">{item.alunoNome || item.orientadorRotulo || ''}</p>
            </div>
            <span className={`badge estado-${item.estado.toLowerCase()}`}>{rotuloEstadoTcc(item.estado)}</span>
          </header>

          {(erroAcao || erroDownload) && (
            <div className="banner danger" role="alert">
              {erroAcao instanceof ApiError ? erroAcao.message : erroDownload || 'Não foi possível concluir a ação.'}
            </div>
          )}

          <section>
            <h2>Banca</h2>
            <table>
              <thead>
                <tr>
                  <th scope="col">Nome</th>
                  <th scope="col">Papel</th>
                </tr>
              </thead>
              <tbody>
                {item.membros.map((membro) => (
                  <tr key={membro.id}>
                    <td>{membro.rotulo || '—'}</td>
                    <td>{rotuloPapelBanca(membro.papel)}</td>
                  </tr>
                ))}
              </tbody>
            </table>
          </section>

          <section>
            <h2>Datas-chave</h2>
            <p>
              Defesa: <strong>{dataBr(item.dataDefesa)}</strong>
            </p>
            <p>
              Limite de entrega: <strong>{dataBr(item.dataEntrega)}</strong>
              {prazo && <span className="badge prazo-proximo">Prazo próximo</span>}
            </p>
          </section>

          {actions.can('download') && (
            <p>
              <button type="button" className="ghost" onClick={() => void baixar()} disabled={baixando}>
                {baixando ? 'Baixando…' : 'Baixar TCC'}
              </button>
            </p>
          )}

          {actions.can('upload-final') && (
            <form onSubmit={onEnviar}>
              <h2>Versão final</h2>
              <label>
                PDF
                <input
                  type="file"
                  accept="application/pdf"
                  onChange={(event) => setArquivo(event.target.files?.[0] ?? null)}
                />
              </label>
              <button type="submit" disabled={!arquivo || pending}>
                {enviar.isPending ? 'Enviando…' : 'Enviar versão final'}
              </button>
            </form>
          )}

          {actions.can('avaliar') && (
            <form
              onSubmit={(event) => {
                event.preventDefault()
              }}
            >
              <h2>Avaliação</h2>
              <label>
                Nota
                <input
                  type="number"
                  min="0"
                  max="10"
                  step="0.1"
                  value={nota}
                  onChange={(event) => setNota(event.target.value)}
                  aria-label="Nota"
                  required
                />
              </label>
              <label>
                Parecer
                <textarea value={parecer} onChange={(event) => setParecer(event.target.value)} rows={4} required />
              </label>
              <div className="action-bar">
                <button type="button" onClick={() => onAvaliar('APROVAR')} disabled={pending || parecer.trim().length === 0}>
                  Aprovar
                </button>
                <button
                  type="button"
                  onClick={() => onAvaliar('SOLICITAR_CORRECOES')}
                  disabled={pending || parecer.trim().length < PARECER_MIN}
                >
                  Solicitar correções
                </button>
                <button
                  type="button"
                  onClick={() => onAvaliar('INDEFERIR')}
                  disabled={pending || parecer.trim().length < PARECER_MIN}
                >
                  Indeferir
                </button>
              </div>
            </form>
          )}

          <section>
            <h2>Avaliações</h2>
            {item.avaliacoes.length === 0 && <p className="empty">Nenhuma avaliação registrada.</p>}
            {item.avaliacoes.length > 0 && (
              <ol className="timeline">
                {item.avaliacoes.map((avaliacao) => (
                  <li key={avaliacao.id}>
                    <strong>{rotuloResultadoTcc(avaliacao.resultado)}</strong>
                    {' · '}
                    {rotuloPapelBanca(avaliacao.papel)}
                    {avaliacao.autorRotulo ? ` · ${avaliacao.autorRotulo}` : ''}
                    {' · nota '}
                    {avaliacao.nota}
                    <p>{avaliacao.parecer}</p>
                  </li>
                ))}
              </ol>
            )}
          </section>
        </>
      )}
    </section>
  )
}
