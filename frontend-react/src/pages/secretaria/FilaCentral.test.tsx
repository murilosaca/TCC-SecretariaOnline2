import { fireEvent, render, screen, waitFor } from '@testing-library/react'
import { QueryClient, QueryClientProvider } from '@tanstack/react-query'
import { MemoryRouter } from 'react-router-dom'
import { beforeEach, describe, expect, it, vi } from 'vitest'
import { FilaCentral } from './FilaCentral'

const listarFilaCurso = vi.fn()
const atribuirEmMassa = vi.fn()

vi.mock('../../api/solicitacoes', () => ({
  solicitacoesApi: {
    listarFilaCurso: (...args: unknown[]) => listarFilaCurso(...args),
    atribuirEmMassa: (...args: unknown[]) => atribuirEmMassa(...args),
  },
}))

function renderFila(initial = '/solicitacoes') {
  const client = new QueryClient({
    defaultOptions: { queries: { retry: false } },
  })
  return render(
    <QueryClientProvider client={client}>
      <MemoryRouter initialEntries={[initial]}>
        <FilaCentral />
      </MemoryRouter>
    </QueryClientProvider>,
  )
}

describe('FilaCentral', () => {
  beforeEach(() => {
    listarFilaCurso.mockReset()
    atribuirEmMassa.mockReset()
  })

  it('persiste filtros na URL ao aplicar', async () => {
    listarFilaCurso.mockResolvedValue({
      content: [],
      page: { number: 0, size: 20, totalElements: 0, totalPages: 0 },
      deliberadores: [],
      _links: {},
    })
    renderFila()
    await screen.findByRole('heading', { name: 'Fila de solicitações' })
    fireEvent.change(screen.getByLabelText('Estado'), { target: { value: 'EM_ANALISE' } })
    fireEvent.click(screen.getByRole('button', { name: 'Filtrar' }))
    await waitFor(() => {
      expect(listarFilaCurso).toHaveBeenCalledWith(
        expect.objectContaining({ estado: 'EM_ANALISE', page: 0 }),
      )
    })
  })

  it('exibe empty e permite atribuição em massa via bulk_assign', async () => {
    listarFilaCurso.mockResolvedValue({
      content: [
        {
          id: 's1',
          protocolo: 'PROT-2026-0001',
          tipoCodigo: 'DECLARACAO_SIMPLES',
          tipoNome: 'Declaração simples',
          tipoVersao: 1,
          estado: 'EM_ANALISE',
          payload: {},
          solicitanteNome: 'Ana',
          deliberadorNome: null,
          prazoEm: '2026-01-01T00:00:00Z',
          prazoVencido: true,
          sla: 'ATRASADO',
          slaStatus: 'danger',
          createdAt: '2026-01-01T00:00:00Z',
          updatedAt: '2026-01-01T00:00:00Z',
          eventos: [],
          _links: { bulk_assign: '/requests/bulk', deliberar: '/requests/s1' },
        },
      ],
      page: { number: 0, size: 20, totalElements: 1, totalPages: 1 },
      deliberadores: [{ id: 'p1', nome: 'Prof Dev' }],
      _links: { bulk: '/requests/bulk' },
    })
    atribuirEmMassa.mockResolvedValue([])
    renderFila()
    expect(await screen.findByText('PROT-2026-0001')).toBeTruthy()
    expect(screen.getByText('Atrasado')).toBeTruthy()
    fireEvent.click(screen.getByLabelText('Selecionar PROT-2026-0001'))
    fireEvent.change(screen.getByLabelText('Selecionar deliberador'), { target: { value: 'p1' } })
    fireEvent.click(screen.getByRole('button', { name: 'Atribuir' }))
    await waitFor(() => {
      expect(atribuirEmMassa).toHaveBeenCalledWith(['s1'], 'p1')
    })
  })
})
