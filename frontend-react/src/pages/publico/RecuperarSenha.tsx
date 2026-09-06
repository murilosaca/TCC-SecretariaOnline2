import { FormEvent, useState } from 'react'
import { Link } from 'react-router-dom'
import { authApi } from '../../api/auth'
import { ApiError } from '../../api/client'
import { isValidEmail } from '../../auth/password'

export function RecuperarSenha() {
  const [email, setEmail] = useState('')
  const [erroCampo, setErroCampo] = useState<string | null>(null)
  const [aviso, setAviso] = useState<string | null>(null)
  const [avisoTipo, setAvisoTipo] = useState<'danger' | 'warning' | 'info'>('info')
  const [enviando, setEnviando] = useState(false)
  const [sucesso, setSucesso] = useState(false)

  async function onSubmit(event: FormEvent) {
    event.preventDefault()
    setAviso(null)
    if (!isValidEmail(email)) {
      setErroCampo('Informe um e-mail válido')
      document.getElementById('email')?.focus()
      return
    }
    setErroCampo(null)
    setEnviando(true)
    try {
      await authApi.recuperarSenha(email.trim())
      setSucesso(true)
      setAvisoTipo('info')
      setAviso('Se este e-mail estiver cadastrado, você receberá um link válido por 24 horas.')
    } catch (error) {
      if (error instanceof ApiError && error.status === 429) {
        setAvisoTipo('warning')
        setAviso('Muitas tentativas. Aguarde antes de tentar novamente.')
      } else {
        setAvisoTipo('danger')
        setAviso('Erro ao processar a solicitação. Verifique sua conexão e tente novamente.')
      }
    } finally {
      setEnviando(false)
    }
  }

  return (
    <section className="page">
      <p>
        <Link to="/login" className="ghost-link">
          ← Voltar
        </Link>
      </p>
      <h1>Recuperar senha</h1>
      <p className="lead">Informe o e-mail cadastrado. A resposta é a mesma exista ou não a conta.</p>
      {aviso && (
        <div className={`banner ${avisoTipo}`} role="status" aria-live="polite">
          {aviso}
        </div>
      )}
      {!sucesso && (
        <form className="panel auth-form" onSubmit={onSubmit} noValidate>
          <label>
            E-mail
            <input
              id="email"
              name="email"
              type="email"
              autoComplete="email"
              value={email}
              disabled={enviando}
              onChange={(event) => setEmail(event.target.value)}
              className={erroCampo ? 'invalid' : undefined}
              aria-invalid={Boolean(erroCampo)}
            />
            {erroCampo && <span className="field-error">{erroCampo}</span>}
          </label>
          <button type="submit" disabled={enviando}>
            {enviando ? 'Enviando...' : 'Enviar link'}
          </button>
        </form>
      )}
    </section>
  )
}
