import { render, screen, waitFor } from '@testing-library/react'
import { QueryClient, QueryClientProvider } from '@tanstack/react-query'
import { MemoryRouter, Route, Routes } from 'react-router-dom'
import { describe, expect, it, vi } from 'vitest'
import { SolicitacaoDetalhe } from './SolicitacaoDetalhe'

const obter = vi.fn()

vi.mock('../../api/solicitacoes', () => ({
  solicitacoesApi: {
    obter: (...args: unknown[]) => obter(...args),
    editarPayload: vi.fn(),
    reenviar: vi.fn(),
    gerarProtocolo: vi.fn(),
  },
}))

function renderDetalhe() {
  const client = new QueryClient({ defaultOptions: { queries: { retry: false } } })
  return render(
    <QueryClientProvider client={client}>
      <MemoryRouter initialEntries={['/solicitacoes/s1']}>
        <Routes>
          <Route path="/solicitacoes/:id" element={<SolicitacaoDetalhe />} />
        </Routes>
      </MemoryRouter>
    </QueryClientProvider>,
  )
}

const base = {
  id: 's1',
  protocolo: 'PROT-2026-00008',
  tipoCodigo: 'DECLARACAO_SIMPLES',
  tipoNome: 'Declaração simples',
  tipoVersao: 1,
  estado: 'DELIBERADA',
  payload: { finalidade: 'vinculo academico' },
  formSchema: null,
  prazoEm: '2026-12-01T00:00:00Z',
  prazoVencido: false,
  sla: 'NO_PRAZO' as const,
  createdAt: '2026-11-01T00:00:00Z',
  updatedAt: '2026-11-01T00:00:00Z',
  eventos: [],
}

describe('SolicitacaoDetalhe', () => {
  it('não oferece gerar protocolo sem o link', async () => {
    obter.mockResolvedValue({ ...base, _links: { self: '/requests/s1' } })
    renderDetalhe()
    expect(await screen.findByRole('heading', { name: 'PROT-2026-00008' })).toBeTruthy()
    expect(screen.queryByRole('button', { name: 'Gerar protocolo' })).toBeNull()
    expect(screen.queryByRole('button', { name: 'Editar' })).toBeNull()
    expect(screen.queryByRole('button', { name: 'Reenviar' })).toBeNull()
  })

  it('oferece editar, reenviar e protocolo só pelos links', async () => {
    obter.mockResolvedValue({
      ...base,
      estado: 'EM_AJUSTE',
      _links: {
        self: '/requests/s1',
        editar: '/requests/s1',
        reenviar: '/requests/s1/reenviar',
        'gerar-protocolo': '/requests/s1/protocolo',
      },
    })
    renderDetalhe()
    await waitFor(() => {
      expect(screen.getByRole('button', { name: 'Editar' })).toBeTruthy()
    })
    const protocolo = screen.getByRole('button', { name: 'Gerar protocolo' })
    expect((protocolo as HTMLButtonElement).disabled).toBe(false)
    expect(screen.getByRole('button', { name: 'Reenviar' })).toBeTruthy()
  })
})
