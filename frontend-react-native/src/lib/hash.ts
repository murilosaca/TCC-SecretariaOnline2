export function truncarHash(hash: string | null | undefined): string {
  if (!hash) {
    return '—';
  }
  if (hash.length <= 16) {
    return hash;
  }
  return `${hash.slice(0, 8)}…${hash.slice(-4)}`;
}
