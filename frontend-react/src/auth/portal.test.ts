import { describe, expect, it } from 'vitest'
import { inicioDaSessao, rotaBloqueada } from './portal'

const egresso = { 'egresso-inicio': '/egresso/inicio', contato: '/contato' }
const aluno = { inicio: '/inicio', formativas: '/formativas', eventos: '/eventos' }

describe('portal do egresso', () => {
  it('egresso não entra nas rotas exclusivas de aluno', () => {
    expect(rotaBloqueada('/inicio', egresso)).toBe(true)
    expect(rotaBloqueada('/solicitacoes/nova', egresso)).toBe(true)
    expect(rotaBloqueada('/formativas', egresso)).toBe(true)
    expect(rotaBloqueada('/formativas/1', egresso)).toBe(true)
    expect(rotaBloqueada('/estagios', egresso)).toBe(true)
    expect(rotaBloqueada('/estagios/1', egresso)).toBe(true)
    expect(rotaBloqueada('/tccs', egresso)).toBe(true)
    expect(rotaBloqueada('/tccs/1', egresso)).toBe(true)
    expect(rotaBloqueada('/eventos', egresso)).toBe(true)
    expect(rotaBloqueada('/eventos/1/presenca', egresso)).toBe(true)
  })

  it('egresso entra no próprio início e o aluno não entra no portal', () => {
    expect(rotaBloqueada('/egresso/inicio', egresso)).toBe(false)
    expect(rotaBloqueada('/contato', egresso)).toBe(false)
    expect(rotaBloqueada('/certificados', egresso)).toBe(false)
    expect(rotaBloqueada('/primeiro-acesso', egresso)).toBe(false)
    expect(inicioDaSessao(egresso)).toBe('/egresso/inicio')

    expect(rotaBloqueada('/egresso/inicio', aluno)).toBe(true)
    expect(rotaBloqueada('/inicio', aluno)).toBe(false)
    expect(rotaBloqueada('/formativas', aluno)).toBe(false)
    expect(rotaBloqueada('/professor/eventos', aluno)).toBe(false)
    expect(inicioDaSessao(aluno)).toBe('/inicio')
  })
})
