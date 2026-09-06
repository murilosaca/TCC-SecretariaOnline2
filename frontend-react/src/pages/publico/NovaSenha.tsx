import { FormEvent, useEffect, useState } from 'react'
import { Link, useNavigate, useSearchParams } from 'react-router-dom'
import { authApi } from '../../api/auth'
import { ApiError } from '../../api/client'
import { isStrongPassword, passwordRules, passwordScore } from '../../auth/password'

export function NovaSenha() {
  const [params] = useSearchParams()
  const navigate = useNavigate()
  const token = params.get('token') ?? ''
  const [estado, setEstado] = useState<'checando' | 'ok' | 'invalido'>('checando')
  const [novaSenha, setNovaSenha] = useState('')
  const [confirmacao, setConfirmacao] = useState('')
  const [erroConfirmacao, setErroConfirmacao] = useState<string | null>(null)
  const [aviso, setAviso] = useState<string | null>(null)
  const [enviando, setEnviando] = useState(false)

  useEffect(() => {
    if (!token) {
      setEstado('invalido')
      return
    }
    authApi
      .validarTokenRedefinicao(token)
      .then(() => setEstado('ok'))
      .catch(() => setEstado('invalido'))
  }, [token])

  async function onSubmit(event: FormEvent) {
    event.preventDefault()
    setAviso(null)
    if (novaSenha !== confirmacao) {
      setErroConfirmacao('As senhas não coincidem')
      return
    }
    if (!isStrongPassword(novaSenha)) {
      setAviso('A senha não atende aos requisitos de segurança.')
      return
    }
    setErroConfirmacao(null)
    setEnviando(true)
    try {
      await authApi.redefinirSenha(token, novaSenha)
      navigate('/login?redefinida=1', { replace: true })
    } catch (error) {
      if (error instanceof ApiError && error.status === 422) {
        setAviso(error.message)
        setNovaSenha('')
        setConfirmacao('')
      } else if (error instanceof ApiError && error.status === 401) {
        setEstado('invalido')
      } else {
        setAviso('Erro ao processar a solicitação. Tente novamente.')
      }
    } finally {
      setEnviando(false)
    }
  }

  if (estado === 'checando') {
    return (
      <section className="page">
        <p className="muted">Validando o link…</p>
      </section>
    )
  }

  if (estado === 'invalido') {
    return (
      <section className="page">
        <h1>Link inválido ou expirado</h1>
        <p className="lead">Este link não é mais válido. Solicite um novo para redefinir sua senha.</p>
        <Link to="/recuperar-senha">
          <button type="button">Solicitar novo link</button>
        </Link>
      </section>
    )
  }

  const score = passwordScore(novaSenha)

  return (
    <section className="page">
      <h1>Definir nova senha</h1>
      {aviso && (
        <div className="banner danger" role="alert" aria-live="assertive">
          {aviso}
        </div>
      )}
      <form className="panel auth-form" onSubmit={onSubmit} noValidate>
        <PasswordFields
          novaSenha={novaSenha}
          confirmacao={confirmacao}
          score={score}
          erroConfirmacao={erroConfirmacao}
          onNovaSenha={setNovaSenha}
          onConfirmacao={setConfirmacao}
        />
        <button type="submit" disabled={enviando}>
          {enviando ? 'Salvando...' : 'Salvar senha'}
        </button>
      </form>
    </section>
  )
}

export function PasswordFields({
  novaSenha,
  confirmacao,
  score,
  erroConfirmacao,
  onNovaSenha,
  onConfirmacao,
}: {
  novaSenha: string
  confirmacao: string
  score: number
  erroConfirmacao: string | null
  onNovaSenha: (value: string) => void
  onConfirmacao: (value: string) => void
}) {
  return (
    <>
      <label>
        Nova senha
        <input
          type="password"
          autoComplete="new-password"
          value={novaSenha}
          onChange={(event) => onNovaSenha(event.target.value)}
          aria-describedby="requisitos-senha"
        />
      </label>
      <div className="password-meter" aria-hidden="true">
        {[1, 2, 3, 4].map((nivel) => (
          <span key={nivel} className={`meter-seg ${score >= nivel ? `lv${score}` : ''}`} />
        ))}
      </div>
      <ul id="requisitos-senha" className="req-list">
        {passwordRules.map((rule) => (
          <li key={rule.id} className={rule.test(novaSenha) ? 'ok' : undefined}>
            {rule.test(novaSenha) ? '✓' : '○'} {rule.label}
          </li>
        ))}
      </ul>
      <label>
        Confirmar senha
        <input
          type="password"
          autoComplete="new-password"
          value={confirmacao}
          onChange={(event) => onConfirmacao(event.target.value)}
          className={erroConfirmacao ? 'invalid' : undefined}
          aria-invalid={Boolean(erroConfirmacao)}
        />
        {erroConfirmacao && <span className="field-error">{erroConfirmacao}</span>}
      </label>
    </>
  )
}
