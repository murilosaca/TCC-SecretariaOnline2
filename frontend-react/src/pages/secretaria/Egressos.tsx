import { useState } from 'react'
import { useSearchParams } from 'react-router-dom'
import { useQuery } from '@tanstack/react-query'
import { academicoApi } from '../../api/academico'
import { ApiError } from '../../api/client'
import { egressosApi } from '../../api/egressos'

export function Egressos() {
  const [params, setParams] = useSearchParams()
  const page = Number(params.get('page') ?? '0') || 0
  const [cursoId, setCursoId] = useState('')
  const [ano, setAno] = useState('')
  const [situacao, setSituacao] = useState('')
  const [exportando, setExportando] = useState(false)
  const [erroExport, setErroExport] = useState<string | null>(null)

  function atualizarFiltro(aplicar: (valor: string) => void, valor: string) {
    aplicar(valor)
    const next = new URLSearchParams(params)
    next.set('page', '0')
    setParams(next)
  }

  const cursos = useQuery({
    queryKey: ['cursos', 'egressos'],
    queryFn: () => academicoApi.listarCursos(0, 100),
  })
  const lista = useQuery({
    queryKey: ['egressos', cursoId, ano, situacao, page],
    queryFn: () =>
      egressosApi.listar({
        cursoId: cursoId || undefined,
        ano: ano || undefined,
        situacao: situacao || undefined,
        page,
        size: 20,
      }),
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
        page: 0,
        size: 100,
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
          <select value={cursoId} onChange={(event) => atualizarFiltro(setCursoId, event.target.value)}>
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
          <input
            value={ano}
            onChange={(event) => atualizarFiltro(setAno, event.target.value)}
            inputMode="numeric"
            placeholder="2026"
          />
        </label>
        <label>
          Situação
          <select value={situacao} onChange={(event) => atualizarFiltro(setSituacao, event.target.value)}>
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
      {lista.data && lista.data.page.totalElements > 100 && (
        <p className="muted">O CSV traz só os 100 primeiros do filtro.</p>
      )}
      {lista.data &&
        page + 1 < lista.data.page.totalPages &&
        lista.data.page.totalElements > lista.data.content.length && (
          <p className="muted">Há mais nesta lista.</p>
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

      {lista.data && lista.data.page.totalPages > 1 && (
        <nav className="pagination" aria-label="Paginação de egressos">
          <button
            type="button"
            disabled={page <= 0}
            onClick={() => {
              const next = new URLSearchParams(params)
              next.set('page', String(page - 1))
              setParams(next)
            }}
          >
            Anterior
          </button>
          <span className="muted">
            Página {page + 1} de {lista.data.page.totalPages}
          </span>
          <button
            type="button"
            disabled={page + 1 >= lista.data.page.totalPages}
            onClick={() => {
              const next = new URLSearchParams(params)
              next.set('page', String(page + 1))
              setParams(next)
            }}
          >
            Próxima
          </button>
        </nav>
      )}
    </section>
  )
}
