import { describe, expect, it } from 'vitest'
import { isStrongPassword, isValidEmail, passwordScore } from './password'

describe('política de senha (F0.3 / F1.2)', () => {
  it('exige 12 caracteres e complexidade', () => {
    expect(isStrongPassword('fraca')).toBe(false)
    expect(isStrongPassword('TroqueEstaSenha1!')).toBe(true)
    expect(passwordScore('TroqueEstaSenha1!')).toBe(4)
    expect(passwordScore('abc')).toBe(1)
  })

  it('valida e-mail de recuperação', () => {
    expect(isValidEmail('aluno.dev@ufpr.br')).toBe(true)
    expect(isValidEmail('sem-dominio')).toBe(false)
  })
})
