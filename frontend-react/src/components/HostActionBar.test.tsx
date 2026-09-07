import { render, screen } from '@testing-library/react'
import { describe, expect, it } from 'vitest'
import { HostActionBar } from './HostActionBar'

describe('HostActionBar', () => {
  it('não mostra abrir janela quando o _link está ausente', () => {
    render(<HostActionBar links={{ self: '/events/1' }} />)
    expect(screen.queryByRole('button', { name: 'Abrir janela de entrada' })).toBeNull()
  })

  it('mostra o botão somente com _links.abrir-janela-entrada', () => {
    render(
      <HostActionBar
        links={{ 'abrir-janela-entrada': '/events/1/attendance/windows/entry' }}
      />,
    )
    expect(screen.getByRole('button', { name: 'Abrir janela de entrada' })).toBeTruthy()
  })
})
