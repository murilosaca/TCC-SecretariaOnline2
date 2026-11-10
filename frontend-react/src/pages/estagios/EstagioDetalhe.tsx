import { useRef, useState } from 'react'
import { Link, useLocation, useParams } from 'react-router-dom'
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { ApiError } from '../../api/client'
import { estagiosApi } from '../../api/estagios'
import { useAuth } from '../../auth/AuthContext'
import { useActions } from '../../hooks/useActions'
import {
  dataBr,
  rotuloAcaoParecer,
  rotuloEstadoDocumento,
  rotuloSituacaoEstagio,
  rotuloTipoDocumento,
  vigencia,
} from '../../lib/estagio'
import type { DocumentoEstagio } from '../../models/estagio'

const PARECER_REPROVAR_MIN = 20

type VoltarOrigem = { href: string; label: string }

function voltarDaOrigem(state: unknown): VoltarOrigem | null {
  const from = (state as { from?: Partial<VoltarOrigem> } | null)?.from
  if (!from || typeof from.href !== 'string' || typeof from.label !== 'string') {
    return null
  }
  if (!from.href.startsWith('/') || from.href.startsWith('//')) {
    return null
  }
  return { href: from.href, label: from.label }
}

export function EstagioDetalhe() {
  const { id = '' } = useParams()
  const location = useLocation()
  const { links } = useAuth()
  const menu = useActions(links)
  const queryClient = useQueryClient()
  const [aviso, setAviso] = useState<string | null>(null)

  const detalhe = useQuery({
    queryKey: ['estagio', id],
    queryFn: () => estagiosApi.obter(id),
    enabled: Boolean(id),
  })

  const enviar = useMutation({
    mutationFn: ({ tipo, arquivo }: { tipo: string; arquivo: File }) =>
      estagiosApi.enviarDocumento(id, tipo, arquivo),
    onSuccess: (atual) => {
      setAviso(null)
      queryClient.setQueryData(['estagio', id], atual)
      void queryClient.invalidateQueries({ queryKey: ['estagios'] })
    },
    onError: (erro) => {
      setAviso(erro instanceof ApiError ? erro.message : 'Não foi possível enviar o documento.')
    },
  })

  const parecer = useMutation({
    mutationFn: ({
      documentoId,
      acao,
      texto,
    }: {
      documentoId: string
      acao: 'APROVAR' | 'REPROVAR'
      texto: string
    }) => estagiosApi.parecer(id, documentoId, acao, texto),
    onSuccess: (atual) => {
      setAviso(null)
      queryClient.setQueryData(['estagio', id], atual)
      void queryClient.invalidateQueries({ queryKey: ['estagios'] })
    },
    onError: (erro) => {
      setAviso(erro instanceof ApiError ? erro.message : 'Não foi possível registrar o parecer.')
    },
  })

  const encerrar = useMutation({
    mutationFn: () => estagiosApi.encerrar(id),
    onSuccess: (atual) => {
      setAviso(null)
      queryClient.setQueryData(['estagio', id], atual)
      void queryClient.invalidateQueries({ queryKey: ['estagios'] })
    },
    onError: (erro) => {
      setAviso(erro instanceof ApiError ? erro.message : 'Não foi possível arquivar o estágio.')
    },
  })

  const item = detalhe.data
  const actions = useActions(item?._links)
  const voltar =
    voltarDaOrigem(location.state) ??
    (menu.can('estagios-revisao') && !menu.can('estagios')
      ? { href: '/estagios?to=me', label: '← Revisão de estágios' }
      : { href: '/estagios', label: '← Estágios' })
  const pending = enviar.isPending || parecer.isPending || encerrar.isPending

  return (
    <section className="page detalhe">
      <p>
        <Link to={voltar.href} className="ghost-link">
          {voltar.label}
        </Link>
      </p>

      {detalhe.isLoading && (
        <p className="muted" aria-busy="true">
          Carregando estágio…
        </p>
      )}
      {detalhe.isError && (
        <div className="banner danger" role="alert">
          Estágio não encontrado ou indisponível.{' '}
          <button type="button" onClick={() => detalhe.refetch()}>
            Tentar de novo
          </button>
        </div>
      )}

      {aviso && (
        <div className="banner danger" role="alert">
          {aviso}
        </div>
      )}

      {item && (
        <>
          <header className="page-head">
            <h1>{item.empresa}</h1>
            <span className={`badge estado-${item.situacao.toLowerCase()}`}>
              {rotuloSituacaoEstagio(item.situacao)}
            </span>
          </header>

          {actions.can('arquivar') && (
            <div className="action-bar">
              <button type="button" onClick={() => encerrar.mutate()} disabled={pending}>
                {encerrar.isPending ? 'Arquivando…' : 'Arquivar estágio'}
              </button>
            </div>
          )}

          <article className="panel estagio-detalhes">
            <h2>Detalhes</h2>
            <dl className="resumo resumo-horizontal">
              <div>
                <dt>Empresa</dt>
                <dd>{item.empresa}</dd>
              </div>
              <div>
                <dt>Supervisor</dt>
                <dd>{item.supervisor}</dd>
              </div>
              <div>
                <dt>Vigência</dt>
                <dd>{vigencia(item.inicio, item.fim)}</dd>
              </div>
              <div>
                <dt>Orientador</dt>
                <dd>{item.orientadorRotulo || '—'}</dd>
              </div>
              <div>
                <dt>Situação</dt>
                <dd>{rotuloSituacaoEstagio(item.situacao)}</dd>
              </div>
            </dl>
          </article>

          {item.documentos.length === 0 ? (
            <p className="empty" role="status">
              Nenhum documento exigido neste estágio.
            </p>
          ) : (
            <div className="estagio-docs-grid">
              {item.documentos.map((documento) => (
                <DocumentoCard
                  key={documento.id}
                  documento={documento}
                  pending={pending}
                  onEnviar={(arquivo) => enviar.mutate({ tipo: documento.tipo, arquivo })}
                  onParecer={(acao, texto) =>
                    parecer.mutate({ documentoId: documento.id, acao, texto })
                  }
                  onErro={(mensagem) => setAviso(mensagem)}
                />
              ))}
            </div>
          )}

          <section className="estagio-secao estagio-secao-pareceres" aria-labelledby="pareceres-titulo">
            <h2 id="pareceres-titulo">Pareceres</h2>
            <div className="estagio-tabela-wrap estagio-tabela-wrap-scroll">
              <table className="estagio-tabela estagio-tabela-pareceres">
                <thead>
                  <tr>
                    <th scope="col">Documento</th>
                    <th scope="col">Decisão</th>
                    <th scope="col">Autor</th>
                    <th scope="col">Data</th>
                    <th scope="col">Parecer</th>
                  </tr>
                </thead>
                <tbody>
                  {item.pareceres.length === 0 ? (
                    <tr>
                      <td colSpan={5} className="tabela-vazio">
                        Nenhum parecer registrado ainda.
                      </td>
                    </tr>
                  ) : (
                    item.pareceres.map((registro) => (
                      <tr key={registro.id}>
                        <td>{rotuloTipoDocumento(registro.tipoDocumento)}</td>
                        <td>
                          <span className={`badge estado-${registro.acao.toLowerCase()}`}>
                            {rotuloAcaoParecer(registro.acao)}
                          </span>
                        </td>
                        <td>{registro.autorRotulo || 'Orientador'}</td>
                        <td>{dataBr(registro.createdAt)}</td>
                        <td>{registro.texto}</td>
                      </tr>
                    ))
                  )}
                </tbody>
              </table>
            </div>
          </section>
        </>
      )}
    </section>
  )
}

