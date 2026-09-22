import { QueryClient, QueryClientProvider } from '@tanstack/react-query'
import { render, screen, waitFor } from '@testing-library/react'
import { MemoryRouter } from 'react-router-dom'
import { beforeEach, describe, expect, it, vi } from 'vitest'
import { TccsRota } from './TccsRota'

const listarMeus = vi.fn()
const listarParaRevisao = vi.fn()

vi.mock('../../api/tccs', () => ({
  tccsApi: {
    listarMeus: (...args: unknown[]) => listarMeus(...args),
    listarParaRevisao: (...args: unknown[]) => listarParaRevisao(...args),
  },
}))

function renderRota(path = '/tccs') {
  const client = new QueryClient({ defaultOptions: { queries: { retry: false } } })
  return render(
    <QueryClientProvider client={client}>
      <MemoryRouter initialEntries={[path]}>
        <TccsRota />
      </MemoryRouter>
    </QueryClientProvider>,
  )
}

const paginaVazia = { content: [], page: { number: 0, size: 20, totalElements: 0, totalPages: 0 }, _links: {} }

describe('Tccs', () => {
  beforeEach(() => {
    listarMeus.mockReset()
    listarParaRevisao.mockReset()
  })

  it('mostra empty da secretaria e não oferece Novo TCC', async () => {
    listarMeus.mockResolvedValue(paginaVazia)
    renderRota()
    await waitFor(() => {
      expect(screen.getByText('Nenhum TCC registrado. Consulte a secretaria.')).toBeTruthy()
    })
    expect(screen.queryByText('Novo TCC')).toBeNull()
  })

  it('lista título e orientador sem botão de criar', async () => {
    listarMeus.mockResolvedValue({
      ...paginaVazia,
      content: [
        {
          id: 't1',
          titulo: 'Plataforma SEPT',
          orientadorRotulo: 'professor.dev@ufpr.br',
          estado: 'EM_ELABORACAO',
          dataDefesa: '2026-11-12',
          _links: { self: '/tccs/t1' },
        },
      ],
    })
    renderRota()
    await waitFor(() => {
      expect(screen.getByText('Plataforma SEPT')).toBeTruthy()
    })
    expect(screen.getByText('professor.dev@ufpr.br')).toBeTruthy()
    expect(screen.queryByText('Novo TCC')).toBeNull()
  })

  it('fila vazia usa a mensagem de avaliação', async () => {
    listarParaRevisao.mockResolvedValue(paginaVazia)
    renderRota('/tccs?to=me')
    await waitFor(() => {
      expect(screen.getByText('Nenhum TCC aguardando sua avaliação.')).toBeTruthy()
    })
    expect(screen.queryByRole('checkbox')).toBeNull()
  })

  it('fila mostra Avaliar só com o link', async () => {
    listarParaRevisao.mockResolvedValue({
      ...paginaVazia,
      content: [
        {
          id: 't1',
          alunoNome: 'Aluno',
          titulo: 'Plataforma SEPT',
          papel: 'ORIENTADOR',
          estado: 'SUBMETIDO',
          _links: { self: '/tccs/t1', avaliar: '/tccs/t1/avaliacoes' },
        },
      ],
    })
    renderRota('/tccs?to=me')
    await waitFor(() => {
      expect(screen.getByText('Avaliar')).toBeTruthy()
    })
    expect(screen.getByText('Orientador')).toBeTruthy()
  })
})
