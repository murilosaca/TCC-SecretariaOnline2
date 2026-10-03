import { render, screen } from '@testing-library/react'
import { QueryClient, QueryClientProvider } from '@tanstack/react-query'
import { describe, expect, it, vi } from 'vitest'
import { Perfis } from './Perfis'

vi.mock('../../api/perfis', () => ({
  perfisApi: {
    listar: () =>
      Promise.resolve({
        content: [
          {
            id: 'p1',
            nome: 'ALUNO',
            descricao: 'Papel de sistema',
            tipo: 'SYSTEM',
            authorities: ['request.open'],
            quantidadeAuthorities: 1,
            quantidadeUsuarios: 2,
            _links: { self: '/admin/perfis/p1', atualizar: '/admin/perfis/p1' },
          },
          {
            id: 'p2',
            nome: 'estagio_local',
            descricao: 'Customizado',
            tipo: 'CUSTOM',
            authorities: [],
            quantidadeAuthorities: 0,
            quantidadeUsuarios: 0,
            _links: { self: '/admin/perfis/p2', excluir: '/admin/perfis/p2' },
          },
        ],
        page: { number: 0, size: 20, totalElements: 2, totalPages: 1 },
      }),
    criar: vi.fn(),
    excluir: vi.fn(),
  },
}))

describe('Perfis', () => {
  it('perfil de sistema não tem Excluir', async () => {
    const client = new QueryClient({ defaultOptions: { queries: { retry: false } } })
    render(
      <QueryClientProvider client={client}>
        <Perfis />
      </QueryClientProvider>,
    )
    expect(await screen.findByText('ALUNO')).toBeTruthy()
    expect(screen.getByText('estagio_local')).toBeTruthy()
    expect(screen.getAllByRole('button', { name: 'Excluir' })).toHaveLength(1)
  })
})
