import { FormEvent, useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { ApiError } from '../../api/client'
import { useAuth } from '../../auth/AuthContext'
import { isStrongPassword, passwordScore } from '../../auth/password'
import { PasswordFields } from '../publico/NovaSenha'

export function PrimeiroAcesso() {
  const { completeFirstAccess } = useAuth()
  const navigate = useNavigate()
  const [novaSenha, setNovaSenha] = useState('')
  const [confirmacao, setConfirmacao] = useState('')
  const [lgpd, setLgpd] = useState(false)
  const [erroConfirmacao, setErroConfirmacao] = useState<string | null>(null)
  const [aviso, setAviso] = useState<string | null>(null)
  const [enviando, setEnviando] = useState(false)
  const score = passwordScore(novaSenha)
  const podeContinuar = isStrongPassword(novaSenha) && novaSenha === confirmacao && lgpd

  async function onSubmit(event: FormEvent) {
    event.preventDefault()
    setAviso(null)
    if (novaSenha !== confirmacao) {
      setErroConfirmacao('As senhas não coincidem')
      return
    }
    if (!podeContinuar) {
      return
    }
    setErroConfirmacao(null)
    setEnviando(true)
    try {
      await completeFirstAccess(novaSenha)
      navigate('/inicio', { replace: true })
    } catch (error) {
      if (error instanceof ApiError) {
        setAviso(error.message)
      } else {
        setAviso('Não foi possível concluir o primeiro acesso.')
      }
    } finally {
      setEnviando(false)
    }
  }

  return (
    <section className="page first-access">
      <h1>Primeiro acesso</h1>
      <p className="lead">
        Defina uma senha pessoal forte e aceite a política de privacidade (LGPD) para desbloquear o sistema
        (RF-F1-002).
      </p>
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
        <label className="checkbox-row">
          <input
            type="checkbox"
            checked={lgpd}
            onChange={(event) => setLgpd(event.target.checked)}
          />
          <span>
            Li e aceito a{' '}
            <a href="https://www.ufpr.br/lgpd/" target="_blank" rel="noreferrer">
              política de privacidade (LGPD)
            </a>
          </span>
        </label>
        <button type="submit" disabled={!podeContinuar || enviando}>
          {enviando ? 'Salvando...' : 'Continuar'}
        </button>
      </form>
    </section>
  )
}
