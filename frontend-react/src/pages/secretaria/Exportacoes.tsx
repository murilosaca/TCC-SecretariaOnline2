import { useState } from 'react'
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { ApiError } from '../../api/client'
import { exportacoesApi, type ExportacaoJob } from '../../api/exportacoes'
import { useActions } from '../../hooks/useActions'

export function Exportacoes() {
  const queryClient = useQueryClient()
  const [aviso, setAviso] = useState<string | null>(null)
  const catalogo = useQuery({
    queryKey: ['exportacoes'],
    queryFn: () => exportacoesApi.listar(),
    refetchInterval: (query) => (query.state.data?.content.some((job) => job.status === 'PROCESSANDO') ? 10_000 : false),
  })

  const solicitar = useMutation({
    mutationFn: (kind: string) => exportacoesApi.solicitar(kind),
    onSuccess: async (job) => {
      setAviso(`Exportação ${job.kind} em processamento.`)
      await queryClient.invalidateQueries({ queryKey: ['exportacoes'] })
    },
    onError: (erro) => setAviso(erro instanceof ApiError ? erro.message : 'Não foi possível solicitar a exportação.'),
  })

  async function baixar(job: ExportacaoJob) {
    const href = job._links.download
    if (!href) return
    const resposta = await exportacoesApi.download(job.id)
    window.location.assign(resposta.url)
  }

  return (
    <section className="page" aria-labelledby="exportacoes-titulo">
      <header className="page-head">
        <div>
          <h1 id="exportacoes-titulo">Exportações</h1>
          <p className="muted">O arquivo fica pronto em segundo plano. O download usa o link da resposta.</p>
        </div>
      </header>
      <div aria-live="polite">{aviso}</div>
      <div className="cards">
        {(catalogo.data?.kinds ?? []).map((kind) => (
          <article key={kind.kind} className="card">
            <h2>{kind.titulo}</h2>
            <button type="button" disabled={solicitar.isPending} onClick={() => solicitar.mutate(kind.kind)}>
              Gerar
            </button>
          </article>
        ))}
      </div>
      <table>
        <thead>
          <tr>
            <th>Tipo</th>
            <th>Status</th>
            <th>Arquivo</th>
          </tr>
        </thead>
        <tbody>
          {(catalogo.data?.content ?? []).map((job) => (
            <Historico key={job.id} job={job} onDownload={baixar} />
          ))}
        </tbody>
      </table>
    </section>
  )
}

function Historico({ job, onDownload }: { job: ExportacaoJob; onDownload: (job: ExportacaoJob) => void }) {
  const acoes = useActions(job._links)
  return (
    <tr>
      <td>{job.titulo}</td>
      <td>{rotulo(job.status)}</td>
      <td>
        {acoes.can('download') && (
          <button type="button" onClick={() => onDownload(job)}>
            Baixar
          </button>
        )}
      </td>
    </tr>
  )
}

function rotulo(status: string): string {
  if (status === 'PROCESSANDO') return 'processando'
  if (status === 'PRONTO') return 'pronto'
  if (status === 'EXPIRADO') return 'expirado'
  return status.toLowerCase()
}
