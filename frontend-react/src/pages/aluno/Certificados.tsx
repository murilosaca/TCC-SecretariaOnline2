import { useState } from 'react'
import { Link } from 'react-router-dom'
import { useQuery } from '@tanstack/react-query'
import { certificadosApi } from '../../api/certificados'
import { useActions } from '../../hooks/useActions'
import { truncarHash } from '../../lib/protocoloPublico'
import type { Certificado } from '../../models/certificado'

export function Certificados() {
  const lista = useQuery({
    queryKey: ['certificates', 'me'],
    queryFn: () => certificadosApi.listarMeus(),
  })

  return (
    <section className="page">
      <header className="page-head">
        <div>
          <h1>Certificados</h1>
          <p className="muted">Documentos emitidos pelo sistema após aprovação da CAAF (RF-F1-010).</p>
        </div>
      </header>

      {lista.isError && (
        <div className="banner danger" role="alert">
          Não foi possível carregar os certificados.{' '}
          <button type="button" onClick={() => lista.refetch()}>
            Tentar de novo
          </button>
        </div>
      )}

      {lista.isLoading && (
        <p className="muted" aria-busy="true">
          Carregando certificados…
        </p>
      )}
      {lista.data && lista.data.content.length === 0 && (
        <p className="empty" role="status">
          Você ainda não possui certificados emitidos.
        </p>
      )}
      {lista.data && lista.data.content.length > 0 && (
        <table>
          <thead>
            <tr>
              <th scope="col">Tipo</th>
              <th scope="col">Evento/Atividade</th>
              <th scope="col">Data</th>
              <th scope="col">Hash</th>
              <th scope="col">Download</th>
            </tr>
          </thead>
          <tbody>
            {lista.data.content.map((item) => (
              <Linha key={item.id} item={item} />
            ))}
          </tbody>
        </table>
      )}
    </section>
  )
}

function Linha({ item }: { item: Certificado }) {
  const actions = useActions(item._links)
  const data = new Date(item.emitidoEm).toLocaleDateString('pt-BR')
  const [baixando, setBaixando] = useState(false)
  const [erro, setErro] = useState<string | null>(null)

  async function baixar() {
    const href = actions.href('download')
    if (!href) {
      return
    }
    setErro(null)
    setBaixando(true)
    try {
      await certificadosApi.baixar(href, `certificado-${item.id}.pdf`)
    } catch {
      setErro('Não foi possível baixar o PDF.')
    } finally {
      setBaixando(false)
    }
  }

  return (
    <tr>
      <td>{item.tipo === 'FORMATIVA' ? 'Formativa' : item.tipo}</td>
      <td>{item.titulo}</td>
      <td>{data}</td>
      <td>
        <code>{truncarHash(item.hashSha256)}</code>
        <p className="muted">
          <Link to={`/publico/verificar-certificado/${item.hashSha256}`}>Verificar</Link>
        </p>
      </td>
      <td className="actions">
        {actions.can('download') && (
          <button type="button" className="ghost" onClick={() => void baixar()} disabled={baixando}>
            {baixando ? 'Baixando…' : 'Download'}
          </button>
        )}
        {erro && (
          <span className="field-error" role="alert">
            {erro}
          </span>
        )}
      </td>
    </tr>
  )
}
