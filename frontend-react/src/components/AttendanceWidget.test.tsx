import { render, screen } from '@testing-library/react'
import { describe, expect, it } from 'vitest'
import { AttendanceWidget } from './AttendanceWidget'

describe('AttendanceWidget', () => {
  it('não mostra botão de confirmar quando o _link está ausente', () => {
    render(
      <AttendanceWidget
        links={{ self: '/events/1/attendance/session' }}
        janelaExpira={new Date(Date.now() + 30 * 60_000).toISOString()}
        faseDisponivel="ENTRADA"
        onConfirm={() => undefined}
      />,
    )
    expect(screen.queryByRole('button', { name: 'Confirmar' })).toBeNull()
    expect(screen.queryByLabelText('PIN de presença')).toBeNull()
  })

  it('mostra confirmar somente com _links.confirmar-entrada', () => {
    render(
      <AttendanceWidget
        links={{ 'confirmar-entrada': '/events/1/attendance/confirm' }}
        janelaExpira={new Date(Date.now() + 30 * 60_000).toISOString()}
        faseDisponivel="ENTRADA"
        onConfirm={() => undefined}
      />,
    )
    expect(screen.getByRole('button', { name: 'Confirmar' })).toBeTruthy()
  })
})
