import { FormEvent, useRef, useState } from 'react'
import { Link, useLocation, useNavigate, useSearchParams } from 'react-router-dom'
import { ApiError } from '../../api/client'
import { useAuth } from '../../auth/AuthContext'
import { destinoPosLogin } from '../../lib/destinoPosLogin'
import { normalizarProtocolo, protocoloValido } from '../../lib/protocoloPublico'

export function Login() {
  const { login } = useAuth()
  const navigate = useNavigate()
  const location = useLocation()
  const [params] = useSearchParams()
  const [protocolo, setProtocolo] = useState('')
  const [erroProtocolo, setErroProtocolo] = useState<string | null>(null)
  const senhaRef = useRef<HTMLInputElement>(null)
  const [identificador, setIdentificador] = useState('')
  const [senha, setSenha] = useState('')
  const [mostrarSenha, setMostrarSenha] = useState(false)
  const [erroCampo, setErroCampo] = useState<{ identificador?: string; senha?: string }>({})
  const [aviso, setAviso] = useState<string | null>(
    params.get('redefinida') === '1' ? 'Senha redefinida com sucesso.' : null,
  )
  const [avisoTipo, setAvisoTipo] = useState<'danger' | 'warning' | 'success'>(
    params.get('redefinida') === '1' ? 'success' : 'danger',
  )
  const [enviando, setEnviando] = useState(false)

  function validar(): boolean {
    const erros: { identificador?: string; senha?: string } = {}
    if (!identificador.trim()) {
      erros.identificador = 'Informe e-mail ou GRR'
    }
    if (!senha) {
      erros.senha = 'Informe a senha'
    }
    setErroCampo(erros)
    if (erros.identificador) {
      document.getElementById('identificador')?.focus()
    } else if (erros.senha) {
      senhaRef.current?.focus()
    }
    return Object.keys(erros).length === 0
  }

  async function onSubmit(event: FormEvent) {
    event.preventDefault()
    setAviso(null)
    if (!validar()) {
      return
    }
    setEnviando(true)
    try {
      const result = await login(identificador.trim(), senha)
      const from = (location.state as { from?: unknown } | null)?.from
      navigate(destinoPosLogin(result.mustChangePassword, from), { replace: true })
    } catch (error) {
      setSenha('')
      if (error instanceof ApiError && error.status === 429) {
        setAvisoTipo('warning')
        setAviso('Muitas tentativas. Aguarde antes de tentar novamente.')
      } else {
        setAvisoTipo('danger')
        setAviso('Credenciais inválidas. Verifique seus dados e tente novamente.')
      }
    } finally {
      setEnviando(false)
    }
  }

  return (
    <section className="page">
      <h1>Entrar</h1>
      <p className="lead">Acesso ao Secretaria Online 2 (RF-F0-001).</p>
      {aviso && (
        <div className={`banner ${avisoTipo}`} role="status" aria-live="polite">
          {aviso}
        </div>
      )}
      <form className="panel auth-form" onSubmit={onSubmit} noValidate>
        <label>
          E-mail ou GRR
          <input
            id="identificador"
            name="identificador"
            autoComplete="username"
            placeholder="GRR20241234 ou e-mail"
            value={identificador}
            onChange={(event) => setIdentificador(event.target.value)}
            className={erroCampo.identificador ? 'invalid' : undefined}
            aria-invalid={Boolean(erroCampo.identificador)}
          />
          {erroCampo.identificador && <span className="field-error">{erroCampo.identificador}</span>}
        </label>
        <label>
          Senha
          <span className="password-row">
            <input
              ref={senhaRef}
              name="senha"
              type={mostrarSenha ? 'text' : 'password'}
              autoComplete="current-password"
              value={senha}
              onChange={(event) => setSenha(event.target.value)}
              className={erroCampo.senha ? 'invalid' : undefined}
              aria-invalid={Boolean(erroCampo.senha)}
            />
            <button
              type="button"
              className="ghost"
              aria-label={mostrarSenha ? 'Ocultar senha' : 'Mostrar senha'}
              onClick={() => setMostrarSenha((atual) => !atual)}
            >
              {mostrarSenha ? 'Ocultar' : 'Mostrar'}
            </button>
          </span>
          {erroCampo.senha && <span className="field-error">{erroCampo.senha}</span>}
        </label>
        <p>
          <Link to="/recuperar-senha">Esqueci minha senha</Link>
        </p>
        <button type="submit" disabled={enviando}>
          {enviando ? 'Entrando...' : 'Entrar'}
        </button>
      </form>
      <form
        className="panel auth-form"
        onSubmit={(event) => {
          event.preventDefault()
          const valor = normalizarProtocolo(protocolo)
          if (!protocoloValido(valor)) {
            setErroProtocolo('Informe um protocolo no formato PROT-AAAA-NNNNN.')
            return
          }
          setErroProtocolo(null)
          navigate(`/publico/verificar-protocolo/${valor}`)
        }}
      >
        <label>
          Verificar protocolo
          <input
            name="protocolo"
            placeholder="PROT-2026-00001"
            value={protocolo}
            onChange={(event) => setProtocolo(event.target.value)}
            aria-invalid={Boolean(erroProtocolo)}
          />
          {erroProtocolo && <span className="field-error">{erroProtocolo}</span>}
        </label>
        <button type="submit">Consultar protocolo</button>
        <p className="muted">
          Em desenvolvimento, abra uma declaração simples em /solicitacoes/nova para obter um número
          PROT-AAAA-NNNNN. Certificado digital (F0.7) ainda não está disponível.
        </p>
      </form>
      <p className="muted auth-links">
        <Link to="/contato">Contato</Link>
        {' · '}
        <Link to="/publico/verificar-protocolo">Verificar protocolo</Link>
        {' · '}
        <Link to="/publico/verificar-certificado/demo">Verificar certificado</Link>
      </p>
    </section>
  )
}
