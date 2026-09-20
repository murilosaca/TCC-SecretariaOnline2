import { QueryClient, QueryClientProvider } from '@tanstack/react-query'
import { render, screen } from '@testing-library/react'
import { MemoryRouter, Route, Routes } from 'react-router-dom'
import { describe, expect, it, vi } from 'vitest'
import { DeliberarSolicitacao } from './DeliberarSolicitacao'

vi.mock('../../auth/AuthContext', () => ({
  useAuth: () => ({
    status: 'anonymous',
    mustChangePassword: false,
    authorities: [],
    links: {},
    login: vi.fn(),
    logout: vi.fn(),
    completeFirstAccess: vi.fn(),
  }),
}))

const obter = vi.fn()
vi.mock('../../api/solicitacoes', () => ({
  solicitacoesApi: {
    obter: (...args: unknown[]) => obter(...args),
    transicionar: vi.fn(),
  },
}))

function renderDeliberar(path: string) {
  const client = new QueryClient({
    defaultOptions: { queries: { retry: false } },
  })
  return render(
    <QueryClientProvider client={client}>
      <MemoryRouter initialEntries={[path]}>
        <Routes>
          <Route path="/solicitacoes/:id/deliberar" element={<DeliberarSolicitacao />} />
          <Route path="/login" element={<p>pagina-login</p>} />
        </Routes>
      </MemoryRouter>
    </QueryClientProvider>,
  )
}

describe('DeliberarSolicitacao', () => {
  it('mostra banner de login quando há token e não há sessão', () => {
    renderDeliberar('/solicitacoes/abc/deliberar?token=jwt-deeplink')
    expect(screen.getByRole('status').textContent).toMatch(/Entre com sua conta institucional/i)
    expect(screen.getByRole('link', { name: 'Fazer login' })).toBeTruthy()
    expect(obter).not.toHaveBeenCalled()
  })
})
