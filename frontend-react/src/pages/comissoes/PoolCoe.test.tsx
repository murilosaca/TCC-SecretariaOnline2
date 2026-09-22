import { QueryClient, QueryClientProvider } from '@tanstack/react-query'
import { fireEvent, render, screen, waitFor } from '@testing-library/react'
import { MemoryRouter } from 'react-router-dom'
import { beforeEach, describe, expect, it, vi } from 'vitest'
import { ApiError } from '../../api/client'
import type { CoePool } from '../../models/coe'
import { PoolCoe } from './PoolCoe'

const pool = vi.fn()
const atribuir = vi.fn()

vi.mock('../../api/coe', () => ({
  coeApi: {
    pool: (...args: unknown[]) => pool(...args),
    atribuir: (...args: unknown[]) => atribuir(...args),
  },
}))

function renderPage() {
  const client = new QueryClient({ defaultOptions: { queries: { retry: false } } })
  return render(
    <QueryClientProvider client={client}>
      <MemoryRouter>
        <PoolCoe />
      </MemoryRouter>
    </QueryClientProvider>,
  )
}

const vazio: CoePool = {
  meuId: 'prof-1',
  kpis: { poolTotal: 0, atribuidosAMim: 0, documentosPendentes: 0, concluidosNoPeriodo: 0 },
  membros: [{ id: 'prof-1', rotulo: 'professor.dev@ufpr.br', carga: 0, cargaAlta: false }],
  content: [],
  _links: { self: '/comissoes/coe', 'assign-member': '/comissoes/coe/atribuicoes' },
}

function comItem(extra: Partial<CoePool['content'][0]> = {}): CoePool {
  return {
    ...vazio,
    kpis: { ...vazio.kpis, poolTotal: 1 },
    content: [
      {
        id: 'e1',
        idAluno: 'a1',
        alunoNome: 'Aluno Dev',
        idCurso: 'c1',
        empresa: 'Pool COE SEPT',
        inicio: '2026-04-01',
        noPool: true,
        comigo: false,
        _links: { self: '/estagios/e1', 'assign-member': '/comissoes/coe/atribuicoes' },
        ...extra,
      },
    ],
  }
}

describe('PoolCoe', () => {
  beforeEach(() => {
    pool.mockReset()
    atribuir.mockReset()
  })

  it('mostra loading, empty e 403 honesto', async () => {
    pool.mockReturnValue(new Promise(() => undefined))
    const { unmount } = renderPage()
    expect(screen.getByText('Carregando pool da COE…')).toBeTruthy()
    unmount()

    pool.mockResolvedValue(vazio)
    const empty = renderPage()
    await waitFor(() => {
      expect(screen.getByText('Todos os estágios do período já têm orientador atribuído.')).toBeTruthy()
    })
    empty.unmount()

    pool.mockRejectedValue(new ApiError(403, 'negado'))
    renderPage()
    await waitFor(() => {
      expect(screen.getByText('Pool da COE indisponível para esta sessão.')).toBeTruthy()
    })
  })

  it('atribui a mim e não oferece aprovar em lote', async () => {
    const inicial = comItem()
    const depois: CoePool = {
      ...inicial,
      kpis: { poolTotal: 0, atribuidosAMim: 1, documentosPendentes: 0, concluidosNoPeriodo: 0 },
      content: [
        {
          ...inicial.content[0],
          noPool: false,
          comigo: true,
          idOrientador: 'prof-1',
          orientadorRotulo: 'professor.dev@ufpr.br',
        },
      ],
    }
    pool.mockResolvedValue(inicial)
    atribuir.mockResolvedValue(depois)
    renderPage()
    await waitFor(() => {
      expect(screen.getByText('Pool COE SEPT')).toBeTruthy()
    })
    expect(screen.getByText('No pool')).toBeTruthy()
    expect(screen.getByText('Atribuir a mim')).toBeTruthy()
    expect(screen.queryByText('Aprovar selecionados')).toBeNull()
    expect(screen.queryByText('Aprovar')).toBeNull()

    fireEvent.click(screen.getByRole('button', { name: 'Atribuir a mim' }))
    await waitFor(() => {
      expect(atribuir).toHaveBeenCalledWith('e1', 'prof-1')
    })
    await waitFor(() => {
      expect(screen.getByText('Comigo')).toBeTruthy()
    })
    expect(screen.getByText('Atribuídos a mim')).toBeTruthy()
  })

  it('barra em lote só atribui ao mesmo orientador', async () => {
    const inicial = comItem()
    pool.mockResolvedValue({
      ...inicial,
      membros: [
        { id: 'prof-1', rotulo: 'professor.dev@ufpr.br', carga: 0, cargaAlta: false },
        { id: 'coe-1', rotulo: 'coe.dev@ufpr.br', carga: 3, cargaAlta: true },
      ],
    })
    atribuir.mockResolvedValue(inicial)
    renderPage()
    await waitFor(() => {
      expect(screen.getByText('Aluno Dev')).toBeTruthy()
    })
    fireEvent.click(screen.getByRole('checkbox', { name: 'Selecionar estágio de Aluno Dev' }))
    expect(screen.getByRole('button', { name: 'Atribuir selecionados' })).toBeTruthy()
    expect(screen.queryByText('Aprovar selecionados')).toBeNull()

    fireEvent.click(screen.getByRole('button', { name: 'Atribuir selecionados' }))
    expect(screen.getByRole('dialog', { name: 'Atribuir orientador' })).toBeTruthy()
    expect(screen.getByText('Carga alta')).toBeTruthy()
    fireEvent.click(screen.getByRole('radio', { name: /coe.dev@ufpr.br/ }))
    fireEvent.click(screen.getByRole('button', { name: 'Confirmar' }))
    await waitFor(() => {
      expect(atribuir).toHaveBeenCalledWith('e1', 'coe-1')
    })
  })
})