function descricaoAcaoDocumento(podeEnviar: boolean, podeVisualizar: boolean, estado: string): string {
  if (podeEnviar && estado === 'REPROVADO') {
    return 'Documento reprovado. Envie uma nova versão em PDF.'
  }
  if (podeEnviar) {
    return 'Envie o PDF deste documento para análise.'
  }
  if (podeVisualizar && estado === 'AGUARDANDO_PARECER') {
    return 'Documento enviado. Aguarde o parecer do orientador.'
  }
  if (podeVisualizar && estado === 'APROVADO') {
    return 'Documento aprovado. Você pode visualizar o arquivo.'
  }
  if (podeVisualizar) {
    return 'Você pode visualizar o arquivo enviado.'
  }
  if (estado === 'AGUARDANDO_PARECER') {
    return 'Documento enviado. Aguarde o parecer do orientador.'
  }
  if (estado === 'APROVADO') {
    return 'Documento aprovado. Nenhuma ação necessária no momento.'
  }
  return 'Aguarde. Nenhuma ação disponível no momento.'
}

function DocumentoCard({
  documento,
  pending,
  onEnviar,
  onParecer,
  onErro,
}: {
  documento: DocumentoEstagio
  pending: boolean
  onEnviar: (arquivo: File) => void
  onParecer: (acao: 'APROVAR' | 'REPROVAR', texto: string) => void
  onErro: (mensagem: string) => void
}) {
  const actions = useActions(documento._links)
  const inputRef = useRef<HTMLInputElement>(null)
  const [baixando, setBaixando] = useState(false)
  const podeEnviar = actions.can('upload')
  const podeVisualizar = actions.can('download')
  const podeAprovar = actions.can('aprovar')
  const podeReprovar = actions.can('reprovar')
  const titulo = rotuloTipoDocumento(documento.tipo)
  const descricao = descricaoAcaoDocumento(podeEnviar, podeVisualizar, documento.estado)

  async function visualizar() {
    const href = actions.href('download')
    if (!href) {
      return
    }
    setBaixando(true)
    try {
      await estagiosApi.baixar(href, documento.nomeArquivo || `${documento.tipo.toLowerCase()}.pdf`)
    } catch {
      onErro('Não foi possível abrir o documento.')
    } finally {
      setBaixando(false)
    }
  }

  return (
    <article className="panel estagio-doc-card">
      <header className="estagio-doc-card-head">
        <h2>{titulo}</h2>
        <span className={`badge estado-${documento.estado.toLowerCase()}`}>
          {rotuloEstadoDocumento(documento.estado)}
        </span>
      </header>

      <div className="estagio-doc-card-corpo">
        <p className="muted estagio-doc-card-desc">{descricao}</p>
        <div className="estagio-doc-card-acao">
          {podeEnviar && (
            <>
              <input
                ref={inputRef}
                type="file"
                accept="application/pdf,.pdf"
                className="sr-only"
                aria-label={`Selecionar PDF de ${titulo}`}
                onChange={(event) => {
                  const arquivo = event.target.files?.[0]
                  event.target.value = ''
                  if (arquivo) {
                    onEnviar(arquivo)
                  }
                }}
              />
              <button
                type="button"
                className="ghost"
                disabled={pending}
                onClick={() => inputRef.current?.click()}
              >
                {pending ? 'Enviando…' : 'Enviar'}
              </button>
            </>
          )}
          {!podeEnviar && podeVisualizar && (
            <button type="button" className="ghost" disabled={baixando} onClick={() => void visualizar()}>
              {baixando ? 'Abrindo…' : 'Visualizar'}
            </button>
          )}
          {!podeEnviar && !podeVisualizar && <span className="muted">Aguarde</span>}
        </div>
      </div>

      {(podeAprovar || podeReprovar) && (
        <PainelParecerDocumento documento={documento} pending={pending} onParecer={onParecer} />
      )}
    </article>
  )
}

