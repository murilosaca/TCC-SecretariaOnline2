import * as SecureStore from 'expo-secure-store';
import { setTokenStore } from './tokenStore';

/** Keychain (iOS) / Keystore (Android). Nunca AsyncStorage em claro. */
export function bindSecureStore(): void {
  setTokenStore({
    getItem: (key) => SecureStore.getItemAsync(key),
    setItem: (key, value) => SecureStore.setItemAsync(key, value),
    removeItem: (key) => SecureStore.deleteItemAsync(key),
  });
}
