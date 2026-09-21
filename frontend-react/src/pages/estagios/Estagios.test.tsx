import { QueryClient, QueryClientProvider } from '@tanstack/react-query'
import { render, screen, waitFor } from '@testing-library/react'
import { MemoryRouter } from 'react-router-dom'
import { beforeEach, describe, expect, it, vi } from 'vitest'
import { ApiError } from '../../api/client'
import { EstagiosRota } from './EstagiosRota'

const listarMeus = vi.fn()
const listarParaRevisao = vi.fn()

vi.mock('../../api/estagios', () => ({
  estagiosApi: {
    listarMeus: (...args: unknown[]) => listarMeus(...args),
    listarParaRevisao: (...args: unknown[]) => listarParaRevisao(...args),
  },
}))

function renderRota(path = '/estagios') {
  const client = new QueryClient({ defaultOptions: { queries: { retry: false } } })
  return render(
    <QueryClientProvider client={client}>
      <MemoryRouter initialEntries={[path]}>
        <EstagiosRota />
      </MemoryRouter>
    </QueryClientProvider>,
  )
}

const paginaVazia = { content: [], page: { number: 0, size: 20, totalElements: 0, totalPages: 0 }, _links: {} }

describe('Estagios', () => {
  beforeEach(() => {
    listarMeus.mockReset()
    listarParaRevisao.mockReset()
  })

  it('mostra loading, empty e não oferece Novo estágio', async () => {
    listarMeus.mockReturnValue(new Promise(() => undefined))
    const { unmount } = renderRota()
    expect(screen.getByText('Carregando estágios…')).toBeTruthy()
    unmount()

    listarMeus.mockResolvedValue(paginaVazia)
    renderRota()
    await waitFor(() => {
      expect(screen.getByText('Você não possui estágios registrados.')).toBeTruthy()
    })
    expect(screen.queryByText('Novo estágio')).toBeNull()
  })

  it('mostra erro com retry', async () => {
    listarMeus.mockRejectedValue(new Error('falha'))
    renderRota()
    await waitFor(() => {
      expect(screen.getByText(/Não foi possível carregar os estágios/)).toBeTruthy()
    })
    expect(screen.getByRole('button', { name: 'Tentar de novo' })).toBeTruthy()
  })

  it('abre o estágio só quando self existe', async () => {
    listarMeus.mockResolvedValue({
      ...paginaVazia,
      content: [
        {
          id: 'e1',
          empresa: 'Empresa Fictícia SEPT',
          supervisor: 'Carla Supervisora',
          inicio: '2026-03-02',
          fim: '2026-11-30',
          situacao: 'ATIVO',
          documentos: [],
          pareceres: [],
          _links: { self: '/estagios/e1' },
        },
      ],
    })
    renderRota()
    await waitFor(() => {
      expect(screen.getByText('Empresa Fictícia SEPT')).toBeTruthy()
    })
    expect(screen.getByRole('link', { name: 'Abrir' })).toBeTruthy()
    expect(screen.queryByText('Novo estágio')).toBeNull()
    expect(screen.getByText('02/03/2026 – 30/11/2026')).toBeTruthy()
  })

  it('fila vazia e 403 honesto', async () => {
    listarParaRevisao.mockResolvedValue(paginaVazia)
    const { unmount } = renderRota('/estagios?to=me')
    await waitFor(() => {
      expect(screen.getByText('Nenhum estágio aguardando sua revisão.')).toBeTruthy()
    })
    unmount()

    listarParaRevisao.mockRejectedValue(new ApiError(403, 'negado'))
    renderRota('/estagios?to=me')
    await waitFor(() => {
      expect(screen.getByText('Fila de revisão indisponível para esta sessão.')).toBeTruthy()
    })
  })

  it('fila mostra Revisar só com o rel', async () => {
    listarParaRevisao.mockResolvedValue({
      ...paginaVazia,
      content: [
        {
          id: 'e1',
          alunoNome: 'Aluno Dev',
          empresa: 'Empresa Fictícia SEPT',
          supervisor: 'Carla',
          inicio: '2026-03-02',
          fim: '2026-11-30',
          situacao: 'ATIVO',
          documentoPendente: 'RELATORIO_FINAL',
          documentos: [],
          pareceres: [],
          _links: { self: '/estagios/e1', revisar: '/estagios/e1' },
        },
        {
          id: 'e2',
          alunoNome: 'Outro',
          empresa: 'Sem pendência',
          supervisor: 'Ana',
          inicio: '2026-03-02',
          fim: '2026-11-30',
          situacao: 'ATIVO',
          documentos: [],
          pareceres: [],
          _links: { self: '/estagios/e2' },
        },
      ],
    })
    renderRota('/estagios?to=me')
    await waitFor(() => {
      expect(screen.getByText('Relatório final')).toBeTruthy()
    })
    expect(screen.getAllByRole('link', { name: 'Revisar' })).toHaveLength(1)
    expect(screen.getByRole('link', { name: 'Abrir' })).toBeTruthy()
    expect(screen.queryByRole('checkbox')).toBeNull()
  })
})
