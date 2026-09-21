import { FormEvent, useState } from 'react'
import { Link, useParams } from 'react-router-dom'
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

export function EstagioDetalhe() {
  const { id = '' } = useParams()
  const { links } = useAuth()
  const menu = useActions(links)
  const queryClient = useQueryClient()
  const [aba, setAba] = useState<'documentos' | 'pareceres'>('documentos')

  const detalhe = useQuery({
    queryKey: ['estagio', id],
    queryFn: () => estagiosApi.obter(id),
    enabled: Boolean(id),
  })

  const enviar = useMutation({
    mutationFn: ({ tipo, arquivo }: { tipo: string; arquivo: File }) =>
      estagiosApi.enviarDocumento(id, tipo, arquivo),
    onSuccess: (atual) => {
      queryClient.setQueryData(['estagio', id], atual)
      void queryClient.invalidateQueries({ queryKey: ['estagios'] })
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
      queryClient.setQueryData(['estagio', id], atual)
      void queryClient.invalidateQueries({ queryKey: ['estagios'] })
    },
  })

  const encerrar = useMutation({
    mutationFn: () => estagiosApi.encerrar(id),
    onSuccess: (atual) => {
      queryClient.setQueryData(['estagio', id], atual)
      void queryClient.invalidateQueries({ queryKey: ['estagios'] })
    },
  })

  const item = detalhe.data
  const actions = useActions(item?._links)
  const voltar = menu.can('estagios-revisao') && !menu.can('estagios')
    ? { href: '/estagios?to=me', label: '← Fila de revisão' }
    : { href: '/estagios', label: '← Estágios' }
  const erroAcao = enviar.error ?? parecer.error ?? encerrar.error
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

      {item && (
        <>
          <header className="page-head">
            <div>
              <h1>{item.empresa}</h1>
              <p className="muted">
                {item.alunoNome ? `${item.alunoNome} · ` : ''}
                {vigencia(item.inicio, item.fim)}
              </p>
            </div>
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

          {erroAcao && (
            <div className="banner danger" role="alert">
              {erroAcao instanceof ApiError ? erroAcao.message : 'Não foi possível concluir a ação.'}
            </div>
          )}

          <div className="detalhe-grid">
            <div>
              <div className="tabs" role="tablist" aria-label="Estágio">
                <button
                  type="button"
                  role="tab"
                  id="tab-documentos"
                  aria-selected={aba === 'documentos'}
                  aria-controls="painel-documentos"
                  onClick={() => setAba('documentos')}
                >
                  Documentos
                </button>
                <button
                  type="button"
                  role="tab"
                  id="tab-pareceres"
                  aria-selected={aba === 'pareceres'}
                  aria-controls="painel-pareceres"
                  onClick={() => setAba('pareceres')}
                >
                  Pareceres
                </button>
              </div>

              {aba === 'documentos' && (
                <div role="tabpanel" id="painel-documentos" aria-labelledby="tab-documentos">
                  {item.documentos.map((documento) => (
                    <DocumentoCard
                      key={documento.id}
                      documento={documento}
                      pending={pending}
                      onEnviar={(arquivo) => enviar.mutate({ tipo: documento.tipo, arquivo })}
                      onParecer={(acao, texto) =>
                        parecer.mutate({ documentoId: documento.id, acao, texto })
                      }
                    />
                  ))}
                </div>
              )}

              {aba === 'pareceres' && (
                <div role="tabpanel" id="painel-pareceres" aria-labelledby="tab-pareceres">
                  {item.pareceres.length === 0 && (
                    <p className="empty" role="status">
                      Nenhum parecer registrado.
                    </p>
                  )}
                  {item.pareceres.length > 0 && (
                    <ol className="timeline">
                      {item.pareceres.map((registro) => (
                        <li key={registro.id}>
                          <span className={`badge estado-${registro.acao.toLowerCase()}`}>
                            {rotuloAcaoParecer(registro.acao)}
                          </span>
                          <p>
                            <strong>{registro.autorRotulo || 'Orientador'}</strong>
                            {' · '}
                            {dataBr(registro.createdAt)}
                            {' · '}
                            {rotuloTipoDocumento(registro.tipoDocumento)}
                          </p>
                          <p>{registro.texto}</p>
                        </li>
                      ))}
                    </ol>
                  )}
                </div>
              )}
            </div>

            <aside className="panel">
              <h2>Metadados</h2>
              <dl className="resumo">
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
            </aside>
          </div>
        </>
      )}
    </section>
  )
}

function DocumentoCard({
  documento,
  pending,
  onEnviar,
  onParecer,
}: {
  documento: DocumentoEstagio
  pending: boolean
  onEnviar: (arquivo: File) => void
  onParecer: (acao: 'APROVAR' | 'REPROVAR', texto: string) => void
}) {
  const actions = useActions(documento._links)
  const [arquivo, setArquivo] = useState<File | null>(null)
  const [erroArquivo, setErroArquivo] = useState<string | null>(null)
  const inputId = `upload-${documento.id}`

  function onSubmit(event: FormEvent) {
    event.preventDefault()
    if (!arquivo) {
      setErroArquivo('Selecione um PDF.')
      document.getElementById(inputId)?.focus()
      return
    }
    setErroArquivo(null)
    onEnviar(arquivo)
  }

  return (
    <article className="panel">
      <header className="page-head">
        <h2>{rotuloTipoDocumento(documento.tipo)}</h2>
        <span className={`badge estado-${documento.estado.toLowerCase()}`}>
          {rotuloEstadoDocumento(documento.estado)}
        </span>
      </header>
      {documento.nomeArquivo && <p className="muted">{documento.nomeArquivo}</p>}
      {actions.can('upload') && (
        <form onSubmit={onSubmit}>
          <label htmlFor={inputId}>
            Enviar PDF
            <input
              id={inputId}
              type="file"
              accept="application/pdf,.pdf"
              onChange={(event) => {
                setArquivo(event.target.files?.[0] ?? null)
                if (erroArquivo) {
                  setErroArquivo(null)
                }
              }}
              disabled={pending}
            />
          </label>
          {erroArquivo && (
            <p className="field-error" role="alert">
              {erroArquivo}
            </p>
          )}
          <button type="submit" disabled={pending}>
            {pending ? 'Enviando…' : 'Enviar PDF'}
          </button>
        </form>
      )}
      <PainelParecerDocumento
        documento={documento}
        pending={pending}
        onParecer={onParecer}
      />
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

  if (!podeAprovar && !podeReprovar) {
    return null
  }

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
      className="painel-decisao"
      onSubmit={(event) => {
        event.preventDefault()
      }}
    >
      <h3>Parecer</h3>
      <label htmlFor={campoId}>
        Parecer
        <textarea
          id={campoId}
          name="parecer"
          rows={4}
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
