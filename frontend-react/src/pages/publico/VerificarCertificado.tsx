import { Link, useParams } from 'react-router-dom'
import { useQuery } from '@tanstack/react-query'
import { ApiError } from '../../api/client'
import { publicoApi } from '../../api/publico'
import {
  chaveEd25519,
  hashValido,
  normalizarHash,
  verificarAssinaturaEd25519,
} from '../../lib/certificadoPublico'

type EstadoVerificacao = 'carregando' | 'valido' | 'invalido' | 'erro'

export function VerificarCertificado() {
  const { hash: bruto } = useParams()
  const hash = bruto ? normalizarHash(bruto) : ''
  const formatoOk = Boolean(hash) && hashValido(hash)

  const consulta = useQuery({
    queryKey: ['publico', 'certificado', hash],
    queryFn: async () => {
      const metadados = await publicoApi.verificarCertificado(hash)
      const jwks = await publicoApi.jwks()
      const jwk = chaveEd25519(jwks)
      if (!jwk) {
        throw new Error('jwks-sem-ed25519')
      }
      const assinaturaOk = await verificarAssinaturaEd25519(
        metadados.hashSha256,
        metadados.assinaturaEd25519,
        jwk,
      )
      return { metadados, assinaturaOk }
    },
    enabled: formatoOk,
    retry: false,
  })

  if (!formatoOk) {
    return <ResultadoInvalido />
  }

  if (consulta.isLoading) {
    return (
      <section className="page" aria-busy="true">
        <h1>Verificar certificado</h1>
        <p className="muted">Consultando o certificado…</p>
        <div className="skeleton skeleton-kpi" aria-hidden="true" />
      </section>
    )
  }

  if (consulta.isError) {
    const status = consulta.error instanceof ApiError ? consulta.error.status : 0
    if (status === 404 || status === 422) {
      return <ResultadoInvalido />
    }
    if (status === 429) {
      return (
        <section className="page">
          <h1>Verificar certificado</h1>
          <div className="banner warning" role="alert">
            Muitas verificações realizadas. Aguarde antes de tentar novamente.
          </div>
        </section>
      )
    }
    return (
      <section className="page">
        <h1>Verificar certificado</h1>
        <div className="banner danger" role="alert">
          Não foi possível verificar o certificado no momento.{' '}
          <button type="button" onClick={() => consulta.refetch()}>
            Tentar de novo
          </button>
        </div>
      </section>
    )
  }

  const dados = consulta.data
  if (!dados || !dados.assinaturaOk) {
    return <ResultadoInvalido />
  }

  return <ResultadoValido hash={hash} dados={dados.metadados} />
}

function ResultadoValido({
  hash,
  dados,
}: {
  hash: string
  dados: {
    beneficiarioNome: string
    atividade: string
    cargaHoraria: number
    emitidoEm: string
    jwksUrl: string
  }
}) {
  return (
    <section className="page">
      <h1>Verificar certificado</h1>
      <Selo estado="valido" />
      <article className="panel">
        <p>
          <strong>Beneficiário</strong> {dados.beneficiarioNome}
        </p>
        <p>
          <strong>Atividade</strong> {dados.atividade}
        </p>
        <p>
          <strong>Carga horária</strong> {dados.cargaHoraria} h
        </p>
        <p>
          <strong>Emissão</strong> {new Date(dados.emitidoEm).toLocaleString('pt-BR')}
        </p>
        <p>
          <strong>Hash</strong> <code>{hash}</code>
        </p>
      </article>
      <article className="panel">
        <p>
          <span aria-hidden="true">🛡️ </span>
          Assinado digitalmente pelo servidor da UFPR SEPT
        </p>
        <p>
          <a href={dados.jwksUrl} target="_blank" rel="noreferrer">
            Ver chave pública
          </a>
        </p>
      </article>
    </section>
  )
}

function ResultadoInvalido() {
  return (
    <section className="page">
      <h1>Verificar certificado</h1>
      <Selo estado="invalido" />
      <p className="empty">
        Este certificado não pôde ser verificado. Ele pode ser falso ou ter sido adulterado.
      </p>
      <p>
        <Link to="/login">Voltar ao login</Link>
      </p>
    </section>
  )
}

function Selo({ estado }: { estado: Exclude<EstadoVerificacao, 'carregando' | 'erro'> }) {
  const valido = estado === 'valido'
  return (
    <div
      className={`verification-seal ${valido ? 'success' : 'danger'}`}
      role="status"
      aria-live="assertive"
      aria-label={valido ? 'Certificado válido' : 'Certificado inválido'}
    >
      <span className="verification-seal-icon" aria-hidden="true">
        {valido ? '✓' : '!'}
      </span>
      <span className={`badge ${valido ? 'estado-aprovada' : 'estado-indeferida'}`}>
        {valido ? 'Certificado válido' : 'Certificado inválido'}
      </span>
    </div>
  )
}
