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
      <p className="lead">Consulta pública de metadados (RF-F0-006). Sem PDF e sem upload nesta fatia.</p>
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
      </article>
      <p>
        <Link to="/publico/verificar-protocolo">Consultar outro protocolo</Link>
      </p>
    </section>
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
