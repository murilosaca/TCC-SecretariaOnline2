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
            <h1>{item.titulo}</h1>
            <span className={`badge estado-${item.estado.toLowerCase()}`}>{rotuloEstadoTcc(item.estado)}</span>
          </header>

          {(erroAcao || erroDownload) && (
            <div className="banner danger" role="alert">
              {erroAcao instanceof ApiError ? erroAcao.message : erroDownload || 'Não foi possível concluir a ação.'}
            </div>
          )}

          <div className="tcc-resumo-grid">
            <section className="panel tcc-resumo-card" aria-labelledby="tcc-banca-titulo">
              <h2 id="tcc-banca-titulo">Banca</h2>
              {item.membros.length === 0 ? (
                <p className="empty" role="status">
                  Nenhum membro na banca.
                </p>
              ) : (
                item.membros.map((membro) => (
                  <dl key={membro.id} className="resumo tcc-banca-membro">
                    <div>
                      <dt>Nome</dt>
                      <dd>{membro.rotulo || '—'}</dd>
                    </div>
                    <div>
                      <dt>Papel</dt>
                      <dd>{rotuloPapelBanca(membro.papel)}</dd>
                    </div>
                  </dl>
                ))
              )}
            </section>

            <section className="panel tcc-resumo-card" aria-labelledby="tcc-datas-titulo">
              <h2 id="tcc-datas-titulo">Datas-chave</h2>
              <dl className="resumo">
                <div>
                  <dt>Defesa</dt>
                  <dd>{dataBr(item.dataDefesa)}</dd>
                </div>
                <div>
                  <dt>Limite de entrega</dt>
                  <dd>
                    {dataBr(item.dataEntrega)}
                    {prazo && <span className="badge prazo-proximo">Prazo próximo</span>}
                  </dd>
                </div>
              </dl>
            </section>

            <section className="panel tcc-resumo-card" aria-labelledby="tcc-versao-titulo">
              <h2 id="tcc-versao-titulo">Versão final</h2>
              {actions.can('upload-final') && (
                <form className="tcc-versao-form" onSubmit={onEnviar}>
                  <div className="arquivo-campo">
                    <span className="arquivo-campo-rotulo">PDF</span>
                    <label className="arquivo-picker">
                      <input
                        type="file"
                        className="sr-only"
                        accept="application/pdf"
                        onChange={(event) => setArquivo(event.target.files?.[0] ?? null)}
                      />
                      <span className="arquivo-picker-btn">Escolher arquivo</span>
                    </label>
                    <span className={`arquivo-nome${arquivo ? '' : ' muted'}`}>
                      {arquivo ? arquivo.name : 'Nenhum arquivo selecionado.'}
                    </span>
                  </div>
                  <button type="submit" disabled={!arquivo || pending}>
                    {enviar.isPending ? 'Enviando…' : 'Enviar versão final'}
                  </button>
                </form>
              )}
              {actions.can('download') && !actions.can('upload-final') && (
                <div className="tcc-versao-download">
                  {item.nomeArquivo && <p className="arquivo-nome">{item.nomeArquivo}</p>}
                  <button type="button" onClick={() => void baixar()} disabled={baixando}>
                    {baixando ? 'Baixando…' : 'Baixar TCC'}
                  </button>
                </div>
              )}
              {!actions.can('upload-final') && !actions.can('download') && (
                <p className="empty" role="status">
                  Nenhuma versão final disponível.
                </p>
              )}
            </section>
          </div>

          {actions.can('avaliar') && (
            <form
              className="panel tcc-avaliacao-form"
              onSubmit={(event) => {
                event.preventDefault()
              }}
            >
              <h2>Avaliação</h2>
              <label className="tcc-nota-campo">
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

          <section className="tcc-avaliacoes" aria-labelledby="tcc-avaliacoes-titulo">
            <h2 id="tcc-avaliacoes-titulo">Avaliações</h2>
            {item.avaliacoes.length === 0 ? (
              <p className="empty" role="status">
                Nenhuma avaliação registrada.
              </p>
            ) : (
              <div className="tcc-avaliacoes-lista">
                {item.avaliacoes.map((avaliacao) => (
                  <article
                    key={avaliacao.id}
                    className={`panel tcc-avaliacao-card resultado-${avaliacao.resultado.toLowerCase()}`}
                  >
                    <header className="tcc-avaliacao-card-head">
                      <h3>{rotuloResultadoTcc(avaliacao.resultado)}</h3>
                      <p className="tcc-avaliacao-nota" aria-label={`Nota ${avaliacao.nota}`}>
                        {avaliacao.nota}
                      </p>
                    </header>
                    <dl className="resumo">
                      <div>
                        <dt>Papel</dt>
                        <dd>{rotuloPapelBanca(avaliacao.papel)}</dd>
                      </div>
                      {avaliacao.autorRotulo && (
                        <div>
                          <dt>Avaliador</dt>
                          <dd>{avaliacao.autorRotulo}</dd>
                        </div>
                      )}
                    </dl>
                    <p className="tcc-avaliacao-parecer">{avaliacao.parecer}</p>
                  </article>
                ))}
              </div>
            )}
          </section>
        </>
      )}
    </section>
  )
}
