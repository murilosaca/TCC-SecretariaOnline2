import { describe, expect, it } from 'vitest'
import { rotuloCodigo } from './rotulos'

describe('rotuloCodigo', () => {
  it('traduz estados conhecidos sem underscore', () => {
    expect(rotuloCodigo('EM_AJUSTE')).toBe('Em ajuste')
    expect(rotuloCodigo('EM_ANDAMENTO')).toBe('Em andamento')
    expect(rotuloCodigo('EM_ANALISE')).toBe('Em análise')
  })

  it('substitui underscore em códigos desconhecidos', () => {
    expect(rotuloCodigo('FOO_BAR_BAZ')).toBe('FOO BAR BAZ')
  })

  it('trata vazio', () => {
    expect(rotuloCodigo(null)).toBe('—')
    expect(rotuloCodigo(undefined)).toBe('—')
  })
})
