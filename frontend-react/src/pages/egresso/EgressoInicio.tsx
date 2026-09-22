import { useState } from 'react'
import { Link } from 'react-router-dom'
import { useQuery } from '@tanstack/react-query'
import { ApiError } from '../../api/client'
import { egressosApi } from '../../api/egressos'
import { useActions } from '../../hooks/useActions'
import { truncarHash } from '../../lib/protocoloPublico'
import type { EgressoCertificado, EgressoColacao, EgressoDiploma, EgressoPainel } from '../../models/egresso'

export function EgressoInicio() {
  const painel = useQuery({
    queryKey: ['egressos', 'me'],
    queryFn: () => egressosApi.painel(),
  })
  const forbidden = painel.isError && painel.error instanceof ApiError && painel.error.status === 403

  return (
    <section className="page dashboard" aria-labelledby="egresso-titulo">
      <header className="page-head">
        <div>
          {painel.isLoading ? (
            <h1 id="egresso-titulo">
              <span className="skeleton skeleton-title" aria-hidden="true" />
              <span className="muted">Carregando painel…</span>
            </h1>
          ) : painel.data ? (
            <>
              <h1 id="egresso-titulo">Olá, {painel.data.nome}</h1>
              <p className="muted caption">
                <span>{painel.data.curso ?? 'Curso indisponível'}</span>
                {' · '}
                <span>
                  {painel.data.concluidoEm
                    ? new Date(painel.data.concluidoEm).toLocaleDateString('pt-BR')
                    : 'Data de conclusão indisponível'}
                </span>
              </p>
              <p>
                <span className="badge estado-concluido">Concluído</span>
              </p>
            </>
          ) : (
            <>
              <h1 id="egresso-titulo">Início</h1>
              <p className="muted caption">Portal do egresso · SEPT/UFPR</p>
            </>
          )}
          <p className="muted">Painel somente leitura.</p>
        </div>
      </header>

      {painel.isLoading && (
        <div className="kpi-row" aria-busy="true">
          <div className="skeleton skeleton-kpi" />
          <div className="skeleton skeleton-kpi" />
          <div className="skeleton skeleton-kpi" />
        </div>
      )}

      {forbidden && (
        <p className="empty" role="alert">
          Você não tem permissão para acessar este recurso.
        </p>
      )}
      {painel.isError && !forbidden && (
        <div className="banner danger" role="alert">
          Não foi possível carregar o painel.{' '}
          <button type="button" onClick={() => painel.refetch()}>
            Tentar de novo
          </button>
        </div>
      )}

      {painel.data && (
        <>
          <KpiRow painel={painel.data} />
          <div className="dashboard-main">
            <DiplomaBloco diploma={painel.data.diploma} />
            <CertificadosBloco itens={painel.data.certificados} />
            <ColacaoBloco colacao={painel.data.colacao} />
          </div>
        </>
      )}
    </section>
  )
}

function KpiRow({ painel }: { painel: EgressoPainel }) {
  const horas = painel.kpis.horasFormativasValidadas
  const certificados = painel.kpis.certificadosEmitidos
  const diploma = painel.kpis.situacaoDiploma
  return (
    <div className="kpi-row">
      <article className="kpi-card">
        <p className="kpi-label">Horas formativas</p>
        {horas === null || horas === undefined ? (
          <p className="muted">Indisponível neste momento.</p>
        ) : (
          <p className="kpi-value">{horas} h</p>
        )}
      </article>
      <article className="kpi-card">
        <p className="kpi-label">Certificados</p>
        {certificados === null || certificados === undefined ? (
          <p className="muted">Indisponível neste momento.</p>
        ) : (
          <p className="kpi-value">{certificados}</p>
        )}
      </article>
      <article className="kpi-card">
        <p className="kpi-label">Diploma</p>
        {diploma === 'EMITIDO' ? (
          <p>
            <span className="badge estado-concluido">Emitido</span>
          </p>
        ) : (
          <p className="muted">Indisponível</p>
        )}
      </article>
    </div>
  )
}

