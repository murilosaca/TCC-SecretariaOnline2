import { useState } from 'react'
import { useQuery } from '@tanstack/react-query'
import { academicoApi } from '../../api/academico'
import { ApiError } from '../../api/client'
import { egressosApi } from '../../api/egressos'

export function Egressos() {
  const [cursoId, setCursoId] = useState('')
  const [ano, setAno] = useState('')
  const [situacao, setSituacao] = useState('')
  const [exportando, setExportando] = useState(false)
  const [erroExport, setErroExport] = useState<string | null>(null)

  const cursos = useQuery({
    queryKey: ['cursos', 'egressos'],
    queryFn: () => academicoApi.listarCursos(0, 100),
  })
  const lista = useQuery({
    queryKey: ['egressos', cursoId, ano, situacao],
    queryFn: () => egressosApi.listar({ cursoId: cursoId || undefined, ano: ano || undefined, situacao: situacao || undefined }),
  })
  const forbidden = lista.isError && lista.error instanceof ApiError && lista.error.status === 403

  async function exportarCsv() {
    setExportando(true)
    setErroExport(null)
    try {
      const blob = await egressosApi.exportarCsv({
        cursoId: cursoId || undefined,
        ano: ano || undefined,
        situacao: situacao || undefined,
      })
      const url = URL.createObjectURL(blob)
      const a = document.createElement('a')
      a.href = url
      a.download = `egressos-${new Date().toISOString().slice(0, 10)}.csv`
      a.click()
      URL.revokeObjectURL(url)
    } catch (erro) {
      setErroExport(erro instanceof ApiError ? erro.message : 'Não foi possível exportar o CSV.')
    } finally {
      setExportando(false)
    }
  }

  return (
    <section className="page">
      <header className="page-head">
        <div>
          <h1>Egressos</h1>
          <p className="muted">Lista read-only dos cursos vinculados. A colação continua em Diplomas.</p>
        </div>
        <button type="button" onClick={() => void exportarCsv()} disabled={exportando || forbidden}>
          {exportando ? 'Exportando…' : 'Exportar CSV'}
        </button>
      </header>

      <div className="filter-bar">
        <label>
          Curso
          <select value={cursoId} onChange={(event) => setCursoId(event.target.value)}>
            <option value="">Todos</option>
            {(cursos.data?.content ?? []).map((curso) => (
              <option key={curso.id} value={curso.id}>
                {curso.sigla}
              </option>
            ))}
          </select>
        </label>
        <label>
          Ano
          <input value={ano} onChange={(event) => setAno(event.target.value)} inputMode="numeric" placeholder="2026" />
        </label>
        <label>
          Situação
          <select value={situacao} onChange={(event) => setSituacao(event.target.value)}>
            <option value="">Todas</option>
            <option value="PENDENTE">PENDENTE</option>
            <option value="ENTREGUE">ENTREGUE</option>
          </select>
        </label>
      </div>

      {forbidden && (
        <p className="empty" role="status">
          Lista de egressos indisponível para esta sessão.
        </p>
      )}
      {lista.isError && !forbidden && (
        <div className="banner danger" role="alert">
          Não foi possível carregar os egressos.{' '}
          <button type="button" onClick={() => lista.refetch()}>
            Tentar de novo
          </button>
        </div>
      )}
      {erroExport && (
        <div className="banner danger" role="alert">
          {erroExport}
        </div>
      )}
      {lista.isLoading && (
        <p className="muted" aria-busy="true">
          Carregando egressos…
        </p>
      )}
      {lista.data && lista.data.content.length === 0 && (
        <p className="empty" role="status">
          Nenhum egresso encontrado.
        </p>
      )}
      {lista.data && lista.data.content.length > 0 && (
        <table>
          <thead>
            <tr>
              <th scope="col">Nome</th>
              <th scope="col">Curso</th>
              <th scope="col">Ano de colação</th>
              <th scope="col">Situação do diploma</th>
            </tr>
          </thead>
          <tbody>
            {lista.data.content.map((item) => (
              <tr key={item.alunoId}>
                <td>{item.nome}</td>
                <td>{item.cursoSigla}</td>
                <td>{item.anoColacao}</td>
                <td>
                  <span className={`badge estado-${item.situacaoDiploma.toLowerCase()}`}>{item.situacaoDiploma}</span>
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      )}
    </section>
  )
}
