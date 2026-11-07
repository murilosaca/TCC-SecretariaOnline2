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
    expect(screen.getByText(/Nenhuma ação disponível/)).toBeTruthy()
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

  it('trata Ver comprovante como ação e não mostra empty state', () => {
    render(
      <ConfirmacaoFormativaWidget
        links={{ comprovante: '/formativas/1/comprovante' }}
        onConfirm={() => undefined}
        onCancel={() => undefined}
        onComprovante={() => undefined}
      />,
    )
    expect(screen.getByRole('button', { name: 'Ver comprovante' })).toBeTruthy()
    expect(screen.queryByText(/Nenhuma ação disponível/)).toBeNull()
  })
})
