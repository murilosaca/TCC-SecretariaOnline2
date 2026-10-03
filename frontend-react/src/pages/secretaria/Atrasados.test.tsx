import { fireEvent, render, screen, waitFor } from '@testing-library/react'
import { QueryClient, QueryClientProvider } from '@tanstack/react-query'
import { MemoryRouter } from 'react-router-dom'
import { beforeEach, describe, expect, it, vi } from 'vitest'
import { Atrasados } from './Atrasados'

const listarFilaCurso = vi.fn()
const exportarAtrasadosCsv = vi.fn()

vi.mock('../../api/solicitacoes', () => ({
  solicitacoesApi: {
    listarFilaCurso: (...args: unknown[]) => listarFilaCurso(...args),
    exportarAtrasadosCsv: (...args: unknown[]) => exportarAtrasadosCsv(...args),
  },
}))

function renderPage(entry = '/secretaria/atrasados') {
  const client = new QueryClient({
    defaultOptions: { queries: { retry: false } },
  })
  return render(
    <QueryClientProvider client={client}>
      <MemoryRouter initialEntries={[entry]}>
        <Atrasados />
      </MemoryRouter>
    </QueryClientProvider>,
  )
}

describe('Atrasados', () => {
  beforeEach(() => {
    listarFilaCurso.mockReset()
    exportarAtrasadosCsv.mockReset()
    vi.stubGlobal('URL', {
      createObjectURL: vi.fn(() => 'blob:atrasados'),
      revokeObjectURL: vi.fn(),
    })
  })

  it('lista slaBreached e exporta CSV', async () => {
    listarFilaCurso.mockResolvedValue({
      content: [
        {
          id: 's1',
          protocolo: 'PROT-2026-0009',
          tipoCodigo: 'DECLARACAO_SIMPLES',
          tipoNome: 'Declaração simples',
          tipoVersao: 1,
          estado: 'EM_ANALISE',
          payload: {},
          solicitanteNome: 'Ana',
          prazoEm: '2026-01-01T00:00:00Z',
          prazoVencido: true,
          sla: 'ATRASADO',
          slaStatus: 'danger',
          diasAtraso: 3,
          createdAt: '2026-01-01T00:00:00Z',
          updatedAt: '2026-01-01T00:00:00Z',
          eventos: [],
          _links: { deliberar: '/requests/s1' },
        },
      ],
      page: { number: 0, size: 20, totalElements: 1, totalPages: 1 },
      deliberadores: [],
    })
    exportarAtrasadosCsv.mockResolvedValue(new Blob(['csv'], { type: 'text/csv' }))
    renderPage()
    expect(await screen.findByText('PROT-2026-0009')).toBeTruthy()
    expect(listarFilaCurso).toHaveBeenCalledWith(
      expect.objectContaining({ slaBreached: true, estado: 'TODOS' }),
    )
    expect(screen.queryByText(/100 primeiros/)).toBeNull()
    fireEvent.click(screen.getByRole('button', { name: 'Exportar' }))
    await waitFor(() => {
      expect(exportarAtrasadosCsv).toHaveBeenCalledWith({ page: 0, size: 100 })
    })
  })

  it('exporta da página 0 com size 100 e avisa quando o total passa de 100', async () => {
    listarFilaCurso.mockResolvedValue({
      content: [
        {
          id: 's1',
          protocolo: 'PROT-2026-0009',
          tipoCodigo: 'DECLARACAO_SIMPLES',
          tipoNome: 'Declaração simples',
          tipoVersao: 1,
          estado: 'EM_ANALISE',
          payload: {},
          solicitanteNome: 'Ana',
          prazoEm: '2026-01-01T00:00:00Z',
          prazoVencido: true,
          sla: 'ATRASADO',
          slaStatus: 'danger',
          diasAtraso: 3,
          createdAt: '2026-01-01T00:00:00Z',
          updatedAt: '2026-01-01T00:00:00Z',
          eventos: [],
          _links: { deliberar: '/requests/s1' },
        },
      ],
      page: { number: 2, size: 20, totalElements: 140, totalPages: 7 },
      deliberadores: [],
    })
    exportarAtrasadosCsv.mockResolvedValue(new Blob(['csv'], { type: 'text/csv' }))
    renderPage('/secretaria/atrasados?page=2')
    expect(await screen.findByText('O CSV traz só os 100 primeiros do filtro.')).toBeTruthy()
    expect(screen.getByRole('button', { name: 'Exportar' })).toBeTruthy()
    fireEvent.click(screen.getByRole('button', { name: 'Exportar' }))
    await waitFor(() => {
      expect(exportarAtrasadosCsv).toHaveBeenCalledWith({ page: 0, size: 100 })
    })
    expect(listarFilaCurso).toHaveBeenCalledWith(
      expect.objectContaining({ slaBreached: true, estado: 'TODOS', page: 2 }),
    )
  })
})
