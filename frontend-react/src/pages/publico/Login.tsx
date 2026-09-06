import { FormEvent, useState } from 'react'
import { Link } from 'react-router-dom'

export function Login() {
  const [aviso, setAviso] = useState<string | null>(null)

  function onSubmit(event: FormEvent) {
    event.preventDefault()
    setAviso('O login (RF-F0-001) ainda não tem IAM/JWT nesta fundação. Identificador aceito no futuro: e-mail institucional, e-mail pessoal ou GRR.')
  }

  return (
    <section className="page">
      <h1>Entrar</h1>
      <p className="lead">Acesso ao Secretaria Online 2 (RF-F0-001).</p>
      {aviso && (
        <div className="banner danger" role="status">
          {aviso}
        </div>
      )}
      <form className="panel" onSubmit={onSubmit}>
        <label>
          E-mail ou GRR
          <input name="identificador" autoComplete="username" required placeholder="GRR20241234 ou e-mail" />
        </label>
        <label>
          Senha
          <input name="senha" type="password" autoComplete="current-password" required />
        </label>
        <button type="submit">Entrar</button>
        <p>
          <Link to="/recuperar-senha">Esqueci minha senha</Link>
        </p>
      </form>
    </section>
  )
}
