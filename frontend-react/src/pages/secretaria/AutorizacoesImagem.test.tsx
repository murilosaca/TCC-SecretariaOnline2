import { fireEvent, render, screen, waitFor } from '@testing-library/react'
import { QueryClient, QueryClientProvider } from '@tanstack/react-query'
import { MemoryRouter } from 'react-router-dom'
import { beforeEach, describe, expect, it, vi } from 'vitest'
import { ApiError } from '../../api/client'
import { AutorizacoesImagem } from './AutorizacoesImagem'

const listar = vi.fn()
const deliberar = vi.fn()

vi.mock('../../api/solicitacoes', () => ({
  solicitacoesApi: {
    listarAutorizacoesImagem: (...args: unknown[]) => listar(...args),
    deliberarImagemLote: (...args: unknown[]) => deliberar(...args),
  },
}))

const item = {
  id: 'img-1',
  protocolo: 'P1',
  tipoCodigo: 'AUTORIZACAO_IMAGEM',
  tipoNome: 'Imagem',
  tipoVersao: 1,
  estado: 'ABERTA',
  payload: {},
  solicitanteNome: 'Ana',
  solicitanteGrr: 'GRR20240001',
  cursoSigla: 'TADS',
  prazoEm: '2026-10-01T00:00:00Z',
  prazoVencido: false,
  sla: 'NO_PRAZO',
  createdAt: '2026-10-01T00:00:00Z',
  updatedAt: '2026-10-01T00:00:00Z',
  eventos: [],
  _links: { bulk_deliberate: '/requests/bulk-deliberate' },
}

function renderTela() {
  const client = new QueryClient({ defaultOptions: { queries: { retry: false } } })
  return render(
    <QueryClientProvider client={client}>
      <MemoryRouter>
        <AutorizacoesImagem />
      </MemoryRouter>
    </QueryClientProvider>,
  )
}

describe('AutorizacoesImagem', () => {
  beforeEach(() => {
    listar.mockReset()
    deliberar.mockReset()
  })

  it('não mostra ações de lote sem os links', async () => {
    listar.mockResolvedValue({
      content: [{ ...item, _links: {} }],
      page: { number: 0, size: 50, totalElements: 1, totalPages: 1 },
      _links: {},
    })
    renderTela()
    expect(await screen.findByText('Ana')).toBeTruthy()
    expect(screen.queryByRole('button', { name: 'Aprovar' })).toBeNull()
    expect(screen.queryByRole('checkbox')).toBeNull()
  })

  it('mantém o status visível quando o lote falha', async () => {
    listar.mockResolvedValue({
      content: [item],
      page: { number: 0, size: 50, totalElements: 1, totalPages: 1 },
      _links: { bulkDeliberate: '/requests/bulk-deliberate' },
    })
    deliberar.mockRejectedValue(new ApiError(409, 'Uma ou mais solicitações não estão abertas.', undefined, ['img-1']))
    renderTela()
    fireEvent.click(await screen.findByRole('checkbox', { name: 'Selecionar Ana' }))
    fireEvent.click(screen.getByRole('button', { name: 'Aprovar' }))
    expect((await screen.findByRole('alert')).textContent).toContain('img-1')
    await waitFor(() => expect(screen.getByText('ABERTA')).toBeTruthy())
  })
})
