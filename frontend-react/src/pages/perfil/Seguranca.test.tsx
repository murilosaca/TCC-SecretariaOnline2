import { QueryClient, QueryClientProvider } from '@tanstack/react-query'
import { fireEvent, render, screen, waitFor } from '@testing-library/react'
import { MemoryRouter } from 'react-router-dom'
import { beforeEach, describe, expect, it, vi } from 'vitest'
import { ApiError } from '../../api/client'
import { Seguranca } from './Seguranca'

const sessoes = vi.fn()
const trocarSenha = vi.fn()
const encerrarSessao = vi.fn()

vi.mock('../../api/perfil', () => ({
  perfilApi: {
    sessoes: (...args: unknown[]) => sessoes(...args),
    trocarSenha: (...args: unknown[]) => trocarSenha(...args),
    encerrarSessao: (...args: unknown[]) => encerrarSessao(...args),
  },
}))

vi.mock('../../auth/AuthContext', () => ({
  useAuth: () => ({
    links: {
      perfil: '/perfil',
      'perfil-seguranca': '/perfil/seguranca',
      'perfil-notificacoes': '/perfil/notificacoes',
    },
  }),
}))

const lista = {
  itens: [
    {
      id: 'sessao-atual',
      dispositivo: 'Windows · Chrome',
      atual: true,
      criadaEm: '2026-10-01T12:00:00Z',
      expiraEm: '2026-10-08T12:00:00Z',
      _links: {},
    },
    {
      id: 'sessao-iphone',
      dispositivo: 'iPhone · Safari',
      atual: false,
      criadaEm: '2026-10-01T11:00:00Z',
      expiraEm: '2026-10-08T11:00:00Z',
      _links: { encerrar: '/me/sessions/sessao-iphone' },
    },
  ],
}

function renderPage() {
  const client = new QueryClient({ defaultOptions: { queries: { retry: false } } })
  return render(
    <QueryClientProvider client={client}>
      <MemoryRouter>
        <Seguranca />
      </MemoryRouter>
    </QueryClientProvider>,
  )
}

describe('Seguranca', () => {
  beforeEach(() => {
    sessoes.mockReset()
    trocarSenha.mockReset()
    encerrarSessao.mockReset()
    sessoes.mockResolvedValue(lista)
  })

  it('mostra senha atual incorreta no 401', async () => {
    trocarSenha.mockRejectedValue(new ApiError(401, 'Senha atual incorreta.'))
    renderPage()
    fireEvent.change(await screen.findByLabelText('Senha atual'), { target: { value: 'senha-errada' } })
    fireEvent.change(screen.getByLabelText('Nova senha'), { target: { value: 'OutraSenhaForte1!' } })
    fireEvent.change(screen.getByLabelText('Confirmar nova senha'), { target: { value: 'OutraSenhaForte1!' } })
    fireEvent.click(screen.getByRole('button', { name: 'Salvar senha' }))
    expect(await screen.findByText('Senha atual incorreta.')).toBeTruthy()
    expect(trocarSenha).toHaveBeenCalledWith({
      senhaAtual: 'senha-errada',
      novaSenha: 'OutraSenhaForte1!',
    })
  })

  it('encerra só a sessão que tem _links.encerrar', async () => {
    encerrarSessao.mockResolvedValue(undefined)
    renderPage()
    expect(await screen.findByText('iPhone · Safari')).toBeTruthy()
    expect(screen.getByText('Este dispositivo')).toBeTruthy()
    const botoes = screen.getAllByRole('button', { name: 'Encerrar' })
    expect(botoes).toHaveLength(1)
    fireEvent.click(botoes[0])
    await waitFor(() => {
      expect(encerrarSessao).toHaveBeenCalledWith('/me/sessions/sessao-iphone')
    })
  })
})
