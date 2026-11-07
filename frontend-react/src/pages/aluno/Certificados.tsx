import { useState } from 'react'
import { Link } from 'react-router-dom'
import { useQuery } from '@tanstack/react-query'
import { certificadosApi } from '../../api/certificados'
import { useActions } from '../../hooks/useActions'
import type { Certificado } from '../../models/certificado'

export function Certificados() {
  const [avisoDownload, setAvisoDownload] = useState<string | null>(null)
  const lista = useQuery({
    queryKey: ['certificates', 'me'],
    queryFn: () => certificadosApi.listarMeus(),
  })

  return (
    <section className="page">
      <header className="page-head">
        <div>
          <h1>Certificados</h1>
          <p className="muted">
            Aqui ficam os seus certificados emitidos pelo sistema após aprovação da CAAF.
          </p>
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

      {avisoDownload && (
        <div className="banner danger" role="alert">
          {avisoDownload}
        </div>
      )}

      {lista.isLoading && (
        <p className="muted" aria-busy="true">
          Carregando certificados…
        </p>
      )}
      {lista.data && (
        <table className="certificados-tabela">
          <thead>
            <tr>
              <th scope="col">Tipo</th>
              <th scope="col">Evento/Atividade</th>
              <th scope="col">Data</th>
              <th scope="col">Verificar</th>
              <th scope="col">Download</th>
            </tr>
          </thead>
          <tbody>
            {lista.data.content.length === 0 ? (
              <tr>
                <td colSpan={5} className="tabela-vazio" role="status">
                  Você ainda não possui certificados emitidos.
                </td>
              </tr>
            ) : (
              lista.data.content.map((item) => (
                <Linha
                  key={item.id}
                  item={item}
                  onErroDownload={(mensagem) => setAvisoDownload(mensagem)}
                  onLimparAviso={() => setAvisoDownload(null)}
                />
              ))
            )}
          </tbody>
        </table>
      )}
    </section>
  )
}

function Linha({
  item,
  onErroDownload,
  onLimparAviso,
}: {
  item: Certificado
  onErroDownload: (mensagem: string) => void
  onLimparAviso: () => void
}) {
  const actions = useActions(item._links)
  const data = new Date(item.emitidoEm).toLocaleDateString('pt-BR')
  const [baixando, setBaixando] = useState(false)

  async function baixar() {
    const href = actions.href('download')
    if (!href) {
      return
    }
    onLimparAviso()
    setBaixando(true)
    try {
      await certificadosApi.baixar(href, `certificado-${item.id}.pdf`)
    } catch {
      onErroDownload('Não foi possível baixar o PDF.')
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
        <Link to={`/publico/verificar-certificado/${item.hashSha256}`}>Verificar</Link>
      </td>
      <td>
        {actions.can('download') ? (
          <button type="button" className="ghost" onClick={() => void baixar()} disabled={baixando}>
            {baixando ? 'Baixando…' : 'Download'}
          </button>
        ) : (
          '—'
        )}
      </td>
    </tr>
  )
}
