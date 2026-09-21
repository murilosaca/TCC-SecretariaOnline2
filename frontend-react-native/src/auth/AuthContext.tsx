import { createContext, useCallback, useContext, useEffect, useMemo, useState, type ReactNode } from 'react';
import { authApi, type LoginResponse } from '../api/auth';
import { setOnUnauthorized } from '../api/client';
import type { HateoasLinks } from '../models/hateoas';
import { authSession } from './session';
import { refreshStore } from './tokenStore';

type Status = 'loading' | 'anonymous' | 'authenticated';

type AuthContextValue = {
  status: Status;
  mustChangePassword: boolean;
  links: HateoasLinks;
  login: (identificador: string, senha: string) => Promise<LoginResponse>;
  logout: () => Promise<void>;
  completeFirstAccess: (novaSenha: string) => Promise<void>;
};

const AuthContext = createContext<AuthContextValue | null>(null);

export function AuthProvider({ children }: { children: ReactNode }) {
  const [status, setStatus] = useState<Status>('loading');
  const [mustChangePassword, setMustChangePassword] = useState(false);
  const [links, setLinks] = useState<HateoasLinks>({});

  const clearLocal = useCallback(async () => {
    authSession.setAccessToken(null);
    await refreshStore.clear();
    setLinks({});
    setMustChangePassword(false);
    setStatus('anonymous');
  }, []);

  const applySession = useCallback(async (accessToken: string, mustChange: boolean, refreshToken?: string) => {
    authSession.setAccessToken(accessToken);
    if (refreshToken) {
      await refreshStore.set(refreshToken);
    }
    setMustChangePassword(mustChange);
    try {
      const me = await authApi.me();
      setMustChangePassword(me.mustChangePassword);
      setLinks(me._links ?? {});
    } catch {
      setLinks({});
    }
    setStatus('authenticated');
  }, []);

  useEffect(() => {
    setOnUnauthorized(() => {
      authSession.setAccessToken(null);
      void refreshStore.clear();
      setLinks({});
      setMustChangePassword(false);
      setStatus('anonymous');
    });
    return () => setOnUnauthorized(null);
  }, []);

  useEffect(() => {
    let cancelled = false;
    refreshStore
      .get()
      .then(async (stored) => {
        if (!stored) {
          if (!cancelled) {
            setStatus('anonymous');
          }
          return;
        }
        try {
          const result = await authApi.refresh(stored);
          if (cancelled) {
            return;
          }
          await applySession(result.accessToken, result.mustChangePassword, result.refreshToken);
        } catch {
          await refreshStore.clear();
          if (!cancelled) {
            authSession.setAccessToken(null);
            setStatus('anonymous');
          }
        }
      })
      .catch(() => {
        if (!cancelled) {
          setStatus('anonymous');
        }
      });
    return () => {
      cancelled = true;
    };
  }, [applySession]);

  const login = useCallback(
    async (identificador: string, senha: string) => {
      const result = await authApi.login(identificador, senha);
      await applySession(result.accessToken, result.mustChangePassword, result.refreshToken);
      return result;
    },
    [applySession],
  );

  const logout = useCallback(async () => {
    const stored = await refreshStore.get();
    try {
      await authApi.logout(stored);
    } finally {
      await clearLocal();
    }
  }, [clearLocal]);

  const completeFirstAccess = useCallback(async (novaSenha: string) => {
    await authApi.primeiroAcesso(novaSenha, true);
    setMustChangePassword(false);
  }, []);

  const value = useMemo(
    () => ({ status, mustChangePassword, links, login, logout, completeFirstAccess }),
    [status, mustChangePassword, links, login, logout, completeFirstAccess],
  );

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
}

export function useAuth(): AuthContextValue {
  const context = useContext(AuthContext);
  if (!context) {
    throw new Error('useAuth deve ser usado dentro de AuthProvider');
  }
  return context;
}
