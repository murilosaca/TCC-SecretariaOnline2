export function resolveApiBaseUrl(envUrl: string | undefined, platform: string): string {
  const fromEnv = envUrl?.trim();
  if (fromEnv) {
    return fromEnv.replace(/\/$/, '');
  }
  if (platform === 'android') {
    return 'http://10.0.2.2:8080';
  }
  return 'http://localhost:8080';
}
