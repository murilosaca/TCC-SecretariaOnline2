import { render, screen } from '@testing-library/react'
import { QueryClient, QueryClientProvider } from '@tanstack/react-query'
import { MemoryRouter } from 'react-router-dom'
import { describe, expect, it, vi } from 'vitest'
import { AuditLog } from './AuditLog'

vi.mock('../../api/audit', () => ({
  auditApi: {
    listar: () =>
      Promise.resolve({
        content: [
          {
            id: 'a1',
            atorId: 'u1',
            atorNome: 'Admin',
            acao: 'importacao.concluida',
            entidade: 'importacao',
            ip: '127.0.0.1',
            timestamp: '2026-10-03T12:00:00Z',
            payloadAntes: null,
            payloadDepois: '{"status":"SUCCESS"}',
          },
        ],
        page: { number: 0, size: 50, totalElements: 1, totalPages: 1 },
      }),
  },
}))

describe('AuditLog', () => {
  it('não oferece Excluir nem Editar', async () => {
    const client = new QueryClient({ defaultOptions: { queries: { retry: false } } })
    render(
      <QueryClientProvider client={client}>
        <MemoryRouter>
          <AuditLog />
        </MemoryRouter>
      </QueryClientProvider>,
    )
    expect(await screen.findByText('importacao.concluida')).toBeTruthy()
    expect(screen.queryByRole('button', { name: 'Excluir' })).toBeNull()
    expect(screen.queryByRole('button', { name: 'Editar' })).toBeNull()
  })
})
