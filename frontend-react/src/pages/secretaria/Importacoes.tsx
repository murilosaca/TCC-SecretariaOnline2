import { useEffect, useState } from 'react'
import { useMutation, useQuery } from '@tanstack/react-query'
import { ApiError } from '../../api/client'
import { importacoesApi, type ImportacaoJob } from '../../api/importacoes'

const KINDS = [
  { id: 'alunos', label: 'Alunos' },
  { id: 'disciplinas', label: 'Disciplinas' },
  { id: 'usuarios', label: 'Usuários' },
  { id: 'alocacao_professor', label: 'Alocação de professor' },
]
const LIMITE_BYTES = 20 * 1024 * 1024

export function Importacoes() {
  const [kind, setKind] = useState(KINDS[0].id)
  const [jobId, setJobId] = useState<string | null>(null)
  const [bloqueio, setBloqueio] = useState<string | null>(null)
  const [erro, setErro] = useState<string | null>(null)

  const job = useQuery({
    queryKey: ['importacoes', jobId],
    queryFn: () => importacoesApi.obter(jobId as string),
    enabled: Boolean(jobId),
    refetchInterval: (query) => {
      const status = query.state.data?.status
      return status === 'RECEBIDO' || status === 'VALIDANDO' ? 2000 : false
    },
  })

  const enviar = useMutation({
    mutationFn: (arquivo: File) => importacoesApi.enviar(kind, arquivo),
    onSuccess: (criado) => {
      setJobId(criado.id)
      setErro(null)
    },
    onError: (falha) => setErro(falha instanceof ApiError ? falha.message : 'Não foi possível enviar a planilha.'),
  })

  const confirmar = useMutation({
    mutationFn: () => importacoesApi.confirmar(jobId as string),
    onSuccess: (atual) => setJobId(atual.id),
    onError: (falha) => setErro(falha instanceof ApiError ? falha.message : 'Não foi possível confirmar.'),
  })

  useEffect(() => {
    if (job.data) {
      setJobId(job.data.id)
    }
  }, [job.data])

  async function baixarModelo() {
    const blob = await importacoesApi.modelo(kind)
    const url = URL.createObjectURL(blob)
    const a = document.createElement('a')
    a.href = url
    a.download = `${kind}_modelo.csv`
    a.click()
    URL.revokeObjectURL(url)
  }

  function aoArquivo(arquivo: File | undefined) {
    setBloqueio(null)
    if (!arquivo) return
    if (arquivo.size > LIMITE_BYTES) {
      setBloqueio('Arquivo excede 20 MB.')
      return
    }
    enviar.mutate(arquivo)
  }

  const atual = job.data
  const podeConfirmar = Boolean(atual && atual._links.confirm && atual.errorCount === 0)

  return (
    <section className="page" aria-labelledby="importacoes-titulo">
      <header className="page-head">
        <div>
          <h1 id="importacoes-titulo">Importações</h1>
          <p className="muted">Modelo, validação e confirmação em lotes (F5.16).</p>
        </div>
      </header>
      <div className="panel">
        <label htmlFor="kind-importacao">Modelo</label>
        <select id="kind-importacao" value={kind} onChange={(event) => setKind(event.target.value)}>
          {KINDS.map((item) => (
            <option key={item.id} value={item.id}>
              {item.label}
            </option>
          ))}
        </select>
        <button type="button" onClick={() => void baixarModelo()}>
          Baixar modelo
        </button>
        <label htmlFor="arquivo-importacao">Planilha</label>
        <input
          id="arquivo-importacao"
          type="file"
          accept=".csv,.xlsx,text/csv,application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
          onChange={(event) => aoArquivo(event.target.files?.[0])}
        />
      </div>
      {bloqueio && (
        <div className="banner danger" role="alert">
          {bloqueio}
        </div>
      )}
      {erro && (
        <div className="banner danger" role="alert">
          {erro}
        </div>
      )}
      {atual && <Preview job={atual} podeConfirmar={podeConfirmar} confirmando={confirmar.isPending} onConfirm={() => confirmar.mutate()} />}
    </section>
  )
}

function Preview({
  job,
  podeConfirmar,
  confirmando,
  onConfirm,
}: {
  job: ImportacaoJob
  podeConfirmar: boolean
  confirmando: boolean
  onConfirm: () => void
}) {
  return (
    <div className="panel" aria-live="polite">
      <p>
        Status: {job.status}. Válidas: {job.validCount}. Erros: {job.errorCount}.
      </p>
      {job.errorCount > 0 && <p>Corrija os {job.errorCount} erros antes de confirmar.</p>}
      <table>
        <tbody>
          {(job.linhas ?? []).map((linha) => (
            <tr key={linha.numero} className={linha.status === 'INVALID' ? 'row-invalid' : undefined}>
              <td>{linha.numero}</td>
              <td>{linha.status}</td>
              <td>{linha.mensagem}</td>
            </tr>
          ))}
        </tbody>
      </table>
      <button type="button" disabled={!podeConfirmar || confirmando} onClick={onConfirm}>
        {confirmando ? 'Confirmando…' : 'Confirmar importação'}
      </button>
    </div>
  )
}
