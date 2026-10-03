import { render, screen } from '@testing-library/react'
import { QueryClient, QueryClientProvider } from '@tanstack/react-query'
import { MemoryRouter } from 'react-router-dom'
import { beforeEach, describe, expect, it, vi } from 'vitest'
import { NovaSolicitacao } from './NovaSolicitacao'

const links = vi.hoisted(() => ({ atual: {} as Record<string, string> }))

vi.mock('../../auth/AuthContext', () => ({
  useAuth: () => ({ links: links.atual, status: 'authenticated', mustChangePassword: false }),
}))

vi.mock('../../api/solicitacoes', () => ({
  solicitacoesApi: {
    listarTipos: () =>
      Promise.resolve({
        content: [
          {
            id: 't1',
            codigo: 'DECLARACAO_SIMPLES',
            nome: 'Declaração simples',
            status: 'PUBLISHED',
            prazoDias: 5,
            versao: 1,
            formSchema: { type: 'object', properties: {} },
            workflowJson: {},
          },
          {
            id: 't-draft',
            codigo: 'RASCUNHO_SECRETO',
            nome: 'Rascunho secreto',
            status: 'DRAFT',
            prazoDias: 5,
            versao: 0,
            formSchema: { type: 'object', properties: {} },
            workflowJson: {},
          },
        ],
        page: { number: 0, size: 20, totalElements: 1, totalPages: 1 },
      }),
    criar: vi.fn(),
  },
}))

vi.mock('../../api/academico', () => ({
  academicoApi: { listarAlunos: vi.fn() },
}))

function renderNova() {
  const client = new QueryClient({ defaultOptions: { queries: { retry: false } } })
  return render(
    <QueryClientProvider client={client}>
      <MemoryRouter>
        <NovaSolicitacao />
      </MemoryRouter>
    </QueryClientProvider>,
  )
}

describe('NovaSolicitacao interna', () => {
  beforeEach(() => {
    links.atual = {}
  })

  it('não mostra o combobox sem o link nova-interna', async () => {
    renderNova()
    await screen.findByRole('heading', { name: 'Declaração simples' })
    expect(screen.queryByRole('heading', { name: 'Rascunho secreto' })).toBeNull()
    expect(screen.queryByRole('combobox', { name: 'Em nome de' })).toBeNull()
  })

  it('mostra o combobox somente com o link nova-interna', async () => {
    links.atual = { 'nova-interna': '/solicitacoes/nova' }
    renderNova()
    expect(await screen.findByRole('combobox', { name: 'Em nome de' })).toBeTruthy()
  })
})
