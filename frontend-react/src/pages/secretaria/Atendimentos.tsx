import { FormEvent, useMemo, useState } from 'react'
import { useMutation, useQuery } from '@tanstack/react-query'
import { academicoApi } from '../../api/academico'
import { atendimentosApi } from '../../api/atendimentos'
import { ApiError } from '../../api/client'
import { useActions } from '../../hooks/useActions'

const LIMITE_MB = 10
const LIMITE_BYTES = LIMITE_MB * 1024 * 1024

const vazio = {
  alunoId: '',
  alunoRotulo: '',
  categoriaId: '',
  assunto: '',
  resposta: '',
}

export function Atendimentos() {
  const [form, setForm] = useState(vazio)
  const [termo, setTermo] = useState('')
  const [anexo, setAnexo] = useState<File | null>(null)
  const [erroArquivo, setErroArquivo] = useState<string | null>(null)
  const [sucesso, setSucesso] = useState<string | null>(null)

  const categorias = useQuery({
    queryKey: ['atendimentos', 'categorias'],
    queryFn: atendimentosApi.categorias,
    staleTime: 5 * 60 * 1000,
  })
  const colecao = useActions(categorias.data?._links)
  const forbidden = categorias.isError && categorias.error instanceof ApiError && categorias.error.status === 403

  const alunos = useQuery({
    queryKey: ['alunos', 'atendimento', termo],
    queryFn: () => academicoApi.listarAlunos(undefined, termo || undefined, 0, 100),
    enabled: colecao.can('registrar') || Boolean(categorias.data),
  })

  const opcoes = useMemo(() => {
    const conteudo = alunos.data?.content ?? []
    if (form.alunoId && !conteudo.some((item) => item.id === form.alunoId)) {
      return [{ id: form.alunoId, grr: '', nome: form.alunoRotulo || 'Aluno selecionado' }, ...conteudo]
    }
    return conteudo
  }, [alunos.data, form.alunoId, form.alunoRotulo])

  const alunoSelecionado = opcoes.find((item) => item.id === form.alunoId)
  const categoriaSelecionada = categorias.data?.content.find((item) => item.id === form.categoriaId)

  const registrar = useMutation({
    mutationFn: () =>
      atendimentosApi.registrar({
        alunoId: form.alunoId,
        categoriaId: form.categoriaId,
        assunto: form.assunto,
        resposta: form.resposta,
        anexo,
      }),
    onSuccess: () => {
      setForm(vazio)
      setTermo('')
      setAnexo(null)
      setErroArquivo(null)
      setSucesso('Atendimento registrado')
    },
  })

  function onArquivo(file: File | null) {
    setErroArquivo(null)
    if (!file) {
      setAnexo(null)
      return
    }
    if (file.size > LIMITE_BYTES) {
      setAnexo(null)
      setErroArquivo('Arquivo excede 10 MB')
      return
    }
    if (file.type !== 'application/pdf' && !file.name.toLowerCase().endsWith('.pdf')) {
      setAnexo(null)
      setErroArquivo('Envie um PDF de até 10 MB.')
      return
    }
    setAnexo(file)
  }

  function onSubmit(event: FormEvent) {
    event.preventDefault()
    setSucesso(null)
    registrar.mutate()
  }

  return (
    <section className="page">
      <header className="page-head">
        <div>
          <h1>Registrar atendimento</h1>
          <p className="muted">Registro imutável do guichê. O aluno dá ciência depois em Meus atendimentos.</p>
        </div>
      </header>

      {forbidden && (
        <p className="empty" role="status">
          Você não tem permissão para registrar atendimentos.
        </p>
      )}
      {categorias.isError && !forbidden && (
        <div className="banner danger" role="alert">
          Não foi possível carregar as categorias.{' '}
          <button type="button" onClick={() => categorias.refetch()}>
            Tentar de novo
          </button>
        </div>
      )}
      {sucesso && (
        <div className="banner" role="status">
          {sucesso}
        </div>
      )}
      {registrar.isError && (
        <div className="banner danger" role="alert">
          {registrar.error instanceof ApiError ? registrar.error.message : 'Falha ao registrar o atendimento.'}
        </div>
      )}

      {colecao.can('registrar') && (
        <form className="card" onSubmit={onSubmit}>
          <label>
            Buscar aluno
            <input
              value={termo}
              onChange={(event) => setTermo(event.target.value)}
              placeholder="Nome ou GRR"
            />
          </label>
          <label>
            Aluno
            <select
              required
              value={form.alunoId}
              onChange={(event) => {
                const escolhido = opcoes.find((item) => item.id === event.target.value)
                setForm({
                  ...form,
                  alunoId: event.target.value,
                  alunoRotulo: escolhido ? `${escolhido.nome} - ${escolhido.grr}` : '',
                })
              }}
            >
              <option value="">Selecione</option>
              {opcoes.map((aluno) => (
                <option key={aluno.id} value={aluno.id}>
                  {aluno.nome} - {aluno.grr}
                </option>
              ))}
            </select>
          </label>
          <label>
            Categoria
            <select
              required
              value={form.categoriaId}
              onChange={(event) => setForm({ ...form, categoriaId: event.target.value })}
            >
              <option value="">Selecione</option>
              {(categorias.data?.content ?? []).map((categoria) => (
                <option key={categoria.id} value={categoria.id}>
                  {categoria.nome}
                </option>
              ))}
            </select>
          </label>
          <label>
            Assunto
            <input
              required
              maxLength={200}
              value={form.assunto}
              onChange={(event) => setForm({ ...form, assunto: event.target.value })}
            />
          </label>
          <label>
            Resposta
            <textarea
              required
              rows={5}
              value={form.resposta}
              onChange={(event) => setForm({ ...form, resposta: event.target.value })}
            />
          </label>
          <label>
            Anexo (PDF, até 10 MB)
            <input
              type="file"
              accept="application/pdf,.pdf"
              onChange={(event) => onArquivo(event.target.files?.[0] ?? null)}
            />
          </label>
          {erroArquivo && (
            <p className="field-error" role="alert">
              {erroArquivo}
            </p>
          )}
          <aside className="card" aria-live="polite">
            <h2>Preview da notificação</h2>
            <p>
              <strong>{alunoSelecionado?.nome ?? 'Aluno'}</strong>
              {categoriaSelecionada ? ` · ${categoriaSelecionada.nome}` : ''}
            </p>
            <p>{form.assunto || 'Assunto'}</p>
            <p>{form.resposta || 'A resposta aparecerá aqui.'}</p>
            <p className="muted">Este e-mail será enviado ao aluno ao confirmar</p>
          </aside>
          <button type="submit" disabled={registrar.isPending || Boolean(erroArquivo)}>
            {registrar.isPending ? 'Registrando…' : 'Registrar'}
          </button>
        </form>
      )}
    </section>
  )
}
