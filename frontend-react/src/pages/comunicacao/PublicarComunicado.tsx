import { FormEvent, useRef, useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { useMutation, useQuery } from '@tanstack/react-query'
import { ApiError } from '../../api/client'
import { comunicacoesApi } from '../../api/comunicacoes'
import type { PrioridadeComunicacao } from '../../models/comunicacao'
import { MarkdownPreview } from './MarkdownPreview'
import { MarkdownToolbar } from './MarkdownToolbar'

const PRIORIDADES: PrioridadeComunicacao[] = ['LOW', 'MEDIUM', 'HIGH', 'CRITICAL']

export function PublicarComunicado() {
  const navigate = useNavigate()
  const corpoRef = useRef<HTMLTextAreaElement>(null)
  const [titulo, setTitulo] = useState('')
  const [corpo, setCorpo] = useState('')
  const [audiencia, setAudiencia] = useState('')
  const [prioridade, setPrioridade] = useState<PrioridadeComunicacao>('MEDIUM')
  const [expiraEm, setExpiraEm] = useState('')
  const [erro, setErro] = useState<string | null>(null)

  const audiencias = useQuery({
    queryKey: ['communications', 'audiencias'],
    queryFn: comunicacoesApi.audiencias,
  })
  const publicar = useMutation({
    mutationFn: comunicacoesApi.publicar,
    onSuccess: () => {
      navigate('/comunicacao', { state: { aviso: 'Comunicado publicado com sucesso.' } })
    },
    onError: (error) => {
      setErro(error instanceof ApiError ? error.message : 'Não foi possível publicar o comunicado.')
    },
  })

  const podePublicar = titulo.trim().length > 0 && corpo.trim().length > 0

  function enviar(event: FormEvent) {
    event.preventDefault()
    setErro(null)
    if (!podePublicar || publicar.isPending) {
      return
    }
    const [tipo, id] = audiencia.split(':')
    if ((tipo !== 'TURMA' && tipo !== 'CURSO') || !id) {
      setErro('Selecione a audiência.')
      return
    }
    publicar.mutate({
      titulo: titulo.trim(),
      corpo: corpo.trim(),
      audiencia: { tipo, id },
      prioridade,
      expiraEm: expiraEm ? new Date(expiraEm).toISOString() : null,
    })
  }

  return (
    <section className="page">
      <header className="page-head">
        <div>
          <h1>Publicar comunicado</h1>
          <p className="muted">
            Redija um aviso para a turma ou o curso sob sua responsabilidade. O preview abaixo mostra como a
            mensagem aparecerá no hub de comunicação.
          </p>
        </div>
      </header>

      {audiencias.isLoading && (
        <p className="muted" aria-busy="true">
          Carregando audiências…
        </p>
      )}
      {audiencias.isError && (
        <div className="banner danger" role="alert">
          {audiencias.error instanceof ApiError && audiencias.error.status === 403
            ? 'Você não tem permissão para publicar comunicados.'
            : 'Não foi possível carregar as audiências.'}{' '}
          <button type="button" onClick={() => audiencias.refetch()}>
            Tentar de novo
          </button>
        </div>
      )}
      {audiencias.data && audiencias.data.content.length === 0 && (
        <p className="empty" role="status">
          Nenhuma turma ou curso vinculado.
        </p>
      )}
      {erro && (
        <div className="banner danger" role="alert">
          {erro}
        </div>
      )}

      <form className="panel" onSubmit={enviar}>
        <label>
          Título
          <input value={titulo} onChange={(event) => setTitulo(event.target.value)} maxLength={200} />
        </label>

        <div className="comunicacao-editor-wrap">
          <span className="comunicacao-editor-label" id="corpo-label">
            Corpo
          </span>
          <p className="muted caption" id="corpo-ajuda">
            O corpo usa Markdown: títulos, negrito, listas e links. Use a barra de formatação ou consulte o{' '}
            <a href="https://www.markdownguide.org/basic-syntax/" target="_blank" rel="noreferrer">
              guia básico de Markdown
            </a>
            .
          </p>
          <MarkdownToolbar value={corpo} onChange={setCorpo} textareaRef={corpoRef} />
          <textarea
            ref={corpoRef}
            className="comunicacao-editor"
            aria-labelledby="corpo-label"
            aria-describedby="corpo-ajuda"
            value={corpo}
            onChange={(event) => setCorpo(event.target.value)}
            maxLength={20000}
          />
          <span className="comunicacao-editor-label" id="preview-label">
            Pré-visualização
          </span>
          <MarkdownPreview texto={corpo} />
        </div>

        <div className="grid">
          <label>
            Audiência
            <select value={audiencia} onChange={(event) => setAudiencia(event.target.value)}>
              <option value="">Selecione</option>
              {audiencias.data?.content.map((item) => (
                <option key={`${item.tipo}:${item.id}`} value={`${item.tipo}:${item.id}`}>
                  {item.rotulo}
                </option>
              ))}
            </select>
          </label>
          <label>
            Prioridade
            <select value={prioridade} onChange={(event) => setPrioridade(event.target.value as PrioridadeComunicacao)}>
              {PRIORIDADES.map((item) => (
                <option key={item} value={item}>
                  {item}
                </option>
              ))}
            </select>
          </label>
          <label>
            Expiração
            <input type="datetime-local" value={expiraEm} onChange={(event) => setExpiraEm(event.target.value)} />
          </label>
        </div>

        <button type="submit" disabled={!podePublicar || publicar.isPending}>
          {publicar.isPending ? 'Publicando…' : 'Publicar'}
        </button>
        <p className="muted caption">
          A audiência define o tipo no hub: turma vira comunicado de Turma; curso vira Institucional.
        </p>
      </form>
    </section>
  )
}
