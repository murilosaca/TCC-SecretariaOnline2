import { FormEvent, useState } from 'react'
import { Link, useNavigate, useParams } from 'react-router-dom'
import { useQuery } from '@tanstack/react-query'
import { ApiError } from '../../api/client'
import { publicoApi } from '../../api/publico'
import { normalizarProtocolo, protocoloValido, truncarHash } from '../../lib/protocoloPublico'

export function VerificarProtocolo() {
  const { id } = useParams()
  const protocolo = id ? normalizarProtocolo(id) : ''
  const formatoOk = Boolean(protocolo) && protocoloValido(protocolo)
  const consulta = useQuery({
    queryKey: ['publico', 'protocolo', protocolo],
    queryFn: () => publicoApi.protocolo(protocolo),
    enabled: formatoOk,
    retry: false,
  })

  if (!id) {
    return <ConsultaFormulario />
  }

  if (!formatoOk) {
    return <ResultadoNaoEncontrado protocolo={id} invalido />
  }

  if (consulta.isLoading) {
    return (
      <section className="page" aria-busy="true">
        <h1>Verificar protocolo</h1>
        <p className="muted">Consultando o protocolo {protocolo}…</p>
      </section>
    )
  }

  if (consulta.isError) {
    const status = consulta.error instanceof ApiError ? consulta.error.status : 0
    if (status === 404 || status === 422) {
      return <ResultadoNaoEncontrado protocolo={protocolo} />
    }
    return (
      <section className="page">
        <h1>Verificar protocolo</h1>
        <div className="banner danger" role="alert">
          Não foi possível consultar o protocolo no momento.{' '}
          <button type="button" onClick={() => consulta.refetch()}>
            Tentar de novo
          </button>
        </div>
        <p>
          <Link to="/publico/verificar-protocolo">Informar outro protocolo</Link>
        </p>
      </section>
    )
  }

  const dados = consulta.data
  if (!dados) {
    return <ResultadoNaoEncontrado protocolo={protocolo} />
  }

  return (
    <section className="page">
      <h1>Verificar protocolo</h1>
      <p className="lead">
        Consulta pública de metadados (RF-F0-006). O PDF escolhido é comparado neste navegador e não
        segue para o servidor.
      </p>
      <article className="panel">
        <p>
          <strong>Protocolo</strong> {dados.protocolo}
        </p>
        <p>
          <strong>Tipo</strong> {dados.tipoNome}
        </p>
        <p>
          <strong>Estado</strong>{' '}
          <span className={`badge estado-${dados.estado.toLowerCase()}`}>{dados.estado}</span>
        </p>
        <p>
          <strong>Hash</strong> <code>{truncarHash(dados.hashSha256)}</code>
        </p>
        {dados.hashSha256 && (
          <details>
            <summary>Hash completo</summary>
            <code>{dados.hashSha256}</code>
          </details>
        )}
      </article>
      {dados.hashSha256 && <ZonaHash esperado={dados.hashSha256} />}
      <p>
        <Link to="/publico/verificar-protocolo">Consultar outro protocolo</Link>
      </p>
    </section>
  )
}

function ZonaHash({ esperado }: { esperado: string }) {
  const [resultado, setResultado] = useState<'ok' | 'divergente' | null>(null)

  async function comparar(file: File | undefined) {
    if (!file) {
      return
    }
    const digest = await crypto.subtle.digest('SHA-256', await file.arrayBuffer())
    const hex = [...new Uint8Array(digest)].map((byte) => byte.toString(16).padStart(2, '0')).join('')
    setResultado(hex === esperado.trim().toLowerCase() ? 'ok' : 'divergente')
  }

  return (
    <div className="panel">
      <p>Compare o PDF recebido com o hash registrado. O arquivo permanece neste computador.</p>
      <label>
        Selecionar arquivo
        <input
          type="file"
          accept="application/pdf,.pdf"
          onChange={(event) => {
            void comparar(event.target.files?.[0])
          }}
        />
      </label>
      {resultado === 'ok' && (
        <div className="banner success" role="status" aria-live="assertive">
          Hash confere. O documento é autêntico e não foi modificado.
        </div>
      )}
      {resultado === 'divergente' && (
        <div className="banner danger" role="alert" aria-live="assertive">
          Atenção: o hash deste arquivo NÃO confere com o protocolo registrado. O documento pode ter
          sido adulterado.
        </div>
      )}
    </div>
  )
}

function ConsultaFormulario() {
  const navigate = useNavigate()
  const [protocolo, setProtocolo] = useState('')
  const [erro, setErro] = useState<string | null>(null)

  function onSubmit(event: FormEvent) {
    event.preventDefault()
    const valor = normalizarProtocolo(protocolo)
    if (!protocoloValido(valor)) {
      setErro('Informe um protocolo no formato PROT-AAAA-NNNNN.')
      return
    }
    navigate(`/publico/verificar-protocolo/${valor}`)
  }

  return (
    <section className="page">
      <h1>Verificar protocolo</h1>
      <p className="lead">Informe o número público do requerimento (PROT-AAAA-NNNNN).</p>
      <form className="panel auth-form" onSubmit={onSubmit} noValidate>
        <label>
          Protocolo
          <input
            name="protocolo"
            placeholder="PROT-2026-00001"
            value={protocolo}
            onChange={(event) => setProtocolo(event.target.value)}
            aria-invalid={Boolean(erro)}
          />
          {erro && <span className="field-error">{erro}</span>}
        </label>
        <button type="submit">Consultar</button>
      </form>
    </section>
  )
}

function ResultadoNaoEncontrado({ protocolo, invalido }: { protocolo: string; invalido?: boolean }) {
  return (
    <section className="page">
      <h1>Verificar protocolo</h1>
      <p className="empty">
        {invalido
          ? `O identificador “${protocolo}” não é um protocolo válido.`
          : `Nenhum protocolo encontrado para ${protocolo}.`}
      </p>
      <p>
        <Link to="/publico/verificar-protocolo">Informar outro protocolo</Link>
      </p>
    </section>
  )
}
