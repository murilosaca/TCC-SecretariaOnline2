import { QueryClient, QueryClientProvider } from '@tanstack/react-query'
import { fireEvent, render, screen, waitFor } from '@testing-library/react'
import { MemoryRouter } from 'react-router-dom'
import { beforeEach, describe, expect, it, vi } from 'vitest'
import { Certificados } from './Certificados'

const listarMeus = vi.fn()
const baixar = vi.fn()

vi.mock('../../api/certificados', () => ({
  certificadosApi: {
    listarMeus: (...args: unknown[]) => listarMeus(...args),
    baixar: (...args: unknown[]) => baixar(...args),
  },
}))

function renderPage() {
  const client = new QueryClient({
    defaultOptions: { queries: { retry: false } },
  })
  return render(
    <QueryClientProvider client={client}>
      <MemoryRouter>
        <Certificados />
      </MemoryRouter>
    </QueryClientProvider>,
  )
}

describe('Certificados', () => {
  beforeEach(() => {
    listarMeus.mockReset()
    baixar.mockReset()
  })

  it('mostra empty quando não há certificados', async () => {
    listarMeus.mockResolvedValue({ content: [], page: { number: 0, size: 20, totalElements: 0, totalPages: 0 } })
    renderPage()
    await waitFor(() => {
      expect(screen.getByText('Você ainda não possui certificados emitidos.')).toBeTruthy()
    })
  })

  it('só mostra Download se _links.download existir', async () => {
    listarMeus.mockResolvedValue({
      content: [
        {
          id: 'c1',
          tipo: 'FORMATIVA',
          titulo: 'Oficina',
          cargaHoraria: 4,
          emitidoEm: '2026-09-20T18:00:00Z',
          hashSha256: 'a'.repeat(64),
          _links: { self: '/certificates/c1', download: '/certificates/c1/download' },
        },
        {
          id: 'c2',
          tipo: 'FORMATIVA',
          titulo: 'Sem download',
          cargaHoraria: 2,
          emitidoEm: '2026-09-20T18:00:00Z',
          hashSha256: 'b'.repeat(64),
          _links: { self: '/certificates/c2' },
        },
      ],
      page: { number: 0, size: 20, totalElements: 2, totalPages: 1 },
    })
    renderPage()
    await waitFor(() => {
      expect(screen.getByText('Oficina')).toBeTruthy()
    })
    expect(screen.getAllByRole('button', { name: 'Download' })).toHaveLength(1)
    expect(screen.getByText('Sem download')).toBeTruthy()
  })

  it('mostra erro com retry', async () => {
    listarMeus.mockRejectedValue(new Error('falha'))
    renderPage()
    await waitFor(() => {
      expect(screen.getByText(/Não foi possível carregar os certificados/)).toBeTruthy()
    })
    expect(screen.getByRole('button', { name: 'Tentar de novo' })).toBeTruthy()
    fireEvent.click(screen.getByRole('button', { name: 'Tentar de novo' }))
    expect(listarMeus).toHaveBeenCalledTimes(2)
  })
})
