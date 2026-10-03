import { render, screen } from '@testing-library/react'
import { QueryClient, QueryClientProvider } from '@tanstack/react-query'
import { describe, expect, it, vi } from 'vitest'
import { Jobs } from './Jobs'

vi.mock('../../api/jobs', () => ({
  jobsApi: {
    listar: () =>
      Promise.resolve({
        content: [
          {
            id: 'e1',
            tipo: 'solicitacao.criada',
            payloadResumo: '{"id":"1"}',
            status: 'FAILED',
            tentativas: 3,
            createdAt: '2026-10-03T12:00:00Z',
            _links: { retry: '/admin/outbox/e1/reentrega' },
          },
          {
            id: 'e2',
            tipo: 'solicitacao.criada',
            payloadResumo: '{"id":"2"}',
            status: 'SENT',
            tentativas: 1,
            createdAt: '2026-10-03T12:00:00Z',
            _links: { self: '/admin/outbox/e2' },
          },
        ],
        page: { number: 0, size: 20, totalElements: 2, totalPages: 1 },
        jobs: [],
        alertaLatencia: null,
      }),
    reentregar: vi.fn(),
  },
}))

describe('Jobs', () => {
  it('mostra Reentregar só quando existe _links.retry', async () => {
    const client = new QueryClient({ defaultOptions: { queries: { retry: false } } })
    render(
      <QueryClientProvider client={client}>
        <Jobs />
      </QueryClientProvider>,
    )
    expect(await screen.findByText('e1')).toBeTruthy()
    expect(screen.getByText('e2')).toBeTruthy()
    expect(screen.getAllByRole('button', { name: 'Reentregar' })).toHaveLength(1)
  })
})