function DiplomaBloco({ diploma }: { diploma: EgressoDiploma | null }) {
  const actions = useActions(diploma?._links)
  const [erro, setErro] = useState<string | null>(null)

  async function baixar() {
    const href = actions.href('download')
    if (!href || !diploma) {
      return
    }
    setErro(null)
    try {
      await baixarPdf(href, `diploma-${diploma.numero}.pdf`)
    } catch {
      setErro('Não foi possível baixar o diploma.')
    }
  }

  return (
    <section className="panel" aria-labelledby="diploma-titulo">
      <h2 id="diploma-titulo">Diploma</h2>
      {!diploma && (
        <p className="empty" role="status">
          O registro do diploma ainda não está disponível.
        </p>
      )}
      {diploma && (
        <>
          <p>Número: {diploma.numero}</p>
          <p>Emissão: {new Date(diploma.emitidoEm).toLocaleDateString('pt-BR')}</p>
          <p>
            <span className="badge estado-concluido">Emitido</span>
          </p>
          {actions.can('download') && (
            <button type="button" className="secondary" aria-label="Baixar diploma" onClick={() => void baixar()}>
              Download
            </button>
          )}
          {erro && (
            <span className="field-error" role="alert">
              {erro}
            </span>
          )}
        </>
      )}
    </section>
  )
}

function CertificadosBloco({ itens }: { itens: EgressoCertificado[] }) {
  return (
    <section className="panel" aria-labelledby="certificados-titulo">
      <h2 id="certificados-titulo">Certificados</h2>
      {itens.length === 0 ? (
        <p className="empty" role="status">
          Nenhum certificado emitido durante o curso.
        </p>
      ) : (
        <table>
          <thead>
            <tr>
              <th scope="col">Atividade</th>
              <th scope="col">Data</th>
              <th scope="col">Hash</th>
              <th scope="col">PDF</th>
            </tr>
          </thead>
          <tbody>
            {itens.map((item) => (
              <Linha key={item.id} item={item} />
            ))}
          </tbody>
        </table>
      )}
    </section>
  )
}

function Linha({ item }: { item: EgressoCertificado }) {
  const actions = useActions(item._links)
  const [baixando, setBaixando] = useState(false)
  const [erro, setErro] = useState<string | null>(null)

  async function reemitir() {
    const href = actions.href('reemitir')
    if (!href) {
      return
    }
    setErro(null)
    setBaixando(true)
    try {
      await baixarPdf(href, `certificado-${item.id}.pdf`)
    } catch {
      setErro('Não foi possível reemitir o PDF.')
    } finally {
      setBaixando(false)
    }
  }

  return (
    <tr>
      <td>{item.titulo}</td>
      <td>{new Date(item.emitidoEm).toLocaleDateString('pt-BR')}</td>
      <td>
        <code>{truncarHash(item.hashSha256)}</code>
        <p className="muted">
          <Link to={`/publico/verificar-certificado/${item.hashSha256}`}>Verificar</Link>
        </p>
      </td>
      <td className="actions">
        {actions.can('reemitir') && (
          <button
            type="button"
            className="secondary"
            aria-label={`Baixar certificado de ${item.titulo}`}
            onClick={() => void reemitir()}
            disabled={baixando}
          >
            {baixando ? 'Baixando…' : 'Reemitir PDF'}
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

async function baixarPdf(href: string, nomeArquivo: string) {
  const blob = await egressosApi.baixar(href)
  const url = URL.createObjectURL(blob)
  const ancora = document.createElement('a')
  ancora.href = url
  ancora.download = nomeArquivo
  ancora.click()
  URL.revokeObjectURL(url)
}

function ColacaoBloco({ colacao }: { colacao: EgressoColacao | null }) {
  return (
    <section className="panel" aria-labelledby="colacao-titulo">
      <h2 id="colacao-titulo">Colação</h2>
      {!colacao && (
        <p className="empty" role="status">
          Dados de colação indisponíveis até o registro do diploma.
        </p>
      )}
      {colacao && (
        <p>
          {colacao.data ? new Date(colacao.data).toLocaleDateString('pt-BR') : 'Data indisponível'}
          {colacao.turma ? ` · Turma ${colacao.turma}` : ''}
        </p>
      )}
    </section>
  )
}
