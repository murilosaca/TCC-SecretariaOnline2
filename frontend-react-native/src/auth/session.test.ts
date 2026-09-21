import { beforeEach, describe, expect, it } from 'vitest';
import { authSession } from './session';
import { obterDeviceUuid, refreshStore, setTokenStore } from './tokenStore';

describe('sessão nativa', () => {
  const memory = new Map<string, string>();

  beforeEach(() => {
    memory.clear();
    authSession.setAccessToken(null);
    setTokenStore({
      getItem: async (key) => memory.get(key) ?? null,
      setItem: async (key, value) => {
        memory.set(key, value);
      },
      removeItem: async (key) => {
        memory.delete(key);
      },
    });
  });

  it('access token fica só em memória', () => {
    authSession.setAccessToken('jwt-de-teste');
    expect(authSession.getAccessToken()).toBe('jwt-de-teste');
    expect(memory.size).toBe(0);
  });

  it('refresh vai para o store injetado, não para um mapa em claro implícito', async () => {
    await refreshStore.set('refresh-opaco');
    expect(await refreshStore.get()).toBe('refresh-opaco');
    await refreshStore.clear();
    expect(await refreshStore.get()).toBeNull();
  });

  it('deviceUuid é estável entre chamadas', async () => {
    const primeiro = await obterDeviceUuid();
    const segundo = await obterDeviceUuid();
    expect(primeiro).toBe(segundo);
    expect(primeiro.length).toBeGreaterThanOrEqual(8);
  });
});
