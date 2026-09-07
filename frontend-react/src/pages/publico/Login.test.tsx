import { fireEvent, render, screen, waitFor } from '@testing-library/react'
import { MemoryRouter, Route, Routes } from 'react-router-dom'
import { beforeEach, describe, expect, it, vi } from 'vitest'
import { Login } from './Login'

const login = vi.fn()

vi.mock('../../auth/AuthContext', () => ({
  useAuth: () => ({ login }),
}))

function renderLogin(from?: string) {
  return render(
    <MemoryRouter initialEntries={[{ pathname: '/login', state: from ? { from } : undefined }]}>
      <Routes>
        <Route path="/login" element={<Login />} />
        <Route path="/inicio" element={<p>destino-inicio</p>} />
        <Route path="/primeiro-acesso" element={<p>destino-primeiro</p>} />
        <Route path="/eventos/:id/presenca" element={<p>destino-presenca</p>} />
      </Routes>
    </MemoryRouter>,
  )
}

describe('Login', () => {
  beforeEach(() => {
    login.mockReset()
  })

  it('navega para from após login com senha já alterada', async () => {
    login.mockResolvedValue({ mustChangePassword: false, accessToken: 'x', expiresIn: 900, tokenType: 'Bearer' })
    renderLogin('/eventos/abc/presenca')
    fireEvent.change(screen.getByLabelText('E-mail ou GRR'), { target: { value: 'aluno.dev@ufpr.br' } })
    fireEvent.change(screen.getByLabelText('Senha'), { target: { value: 'TroqueEstaSenha1!' } })
    fireEvent.click(screen.getByRole('button', { name: 'Entrar' }))
    await waitFor(() => {
      expect(screen.getByText('destino-presenca')).toBeTruthy()
    })
  })

  it('primeiro acesso ignora from', async () => {
    login.mockResolvedValue({ mustChangePassword: true, accessToken: 'x', expiresIn: 900, tokenType: 'Bearer' })
    renderLogin('/eventos/abc/presenca')
    fireEvent.change(screen.getByLabelText('E-mail ou GRR'), { target: { value: 'novo.dev@ufpr.br' } })
    fireEvent.change(screen.getByLabelText('Senha'), { target: { value: 'TroqueEstaSenha1!' } })
    fireEvent.click(screen.getByRole('button', { name: 'Entrar' }))
    await waitFor(() => {
      expect(screen.getByText('destino-primeiro')).toBeTruthy()
    })
  })
})
