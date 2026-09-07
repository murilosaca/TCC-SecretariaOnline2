import { describe, expect, it } from 'vitest'
import { blocoKpi, blocoLista } from './dashboardBlocks'

describe('dashboardBlocks', () => {
  it('trata null como degradação e [] como vazio', () => {
    expect(blocoLista(null)).toBe('error')
    expect(blocoLista([])).toBe('empty')
    expect(blocoLista([{ id: '1' }])).toBe('ok')
    expect(blocoKpi(null)).toBe('error')
    expect(blocoKpi(0)).toBe('ok')
  })
})
