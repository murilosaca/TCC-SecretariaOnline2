const FORMATO = /^PROT-\d{4}-\d{5}$/

export function normalizarProtocolo(raw: string): string {
  return raw.trim().toUpperCase()
}

export function protocoloValido(raw: string): boolean {
  return FORMATO.test(normalizarProtocolo(raw))
}

export function truncarHash(hash: string | null | undefined): string {
  if (!hash) {
    return '—'
  }
  if (hash.length <= 16) {
    return hash
  }
  return `${hash.slice(0, 8)}…${hash.slice(-4)}`
}
