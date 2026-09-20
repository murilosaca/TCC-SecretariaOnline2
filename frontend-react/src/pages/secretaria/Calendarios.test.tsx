import { QueryClient, QueryClientProvider } from '@tanstack/react-query'
import { render, screen } from '@testing-library/react'
import { describe, expect, it, vi } from 'vitest'
import { ApiError } from '../../api/client'
import { Calendarios } from './Calendarios'

const periodoVigente = vi.fn()
const listarPeriodos = vi.fn()

vi.mock('../../api/academico', () => ({
  academicoApi: {
    periodoVigente: (...args: unknown[]) => periodoVigente(...args),
    listarPeriodos: (...args: unknown[]) => listarPeriodos(...args),
    criarPeriodo: vi.fn(),
    atualizarPeriodo: vi.fn(),
    excluirPeriodo: vi.fn(),
  },
}))

function renderPage() {
  const client = new QueryClient({
    defaultOptions: { queries: { retry: false } },
  })
  return render(
    <QueryClientProvider client={client}>
      <Calendarios />
    </QueryClientProvider>,
  )
}

const listaVazia = {
  content: [],
  page: { number: 0, size: 20, totalElements: 0, totalPages: 0 },
}

describe('Calendarios', () => {
  it('404 do vigente é alerta de negócio', async () => {
    periodoVigente.mockRejectedValue(new ApiError(404, 'Não há período letivo vigente'))
    listarPeriodos.mockResolvedValue(listaVazia)
    renderPage()
    expect(
      await screen.findByText(/Não há período letivo vigente hoje/),
    ).toBeTruthy()
    expect(screen.queryByText('Não foi possível carregar o período letivo vigente.')).toBeNull()
  })

  it('500 do vigente é erro técnico', async () => {
    periodoVigente.mockRejectedValue(new ApiError(500, 'Erro interno'))
    listarPeriodos.mockResolvedValue(listaVazia)
    renderPage()
    expect(await screen.findByText('Não foi possível carregar o período letivo vigente.')).toBeTruthy()
    expect(screen.queryByText(/Não há período letivo vigente hoje/)).toBeNull()
  })
})
