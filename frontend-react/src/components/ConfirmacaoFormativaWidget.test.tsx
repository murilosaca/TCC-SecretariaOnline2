import { render, screen } from '@testing-library/react'
import { describe, expect, it } from 'vitest'
import { ConfirmacaoFormativaWidget } from './ConfirmacaoFormativaWidget'

describe('ConfirmacaoFormativaWidget', () => {
  it('não mostra botão Confirmar quando o _link está ausente', () => {
    render(
      <ConfirmacaoFormativaWidget
        links={{ self: '/formativas/1' }}
        onConfirm={() => undefined}
        onCancel={() => undefined}
      />,
    )
    expect(screen.queryByRole('button', { name: 'Confirmar' })).toBeNull()
    expect(screen.queryByRole('button', { name: 'Cancelar' })).toBeNull()
  })

  it('mostra Confirmar somente com _links.confirmar', () => {
    render(
      <ConfirmacaoFormativaWidget
        links={{ confirmar: '/formativas/1/confirmar' }}
        onConfirm={() => undefined}
        onCancel={() => undefined}
      />,
    )
    expect(screen.getByRole('button', { name: 'Confirmar' })).toBeTruthy()
    expect(screen.queryByRole('button', { name: 'Cancelar' })).toBeNull()
  })
})
