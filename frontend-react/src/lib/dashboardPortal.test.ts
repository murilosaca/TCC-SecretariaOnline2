import { describe, expect, it } from 'vitest'
import { ePainelAluno, ePainelProfessor } from './dashboardPortal'

describe('dashboardPortal', () => {
  it('aluno com request.view_own usa painel do aluno', () => {
    const caps = ['dashboard.view_own', 'request.view_own', 'request.open']
    expect(ePainelAluno(caps)).toBe(true)
    expect(ePainelProfessor(caps)).toBe(false)
  })

  it('professor puro usa painel do professor', () => {
    const caps = ['dashboard.view_self_professor', 'event.manage', 'request.deliberate']
    expect(ePainelAluno(caps)).toBe(false)
    expect(ePainelProfessor(caps)).toBe(true)
  })

  it('secretaria sem cap de professor não entra no painel', () => {
    const caps = ['course.manage', 'request.deliberate']
    expect(ePainelAluno(caps)).toBe(false)
    expect(ePainelProfessor(caps)).toBe(false)
  })
})
