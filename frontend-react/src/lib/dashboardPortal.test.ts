import { describe, expect, it } from 'vitest'
import { painelDe } from './dashboardPortal'

describe('dashboardPortal', () => {
  it('escolhe o painel pelo rel painel, sem ler authorities', () => {
    expect(painelDe({ painel: '/bff/dashboard/aluno' })).toBe('aluno')
    expect(painelDe({ painel: '/bff/dashboard/professor' })).toBe('professor')
    expect(painelDe({ painel: '/bff/dashboard/secretary' })).toBe('secretaria')
    expect(painelDe({ inicio: '/inicio' })).toBeNull()
    expect(painelDe(null)).toBeNull()
  })
})
