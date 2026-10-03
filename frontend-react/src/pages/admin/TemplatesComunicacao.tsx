import { FormEvent, useEffect, useMemo, useState } from 'react'
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { ApiError } from '../../api/client'
import { templatesApi, type RevisaoTemplate, type TemplateResumo } from '../../api/templates'
import { useActions } from '../../hooks/useActions'
import { partesDoPreview, VARIAVEIS_TEMPLATE } from '../../lib/templatePreview'

export function TemplatesComunicacao() {
  const client = useQueryClient()
  const lista = useQuery({
    queryKey: ['admin-templates'],
    queryFn: () => templatesApi.listar(),
  })
  const [selecionado, setSelecionado] = useState<string | null>(null)
  const [versaoVista, setVersaoVista] = useState<number | null>(null)
  const [nome, setNome] = useState('')
  const [assunto, setAssunto] = useState('')
  const [corpo, setCorpo] = useState('')
  const detalhe = useQuery({
    queryKey: ['admin-template', selecionado],
    queryFn: () => templatesApi.buscar(selecionado!),
    enabled: Boolean(selecionado),
  })
  const actions = useActions(detalhe.data?._links)
  const revisao = useMemo(() => {
    const itens = detalhe.data?.revisoes ?? []
    return itens.find((item) => item.versao === versaoVista) ?? itens.find((item) => item.status === 'CURRENT')
  }, [detalhe.data, versaoVista])
  const somenteLeitura = revisao?.status === 'ARCHIVED'
  const sugestao = sugestaoAberta(corpo)

  useEffect(() => {
    if (!selecionado && lista.data?.content[0]) {
      setSelecionado(lista.data.content[0].id)
    }
  }, [lista.data, selecionado])

  useEffect(() => {
    if (!revisao) {
      return
    }
    setAssunto(revisao.assunto)
    setCorpo(revisao.corpo)
  }, [revisao])

  const criar = useMutation({
    mutationFn: () => templatesApi.criar({ nome, assunto, corpo }),
    onSuccess: (criado) => {
      setSelecionado(criado.id)
      setVersaoVista(null)
      void client.invalidateQueries({ queryKey: ['admin-templates'] })
    },
  })
  const salvar = useMutation({
    mutationFn: () => templatesApi.salvar(selecionado!, { assunto, corpo }),
    onSuccess: () => {
      setVersaoVista(null)
      void client.invalidateQueries({ queryKey: ['admin-template', selecionado] })
      void client.invalidateQueries({ queryKey: ['admin-templates'] })
    },
  })

  function escolher(item: TemplateResumo) {
    setSelecionado(item.id)
    setVersaoVista(null)
  }

  function verRevisao(item: RevisaoTemplate) {
    setVersaoVista(item.versao)
  }

  function inserirVariavel(variavel: string) {
    setCorpo((atual) => atual.replace(/\{\{[a-zA-Z0-9_]*$/, `{{${variavel}}}`))
  }

  function criarTemplate(event: FormEvent) {
    event.preventDefault()
    criar.mutate()
  }

  return (
    <section className="page" aria-labelledby="templates-titulo">
      <header className="page-head">
        <div>
          <h1 id="templates-titulo">Templates de comunicação</h1>
          <p className="muted">O preview acompanha o texto. Não há outro GET a cada tecla.</p>
        </div>
      </header>
      {lista.isLoading && <p className="muted">Carregando…</p>}
      {lista.isError && <p className="empty">Não foi possível carregar os templates.</p>}
      <div className="editor-3">
        <div>
          <h2>Templates</h2>
          <ul>
            {(lista.data?.content ?? []).map((item) => (
              <li key={item.id}>
                <button type="button" onClick={() => escolher(item)}>
                  {item.nome}
                </button>
              </li>
            ))}
          </ul>
          <form onSubmit={criarTemplate}>
            <h2>Novo</h2>
            <label htmlFor="template-nome">Nome</label>
            <input id="template-nome" value={nome} onChange={(event) => setNome(event.target.value)} />
            <button type="submit" disabled={criar.isPending}>
              Criar
            </button>
            {criar.isError && <p className="banner danger">{mensagem(criar.error)}</p>}
          </form>
        </div>
        <div>
          {somenteLeitura && revisao && (
            <p className="banner info" role="status">
              Versão {revisao.versao} — somente leitura
            </p>
          )}
          <label htmlFor="template-assunto">Assunto</label>
          <input
            id="template-assunto"
            value={assunto}
            readOnly={somenteLeitura}
            onChange={(event) => setAssunto(event.target.value)}
          />
          <label htmlFor="template-corpo">Markdown</label>
          <textarea
            id="template-corpo"
            className="comunicacao-editor"
            value={corpo}
            readOnly={somenteLeitura}
            onChange={(event) => setCorpo(event.target.value)}
          />
          {sugestao && (
            <ul role="listbox" aria-label="Variáveis">
              {VARIAVEIS_TEMPLATE.filter((item) => item.startsWith(sugestao)).map((item) => (
                <li key={item}>
                  <button type="button" onClick={() => inserirVariavel(item)}>
                    {item}
                  </button>
                </li>
              ))}
            </ul>
          )}
          <h2>Pré-visualização</h2>
          <div className="markdown-preview" aria-label="Pré-visualização">
            {partesDoPreview(corpo).map((parte, index) =>
              parte.tipo === 'variavel' && parte.desconhecida ? (
                <span
                  key={index}
                  className="danger"
                  title="Variável não reconhecida — use: nome, protocolo, curso, link, data"
                >
                  {parte.bruto}
                </span>
              ) : (
                <span key={index}>{parte.texto}</span>
              ),
            )}
          </div>
          {actions.can('salvar') && (
            <button type="button" disabled={somenteLeitura || salvar.isPending} onClick={() => salvar.mutate()}>
              Salvar
            </button>
          )}
          {salvar.isError && <p className="banner danger">{mensagem(salvar.error)}</p>}
        </div>
        <div>
          <h2>Histórico</h2>
          <ul>
            {(detalhe.data?.revisoes ?? []).map((item) => (
              <li key={item.versao}>
                <button type="button" onClick={() => verRevisao(item)}>
                  Versão {item.versao} <span className="badge">{item.status}</span>
                </button>
              </li>
            ))}
          </ul>
        </div>
      </div>
    </section>
  )
}

function sugestaoAberta(corpo: string): string | null {
  const match = corpo.match(/\{\{([a-zA-Z0-9_]*)$/)
  return match ? match[1] : null
}

function mensagem(erro: unknown): string {
  if (erro instanceof ApiError) {
    return erro.message
  }
  return 'Não foi possível gravar o template.'
}
