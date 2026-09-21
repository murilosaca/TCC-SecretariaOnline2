import { describe, expect, it } from 'vitest';
import { resolveApiBaseUrl } from './apiUrl';

describe('resolveApiBaseUrl', () => {
  it('usa EXPO_PUBLIC_API_URL quando definida', () => {
    expect(resolveApiBaseUrl('http://192.168.0.10:8080/', 'android')).toBe('http://192.168.0.10:8080');
  });

  it('emulador Android cai em 10.0.2.2', () => {
    expect(resolveApiBaseUrl(undefined, 'android')).toBe('http://10.0.2.2:8080');
  });

  it('iOS cai em localhost', () => {
    expect(resolveApiBaseUrl('', 'ios')).toBe('http://localhost:8080');
  });
});
