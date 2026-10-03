import { QueryClient, QueryClientProvider } from '@tanstack/react-query'
import { render, screen, waitFor } from '@testing-library/react'
import { MemoryRouter } from 'react-router-dom'
import { beforeEach, describe, expect, it, vi } from 'vitest'
import { Formativas } from './Formativas'

const listarMinhas = vi.fn()

vi.mock('../../api/formativas', () => ({
  formativasApi: {
    listarMinhas: (...args: unknown[]) => listarMinhas(...args),
  },
}))

function renderPage() {
  const client = new QueryClient({ defaultOptions: { queries: { retry: false } } })
  return render(
    <QueryClientProvider client={client}>
      <MemoryRouter>
        <Formativas />
      </MemoryRouter>
    </QueryClientProvider>,
  )
}

describe('Formativas', () => {
  beforeEach(() => {
    listarMinhas.mockReset()
  })

  it('mostra Nova atividade só com _links.nova', async () => {
    listarMinhas.mockResolvedValueOnce({
      content: [],
      page: { number: 0, size: 20, totalElements: 0, totalPages: 0 },
    })
    const primeira = renderPage()
    await waitFor(() => {
      expect(screen.getByText(/Nenhuma atividade formativa/)).toBeTruthy()
    })
    expect(screen.queryByRole('link', { name: 'Nova atividade' })).toBeNull()
    primeira.unmount()

    listarMinhas.mockResolvedValueOnce({
      content: [
        {
          id: 'f1',
          idAluno: 'a1',
          origem: 'COMPROVANTE',
          titulo: 'Curso de extensão',
          cargaHoraria: 8,
          estado: 'AGUARDANDO_CAAF',
          createdAt: '2026-10-03T12:00:00Z',
          updatedAt: '2026-10-03T12:00:00Z',
          _links: { self: '/formativas/f1' },
        },
      ],
      page: { number: 0, size: 20, totalElements: 1, totalPages: 1 },
      _links: { nova: '/formativas/nova' },
    })
    renderPage()
    await waitFor(() => {
      expect(screen.getByRole('link', { name: 'Nova atividade' })).toBeTruthy()
    })
    expect(screen.getByText('Curso de extensão')).toBeTruthy()
    expect(screen.getByText('Comprovante')).toBeTruthy()
    expect(screen.getByText('Aguardando CAAF')).toBeTruthy()
  })
})
