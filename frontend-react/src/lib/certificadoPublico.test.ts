import { describe, expect, it } from 'vitest'
import { chaveEd25519, hashValido, hexParaBytes, normalizarHash } from './certificadoPublico'

describe('certificadoPublico', () => {
  it('normaliza e valida hash SHA-256', () => {
    const hash = `${'A'.repeat(64)}`
    expect(normalizarHash(` ${hash} `)).toBe('a'.repeat(64))
    expect(hashValido(hash)).toBe(true)
    expect(hashValido('demo')).toBe(false)
  })

  it('converte hex para bytes', () => {
    expect(Array.from(hexParaBytes('00ff'))).toEqual([0, 255])
  })

  it('escolhe a chave OKP Ed25519 do JWKS', () => {
    expect(
      chaveEd25519({
        keys: [
          { kty: 'RSA', x: 'nao' },
          { kty: 'OKP', crv: 'Ed25519', x: 'abc' },
        ],
      })?.x,
    ).toBe('abc')
    expect(chaveEd25519({ keys: [] })).toBeNull()
  })
})
