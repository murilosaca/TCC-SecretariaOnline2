import { FormEvent, useState } from 'react'
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { ApiError } from '../../api/client'
import { perfilApi } from '../../api/perfil'
import { isStrongPassword, passwordRules, passwordScore } from '../../auth/password'
import { useActions } from '../../hooks/useActions'
import type { SessaoDispositivo } from '../../models/perfil'
import { PerfilNav } from './PerfilNav'

export function Seguranca() {
  const queryClient = useQueryClient()
  const sessoes = useQuery({ queryKey: ['perfil', 'sessoes'], queryFn: perfilApi.sessoes })
  const [senhaAtual, setSenhaAtual] = useState('')
  const [novaSenha, setNovaSenha] = useState('')
  const [confirmacao, setConfirmacao] = useState('')
  const [erroConfirmacao, setErroConfirmacao] = useState<string | null>(null)
  const [erroSenha, setErroSenha] = useState<string | null>(null)
  const [banner, setBanner] = useState<string | null>(null)
  const score = passwordScore(novaSenha)
  const podeSalvar = senhaAtual.length > 0 && isStrongPassword(novaSenha) && novaSenha === confirmacao

  const trocar = useMutation({
    mutationFn: () => perfilApi.trocarSenha({ senhaAtual, novaSenha }),
    onSuccess: async () => {
      setBanner('Senha alterada. Outras sessões foram encerradas.')
      setErroSenha(null)
      setSenhaAtual('')
      setNovaSenha('')
      setConfirmacao('')
      await queryClient.invalidateQueries({ queryKey: ['perfil', 'sessoes'] })
    },
    onError: (error) => {
      setBanner(null)
      if (error instanceof ApiError && error.status === 401) {
        setErroSenha('Senha atual incorreta.')
        return
      }
      setErroSenha(error instanceof ApiError ? error.message : 'Não foi possível alterar a senha.')
    },
  })

  const encerrar = useMutation({
    mutationFn: (href: string) => perfilApi.encerrarSessao(href),
    onSuccess: async () => {
      await queryClient.invalidateQueries({ queryKey: ['perfil', 'sessoes'] })
    },
  })

  function onSubmit(event: FormEvent) {
    event.preventDefault()
    setErroConfirmacao(null)
    if (novaSenha !== confirmacao) {
      setErroConfirmacao('As senhas não coincidem')
      return
    }
    if (!podeSalvar || trocar.isPending) {
      return
    }
    trocar.mutate()
  }

  return (
    <section className="page">
      <h1>Segurança</h1>
      <PerfilNav />
      <div className="settings-stack">
        <form className="panel auth-form" onSubmit={onSubmit} noValidate>
          <h2>Trocar senha</h2>
          {banner && (
            <div className="banner success" role="status">
              {banner}
            </div>
          )}
          <label>
            Senha atual
            <input
              type="password"
              autoComplete="current-password"
              value={senhaAtual}
              onChange={(event) => setSenhaAtual(event.target.value)}
              aria-invalid={Boolean(erroSenha)}
              aria-describedby={erroSenha ? 'erro-senha-atual' : undefined}
            />
            {erroSenha && (
              <span id="erro-senha-atual" className="field-error">
                {erroSenha}
              </span>
            )}
          </label>
          <label>
            Nova senha
            <input
              type="password"
              autoComplete="new-password"
              value={novaSenha}
              onChange={(event) => setNovaSenha(event.target.value)}
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
            Confirmar nova senha
            <input
              type="password"
              autoComplete="new-password"
              value={confirmacao}
              onChange={(event) => setConfirmacao(event.target.value)}
              className={erroConfirmacao ? 'invalid' : undefined}
              aria-invalid={Boolean(erroConfirmacao)}
            />
            {erroConfirmacao && <span className="field-error">{erroConfirmacao}</span>}
          </label>
          <button type="submit" disabled={!podeSalvar || trocar.isPending}>
            {trocar.isPending ? 'Salvando…' : 'Salvar senha'}
          </button>
        </form>
        <section className="panel">
          <h2>Sessões ativas</h2>
          {sessoes.isLoading && <p className="muted">Carregando sessões…</p>}
          {sessoes.isError && (
            <div className="banner danger" role="alert">
              {sessoes.error instanceof ApiError ? sessoes.error.message : 'Não foi possível carregar as sessões.'}
            </div>
          )}
          {sessoes.data && sessoes.data.itens.length === 0 && (
            <p className="empty">Nenhuma sessão ativa.</p>
          )}
          {sessoes.data && sessoes.data.itens.length > 0 && (
            <table>
              <thead>
                <tr>
                  <th>Dispositivo</th>
                  <th>Situação</th>
                  <th>Ações</th>
                </tr>
              </thead>
              <tbody>
                {sessoes.data.itens.map((item) => (
                  <SessaoLinha key={item.id} item={item} onEncerrar={(href) => encerrar.mutate(href)} />
                ))}
              </tbody>
            </table>
          )}
        </section>
      </div>
    </section>
  )
}

function SessaoLinha({ item, onEncerrar }: { item: SessaoDispositivo; onEncerrar: (href: string) => void }) {
  const actions = useActions(item._links)
  const href = actions.href('encerrar')
  return (
    <tr>
      <td>{item.dispositivo}</td>
      <td>{item.atual ? 'Este dispositivo' : 'Outro dispositivo'}</td>
      <td>
        {actions.can('encerrar') && href && (
          <button type="button" className="danger" onClick={() => onEncerrar(href)}>
            Encerrar
          </button>
        )}
      </td>
    </tr>
  )
}
