import { FormEvent, useMemo, useState } from 'react'
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { academicoApi } from '../../api/academico'
import { ApiError } from '../../api/client'
import { diplomasApi } from '../../api/diplomas'
import { useActions } from '../../hooks/useActions'
import type { Diploma, ElegivelColacao } from '../../models/diploma'

const METODOS = [
  { value: 'PRESENCIAL', label: 'Retirada presencial' },
  { value: 'PROCURACAO', label: 'Procuração' },
  { value: 'CORREIO', label: 'Correio' },
] as const

export function Diplomas() {
  const queryClient = useQueryClient()
  const [passo, setPasso] = useState(1)
  const [cursoId, setCursoId] = useState('')
  const [periodoId, setPeriodoId] = useState('')
  const [selecionados, setSelecionados] = useState<Set<string>>(new Set())
  const [dataColacao, setDataColacao] = useState('')
  const [livro, setLivro] = useState('')
  const [folha, setFolha] = useState('')
  const [turma, setTurma] = useState('')
  const [erro, setErro] = useState<string | null>(null)
  const [entregaId, setEntregaId] = useState<string | null>(null)
  const [metodo, setMetodo] = useState<(typeof METODOS)[number]['value']>('PRESENCIAL')
  const [dataEntrega, setDataEntrega] = useState(() => new Date().toISOString().slice(0, 10))

  const cursos = useQuery({
    queryKey: ['cursos', 'diplomas'],
    queryFn: () => academicoApi.listarCursos(0, 100),
  })
  const periodos = useQuery({
    queryKey: ['periodos', 'diplomas'],
    queryFn: () => academicoApi.listarPeriodos(0, 100),
  })

  const elegiveis = useQuery({
    queryKey: ['diplomas', 'elegiveis', cursoId, periodoId],
    queryFn: () => diplomasApi.elegiveis(cursoId, periodoId),
    enabled: Boolean(cursoId && periodoId),
  })
  const colecao = useActions(elegiveis.data?._links)

  const pendentes = useQuery({
    queryKey: ['diplomas', 'pendentes', cursoId],
    queryFn: () => diplomasApi.listar(cursoId, 'PENDENTE'),
    enabled: Boolean(cursoId),
  })

  const confirmar = useMutation({
    mutationFn: () =>
      diplomasApi.confirmar({
        cursoId,
        periodoId,
        alunoIds: Array.from(selecionados),
        dataColacao: new Date(`${dataColacao}T12:00:00`).toISOString(),
        livro,
        folha,
        turma: turma.trim() || null,
      }),
    onSuccess: () => {
      setErro(null)
      setSelecionados(new Set())
      setPasso(1)
      setLivro('')
      setFolha('')
      setTurma('')
      setDataColacao('')
      queryClient.invalidateQueries({ queryKey: ['diplomas'] })
    },
    onError: (falha) => {
      setErro(falha instanceof ApiError ? falha.message : 'Falha ao confirmar a colação.')
    },
  })

  const entregar = useMutation({
    mutationFn: () =>
      diplomasApi.confirmarEntrega(entregaId as string, {
        metodo,
        dataEntrega: new Date(`${dataEntrega}T12:00:00`).toISOString(),
      }),
    onSuccess: () => {
      setEntregaId(null)
      setErro(null)
      queryClient.invalidateQueries({ queryKey: ['diplomas'] })
    },
    onError: (falha) => {
      setErro(falha instanceof ApiError ? falha.message : 'Falha ao confirmar a entrega.')
    },
  })

  const upload = useMutation({
    mutationFn: ({ id, file }: { id: string; file: File }) => diplomasApi.uploadPdf(id, file),
    onSuccess: () => {
      setErro(null)
      queryClient.invalidateQueries({ queryKey: ['diplomas'] })
    },
    onError: (falha) => {
      setErro(falha instanceof ApiError ? falha.message : 'Falha ao enviar o PDF do diploma.')
    },
  })

  const itens = elegiveis.data?.content ?? []
  const elegiveisCount = useMemo(() => itens.filter((item) => item.elegivel).length, [itens])

  function toggle(item: ElegivelColacao) {
    if (!item.elegivel) {
      return
    }
    setSelecionados((atual) => {
      const next = new Set(atual)
      if (next.has(item.alunoId)) {
        next.delete(item.alunoId)
      } else {
        next.add(item.alunoId)
      }
      return next
    })
  }

  function avancarPasso2() {
    setErro(null)
    if (selecionados.size === 0) {
      setErro('Selecione ao menos um aluno elegível.')
      return
    }
    setPasso(2)
  }

  function onConfirmar(event: FormEvent) {
    event.preventDefault()
    setErro(null)
    if (!colecao.can('confirm')) {
      setErro('Sem permissão para confirmar a colação.')
      return
    }
    if (!dataColacao || !livro.trim() || !folha.trim()) {
      setErro('Preencha data da cerimônia, livro e folha.')
      return
    }
    confirmar.mutate()
  }

  function onEntregar(event: FormEvent) {
    event.preventDefault()
    if (!entregaId) {
      return
    }
    entregar.mutate()
  }

  const forbidden =
    (elegiveis.isError && elegiveis.error instanceof ApiError && elegiveis.error.status === 403) ||
    (pendentes.isError && pendentes.error instanceof ApiError && pendentes.error.status === 403)

  return (
    <section className="page wizard">
      <header className="page-head">
        <div>
          <h1>Diplomas e colação</h1>
          <p className="muted">Wizard de colação em lote e confirmação de entrega física (F5.11).</p>
        </div>
      </header>

      {forbidden && (
        <p className="empty" role="alert">
          Você não tem permissão para registrar diplomas.
        </p>
      )}

      {erro && (
        <div className="banner danger" role="alert">
          {erro}
        </div>
      )}

      <section className="panel" aria-labelledby="filtros-colacao">
        <h2 id="filtros-colacao">Curso e período</h2>
        <div className="form-grid">
          <label>
            Curso
            <select
              value={cursoId}
              onChange={(event) => {
                setCursoId(event.target.value)
                setSelecionados(new Set())
                setPasso(1)
              }}
            >
              <option value="">Selecione…</option>
              {(cursos.data?.content ?? []).map((curso) => (
                <option key={curso.id} value={curso.id}>
                  {curso.sigla} — {curso.nome}
                </option>
              ))}
            </select>
          </label>
          <label>
            Período letivo
            <select
              value={periodoId}
              onChange={(event) => {
                setPeriodoId(event.target.value)
                setSelecionados(new Set())
                setPasso(1)
              }}
            >
              <option value="">Selecione…</option>
              {(periodos.data?.content ?? []).map((periodo) => (
                <option key={periodo.id} value={periodo.id}>
                  {periodo.ano}/{periodo.semestre}
                </option>
              ))}
            </select>
          </label>
        </div>
        {(cursos.isError || periodos.isError) && (
          <p className="field-error" role="alert">
            Não foi possível carregar cursos ou períodos.
          </p>
        )}
      </section>

      {cursoId && periodoId && (
        <>
          <ol className="stepper" aria-label="Passos do wizard de colação">
            <li aria-current={passo === 1 ? 'step' : undefined} className={passo === 1 ? 'atual' : undefined}>
              1. Selecionar elegíveis
            </li>
            <li aria-current={passo === 2 ? 'step' : undefined} className={passo === 2 ? 'atual' : undefined}>
              2. Confirmar colação
            </li>
          </ol>

          {passo === 1 && (
            <section className="panel" aria-labelledby="elegiveis-titulo">
              <h2 id="elegiveis-titulo">Elegíveis ({elegiveisCount})</h2>
              {elegiveis.isLoading && <p className="muted">Carregando alunos…</p>}
              {elegiveis.isError && !forbidden && (
                <div className="banner danger" role="alert">
                  Não foi possível carregar a elegibilidade.{' '}
                  <button type="button" onClick={() => elegiveis.refetch()}>
                    Tentar de novo
                  </button>
                </div>
              )}
              {elegiveis.data && itens.length === 0 && (
                <p className="empty" role="status">
                  Nenhum aluno ativo neste curso para colação.
                </p>
              )}
              {itens.length > 0 && (
                <table>
                  <thead>
                    <tr>
                      <th scope="col">Selecionar</th>
                      <th scope="col">Nome</th>
                      <th scope="col">GRR</th>
                      <th scope="col">Situação</th>
                    </tr>
                  </thead>
                  <tbody>
                    {itens.map((item) => (
                      <tr key={item.alunoId}>
                        <td>
                          <input
                            type="checkbox"
                            checked={selecionados.has(item.alunoId)}
                            disabled={!item.elegivel}
                            title={item.bloqueio?.razao ?? undefined}
                            aria-label={`Selecionar ${item.nome}`}
                            onChange={() => toggle(item)}
                          />
                        </td>
                        <td>{item.nome}</td>
                        <td>
                          <code>{item.grr}</code>
                        </td>
                        <td>
                          {item.elegivel ? (
                            <span className="badge estado-concluido">Elegível</span>
                          ) : (
                            <span className="muted" title={item.bloqueio?.detalhe ?? undefined}>
                              {item.bloqueio?.razao ?? 'Inelegível'}
                            </span>
                          )}
                        </td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              )}
              <div className="wizard-actions">
                <button
                  type="button"
                  disabled={selecionados.size === 0 || !colecao.can('confirm')}
                  onClick={avancarPasso2}
                >
                  Continuar ({selecionados.size})
                </button>
              </div>
            </section>
          )}

          {passo === 2 && (
            <section className="panel" aria-labelledby="confirmar-titulo">
              <h2 id="confirmar-titulo">Confirmar colação</h2>
              <p className="muted">{selecionados.size} aluno(s) selecionado(s).</p>
              <form className="form-grid" onSubmit={onConfirmar}>
                <label>
                  Data da cerimônia
                  <input
                    type="date"
                    required
                    value={dataColacao}
                    onChange={(event) => setDataColacao(event.target.value)}
                  />
                </label>
                <label>
                  Livro
                  <input required value={livro} onChange={(event) => setLivro(event.target.value)} />
                </label>
                <label>
                  Folha
                  <input required value={folha} onChange={(event) => setFolha(event.target.value)} />
                </label>
                <label>
                  Turma (opcional)
                  <input value={turma} onChange={(event) => setTurma(event.target.value)} />
                </label>
                <div className="wizard-actions">
                  <button type="button" className="ghost" onClick={() => setPasso(1)}>
                    Voltar
                  </button>
                  <button type="submit" disabled={confirmar.isPending || !colecao.can('confirm')}>
                    {confirmar.isPending ? 'Confirmando…' : 'Confirmar colação'}
                  </button>
                </div>
              </form>
            </section>
          )}
        </>
      )}

      {cursoId && (
        <section className="panel" aria-labelledby="pendentes-titulo">
          <h2 id="pendentes-titulo">Entrega física pendente</h2>
          {pendentes.isLoading && <p className="muted">Carregando diplomas…</p>}
          {pendentes.isError && !forbidden && (
            <div className="banner danger" role="alert">
              Não foi possível carregar diplomas pendentes.{' '}
              <button type="button" onClick={() => pendentes.refetch()}>
                Tentar de novo
              </button>
            </div>
          )}
          {pendentes.data && pendentes.data.content.length === 0 && (
            <p className="empty" role="status">
              Nenhum diploma aguardando entrega neste curso.
            </p>
          )}
          {pendentes.data && pendentes.data.content.length > 0 && (
            <table>
              <thead>
                <tr>
                  <th scope="col">Aluno</th>
                  <th scope="col">Número</th>
                  <th scope="col">Colação</th>
                  <th scope="col">PDF</th>
                  <th scope="col">Ações</th>
                </tr>
              </thead>
              <tbody>
                {pendentes.data.content.map((item) => (
                  <LinhaPendente
                    key={item.id}
                    item={item}
                    onEntregar={() => {
                      setEntregaId(item.id)
                      setErro(null)
                    }}
                    onUpload={(file) => upload.mutate({ id: item.id, file })}
                    uploading={upload.isPending}
                  />
                ))}
              </tbody>
            </table>
          )}

          {entregaId && (
            <form className="form-grid" onSubmit={onEntregar} aria-label="Confirmar entrega">
              <h3>Confirmar entrega</h3>
              <label>
                Modalidade
                <select
                  value={metodo}
                  onChange={(event) => setMetodo(event.target.value as (typeof METODOS)[number]['value'])}
                >
                  {METODOS.map((item) => (
                    <option key={item.value} value={item.value}>
                      {item.label}
                    </option>
                  ))}
                </select>
              </label>
              <label>
                Data da entrega
                <input
                  type="date"
                  required
                  value={dataEntrega}
                  onChange={(event) => setDataEntrega(event.target.value)}
                />
              </label>
              <div className="wizard-actions">
                <button type="button" className="ghost" onClick={() => setEntregaId(null)}>
                  Cancelar
                </button>
                <button type="submit" disabled={entregar.isPending}>
                  {entregar.isPending ? 'Registrando…' : 'Confirmar entrega'}
                </button>
              </div>
            </form>
          )}
        </section>
      )}
    </section>
  )
}

function LinhaPendente({
  item,
  onEntregar,
  onUpload,
  uploading,
}: {
  item: Diploma
  onEntregar: () => void
  onUpload: (file: File) => void
  uploading: boolean
}) {
  const actions = useActions(item._links)
  return (
    <tr>
      <td>{item.alunoNome ?? item.alunoId}</td>
      <td>
        <code>{item.numero}</code>
      </td>
      <td>{new Date(item.dataColacao).toLocaleDateString('pt-BR')}</td>
      <td>
        {item.temPdf ? (
          <span className="badge estado-concluido">Anexado</span>
        ) : actions.can('upload-pdf') ? (
          <label className="ghost">
            Enviar PDF
            <input
              type="file"
              accept="application/pdf"
              hidden
              disabled={uploading}
              onChange={(event) => {
                const file = event.target.files?.[0]
                if (file) {
                  onUpload(file)
                }
                event.target.value = ''
              }}
            />
          </label>
        ) : (
          <span className="muted">Sem PDF</span>
        )}
      </td>
      <td className="actions">
        {actions.can('confirm-delivery') && (
          <button type="button" className="secondary" onClick={onEntregar}>
            Confirmar entrega
          </button>
        )}
      </td>
    </tr>
  )
}
