import { describe, expect, it } from 'vitest'
import { normalizarProtocolo, protocoloValido, truncarHash } from './protocoloPublico'

describe('protocoloPublico', () => {
  it('normaliza e valida PROT-AAAA-NNNNN', () => {
    expect(normalizarProtocolo(' prot-2026-00001 ')).toBe('PROT-2026-00001')
    expect(protocoloValido('PROT-2026-00001')).toBe(true)
    expect(protocoloValido('demo')).toBe(false)
  })

  it('trunca hash longo e preserva curto', () => {
    expect(truncarHash('abcd1234efgh5678ijkl')).toBe('abcd1234…ijkl')
    expect(truncarHash('curto')).toBe('curto')
    expect(truncarHash(null)).toBe('—')
  })
})
