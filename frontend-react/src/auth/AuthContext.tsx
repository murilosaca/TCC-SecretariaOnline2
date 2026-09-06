import { createContext, useCallback, useContext, useEffect, useMemo, useState, type ReactNode } from 'react'
import { authApi, type LoginResponse } from '../api/auth'
import { authSession } from './session'

type Status = 'loading' | 'anonymous' | 'authenticated'

type AuthContextValue = {
  status: Status
  mustChangePassword: boolean
  authorities: string[]
  login: (identificador: string, senha: string) => Promise<LoginResponse>
  logout: () => Promise<void>
  completeFirstAccess: (novaSenha: string) => Promise<void>
}

const AuthContext = createContext<AuthContextValue | null>(null)

export function AuthProvider({ children }: { children: ReactNode }) {
  const [status, setStatus] = useState<Status>('loading')
  const [mustChangePassword, setMustChangePassword] = useState(false)
  const [authorities, setAuthorities] = useState<string[]>([])

  const applySession = useCallback(async (accessToken: string, mustChange: boolean) => {
    authSession.setAccessToken(accessToken)
    setMustChangePassword(mustChange)
    try {
      const me = await authApi.me()
      setMustChangePassword(me.mustChangePassword)
      setAuthorities(me.authorities)
    } catch {
      setAuthorities([])
    }
    setStatus('authenticated')
  }, [])

  useEffect(() => {
    let cancelled = false
    authApi
      .refresh()
      .then(async (result) => {
        if (cancelled) {
          return
        }
        await applySession(result.accessToken, result.mustChangePassword)
      })
      .catch(() => {
        if (cancelled) {
          return
        }
        authSession.setAccessToken(null)
        setStatus('anonymous')
      })
    return () => {
      cancelled = true
    }
  }, [applySession])

  const login = useCallback(
    async (identificador: string, senha: string) => {
      const result = await authApi.login(identificador, senha)
      await applySession(result.accessToken, result.mustChangePassword)
      return result
    },
    [applySession],
  )

  const logout = useCallback(async () => {
    try {
      await authApi.logout()
    } finally {
      authSession.setAccessToken(null)
      setAuthorities([])
      setMustChangePassword(false)
      setStatus('anonymous')
    }
  }, [])

  const completeFirstAccess = useCallback(async (novaSenha: string) => {
    await authApi.primeiroAcesso(novaSenha, true)
    setMustChangePassword(false)
  }, [])

  const value = useMemo(
    () => ({ status, mustChangePassword, authorities, login, logout, completeFirstAccess }),
    [status, mustChangePassword, authorities, login, logout, completeFirstAccess],
  )

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>
}

export function useAuth(): AuthContextValue {
  const context = useContext(AuthContext)
  if (!context) {
    throw new Error('useAuth deve ser usado dentro de AuthProvider')
  }
  return context
}