function PainelParecerDocumento({
  documento,
  pending,
  onParecer,
}: {
  documento: DocumentoEstagio
  pending: boolean
  onParecer: (acao: 'APROVAR' | 'REPROVAR', texto: string) => void
}) {
  const actions = useActions(documento._links)
  const podeAprovar = actions.can('aprovar')
  const podeReprovar = actions.can('reprovar')
  const [texto, setTexto] = useState('')
  const [erro, setErro] = useState<string | null>(null)
  const campoId = `parecer-${documento.id}`

  function disparar(acao: 'APROVAR' | 'REPROVAR') {
    const limpo = texto.trim()
    if (acao === 'REPROVAR' && limpo.length < PARECER_REPROVAR_MIN) {
      setErro(`Informe o parecer para reprovação (mín. ${PARECER_REPROVAR_MIN} caracteres).`)
      document.getElementById(campoId)?.focus()
      return
    }
    if (!limpo) {
      setErro('Informe o parecer.')
      document.getElementById(campoId)?.focus()
      return
    }
    setErro(null)
    onParecer(acao, limpo)
  }

  return (
    <form
      className="painel-decisao painel-decisao-compacto"
      onSubmit={(event) => {
        event.preventDefault()
      }}
    >
      <label htmlFor={campoId}>
        Parecer
        <textarea
          id={campoId}
          name="parecer"
          rows={3}
          value={texto}
          onChange={(event) => {
            setTexto(event.target.value)
            if (erro) {
              setErro(null)
            }
          }}
          className={erro ? 'invalid' : undefined}
          aria-invalid={Boolean(erro)}
          aria-describedby={erro ? `${campoId}-erro` : undefined}
          disabled={pending}
        />
      </label>
      {erro && (
        <p id={`${campoId}-erro`} className="field-error" role="alert">
          {erro}
        </p>
      )}
      <div className="action-bar">
        {podeAprovar && (
          <button type="button" className="success" onClick={() => disparar('APROVAR')} disabled={pending}>
            Aprovar
          </button>
        )}
        {podeReprovar && (
          <button type="button" className="danger" onClick={() => disparar('REPROVAR')} disabled={pending}>
            Reprovar
          </button>
        )}
      </div>
    </form>
  )
}
