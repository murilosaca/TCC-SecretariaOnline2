import { render, screen, waitFor } from '@testing-library/react'
import { QueryClient, QueryClientProvider } from '@tanstack/react-query'
import { MemoryRouter } from 'react-router-dom'
import { describe, expect, it, vi } from 'vitest'
import { FilaDeliberacao } from './FilaDeliberacao'

const listarParaDeliberar = vi.fn()

vi.mock('../../api/solicitacoes', () => ({
  solicitacoesApi: {
    listarParaDeliberar: (...args: unknown[]) => listarParaDeliberar(...args),
    deliberarLote: vi.fn(),
    listarTipos: () =>
      Promise.resolve({
        content: [{ codigo: 'DECLARACAO_SIMPLES', nome: 'Declaração simples', status: 'PUBLISHED' }],
      }),
  },
}))

function renderFila() {
  const client = new QueryClient({ defaultOptions: { queries: { retry: false } } })
  return render(
    <QueryClientProvider client={client}>
      <MemoryRouter>
        <FilaDeliberacao />
      </MemoryRouter>
    </QueryClientProvider>,
  )
}

const item = {
  id: 's1',
  protocolo: 'PROT-2026-00007',
  tipoCodigo: 'DECLARACAO_SIMPLES',
  tipoNome: 'Declaração simples',
  tipoVersao: 1,
  estado: 'EM_ANALISE',
  payload: {},
  solicitanteNome: 'Aluno Dev',
  prazoEm: '2026-12-01T00:00:00Z',
  prazoVencido: false,
  sla: 'NO_PRAZO' as const,
  createdAt: '2026-11-01T00:00:00Z',
  updatedAt: '2026-11-01T00:00:00Z',
  eventos: [],
  _links: { deliberar: '/requests/s1' },
}

describe('FilaDeliberacao', () => {
  it('não oferece lote sem o link do workflow', async () => {
    listarParaDeliberar.mockResolvedValue({
      content: [item],
      page: { number: 0, size: 20, totalElements: 1, totalPages: 1 },
      _links: {},
    })
    renderFila()
    expect(await screen.findByText('PROT-2026-00007')).toBeTruthy()
    expect(screen.queryByRole('button', { name: 'Deliberar selecionadas' })).toBeNull()
  })

  it('oferece o lote só quando o link existe', async () => {
    listarParaDeliberar.mockResolvedValue({
      content: [item],
      page: { number: 0, size: 20, totalElements: 1, totalPages: 1 },
      _links: { 'bulk-deliberate': '/requests/deliberacao-lote' },
    })
    renderFila()
    await waitFor(() => {
      expect(screen.getByRole('button', { name: 'Deliberar selecionadas' })).toBeTruthy()
    })
  })
})
