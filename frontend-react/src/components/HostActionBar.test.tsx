import { render, screen } from '@testing-library/react'
import { describe, expect, it } from 'vitest'
import { HostActionBar } from './HostActionBar'

describe('HostActionBar', () => {
  it('não mostra abrir janela quando o _link está ausente', () => {
    render(<HostActionBar links={{ self: '/events/1' }} />)
    expect(screen.queryByRole('button', { name: 'Abrir janela de entrada' })).toBeNull()
    expect(screen.queryByRole('button', { name: 'Abrir janela de saída' })).toBeNull()
  })

  it('mostra o botão somente com _links.abrir-janela-entrada', () => {
    render(
      <HostActionBar
        links={{ 'abrir-janela-entrada': '/events/1/attendance/windows/entry' }}
      />,
    )
    expect(screen.getByRole('button', { name: 'Abrir janela de entrada' })).toBeTruthy()
  })

  it('mostra saída e renovar QR só pelos _links', () => {
    render(
      <HostActionBar
        links={{
          'abrir-janela-saida': '/events/1/attendance/windows/exit',
          'renovar-qr': '/events/1/attendance/qr/renew',
        }}
      />,
    )
    expect(screen.getByRole('button', { name: 'Abrir janela de saída' })).toBeTruthy()
    expect(screen.getByRole('button', { name: 'Renovar QR' })).toBeTruthy()
    expect(screen.queryByRole('button', { name: 'Abrir janela de entrada' })).toBeNull()
  })
})
