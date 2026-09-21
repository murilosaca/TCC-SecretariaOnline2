export type TokenStore = {
  getItem(key: string): Promise<string | null>;
  setItem(key: string, value: string): Promise<void>;
  removeItem(key: string): Promise<void>;
};

const REFRESH_KEY = 'so2_refresh';
const DEVICE_KEY = 'so2_device_uuid';

const memory = new Map<string, string>();

let store: TokenStore = {
  async getItem(key) {
    return memory.get(key) ?? null;
  },
  async setItem(key, value) {
    memory.set(key, value);
  },
  async removeItem(key) {
    memory.delete(key);
  },
};

export function setTokenStore(next: TokenStore): void {
  store = next;
}

export const refreshStore = {
  get(): Promise<string | null> {
    return store.getItem(REFRESH_KEY);
  },
  set(value: string): Promise<void> {
    return store.setItem(REFRESH_KEY, value);
  },
  clear(): Promise<void> {
    return store.removeItem(REFRESH_KEY);
  },
};

export async function obterDeviceUuid(): Promise<string> {
  const existente = await store.getItem(DEVICE_KEY);
  if (existente && existente.length >= 8) {
    return existente;
  }
  const gerado = crypto.randomUUID();
  await store.setItem(DEVICE_KEY, gerado);
  return gerado;
}
