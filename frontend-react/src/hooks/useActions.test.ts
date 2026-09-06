import { describe, expect, it } from 'vitest'
import { useActions } from './useActions'

describe('useActions', () => {
  it('libera ação somente quando o _link existe', () => {
    const actions = useActions({ atualizar: '/academico/cursos/1' })
    expect(actions.can('atualizar')).toBe(true)
    expect(actions.can('excluir')).toBe(false)
  })
})
