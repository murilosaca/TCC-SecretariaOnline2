export const HASH_SHA256 = /^[0-9a-f]{64}$/i

export function normalizarHash(raw: string): string {
  return raw.trim().toLowerCase()
}

export function hashValido(raw: string): boolean {
  return HASH_SHA256.test(normalizarHash(raw))
}

export function hexParaBytes(hex: string): Uint8Array {
  const limpo = normalizarHash(hex)
  const out = new Uint8Array(limpo.length / 2)
  for (let i = 0; i < out.length; i += 1) {
    out[i] = Number.parseInt(limpo.slice(i * 2, i * 2 + 2), 16)
  }
  return out
}

export function base64ParaBytes(valor: string): Uint8Array {
  const bin = atob(valor)
  const out = new Uint8Array(bin.length)
  for (let i = 0; i < bin.length; i += 1) {
    out[i] = bin.charCodeAt(i)
  }
  return out
}

export type JwkOkp = {
  kty: string
  crv?: string
  x?: string
  kid?: string
}

export function chaveEd25519(jwks: { keys?: JwkOkp[] } | null | undefined): JwkOkp | null {
  const keys = jwks?.keys ?? []
  return keys.find((key) => key.kty === 'OKP' && key.crv === 'Ed25519' && key.x) ?? null
}

export async function verificarAssinaturaEd25519(
  hashSha256: string,
  assinaturaB64: string,
  jwk: JwkOkp,
): Promise<boolean> {
  if (!globalThis.crypto?.subtle) {
    throw new Error('SubtleCrypto indisponível neste browser.')
  }
  const key = await crypto.subtle.importKey(
    'jwk',
    {
      kty: 'OKP',
      crv: 'Ed25519',
      x: jwk.x,
      ext: true,
    },
    { name: 'Ed25519' },
    false,
    ['verify'],
  )
  return crypto.subtle.verify(
    { name: 'Ed25519' },
    key,
    base64ParaBytes(assinaturaB64),
    hexParaBytes(hashSha256),
  )
}
