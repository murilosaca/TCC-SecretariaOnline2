import { render, screen } from '@testing-library/react'
import { QueryClient, QueryClientProvider } from '@tanstack/react-query'
import { MemoryRouter } from 'react-router-dom'
import { beforeEach, describe, expect, it, vi } from 'vitest'
import { academicoApi } from '../../api/academico'
import { eventosApi } from '../../api/eventos'
import { EventosSecretaria } from './EventosSecretaria'

vi.mock('../../api/eventos', () => ({
  eventosApi: {
    listarDoEscopo: vi.fn(),
    excluir: vi.fn(),
  },
}))

vi.mock('../../api/academico', () => ({
  academicoApi: {
    listarCursos: vi.fn(),
  },
}))

function renderPage() {
  const client = new QueryClient({ defaultOptions: { queries: { retry: false } } })
  return render(
    <QueryClientProvider client={client}>
      <MemoryRouter initialEntries={['/secretaria/eventos']}>
        <EventosSecretaria />
      </MemoryRouter>
    </QueryClientProvider>,
  )
}

describe('EventosSecretaria', () => {
  beforeEach(() => {
    vi.mocked(eventosApi.listarDoEscopo).mockReset()
    vi.mocked(academicoApi.listarCursos).mockReset()
    vi.mocked(academicoApi.listarCursos).mockResolvedValue({
      content: [{ id: 'c1', nome: 'TADS', sigla: 'TADS', codigo: 'TADS', horasFormativasMinimas: 120, ativo: true }],
      page: { number: 0, size: 100, totalElements: 1, totalPages: 1 },
    })
  })

  it('CONCLUIDO não oferece excluir e Novo evento só com _links', async () => {
    vi.mocked(eventosApi.listarDoEscopo).mockResolvedValue({
      content: [
        {
          id: 'e1',
          titulo: 'Oficina encerrada',
          inicioEm: '2026-10-01T12:00:00Z',
          fimEm: '2026-10-01T14:00:00Z',
          cargaHoraria: 2,
          attendanceMode: 'QR_SINGLE',
          estado: 'CONCLUIDO',
          situacaoPresenca: 'PENDENTE',
          janelaAtiva: false,
          _links: { self: '/events/e1' },
        },
      ],
      page: { number: 0, size: 20, totalElements: 1, totalPages: 1 },
      _links: { novoEvento: '/events' },
    })
    renderPage()
    expect(await screen.findByText('Oficina encerrada')).toBeTruthy()
    expect(screen.getByRole('link', { name: 'Novo evento' })).toBeTruthy()
    expect(screen.queryByRole('button', { name: 'Excluir' })).toBeNull()
    expect(screen.queryByRole('link', { name: 'Editar' })).toBeNull()
  })
})
