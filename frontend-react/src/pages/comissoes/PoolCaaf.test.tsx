import { QueryClient, QueryClientProvider } from '@tanstack/react-query'
import { fireEvent, render, screen, waitFor } from '@testing-library/react'
import { MemoryRouter } from 'react-router-dom'
import { beforeEach, describe, expect, it, vi } from 'vitest'
import { ApiError } from '../../api/client'
import type { CaafPool } from '../../models/caaf'
import { PoolCaaf } from './PoolCaaf'

const pool = vi.fn()
const atribuir = vi.fn()
const aprovarLote = vi.fn()

vi.mock('../../api/caaf', () => ({
  caafApi: {
    pool: (...args: unknown[]) => pool(...args),
    atribuir: (...args: unknown[]) => atribuir(...args),
    aprovarLote: (...args: unknown[]) => aprovarLote(...args),
  },
}))

function renderPage() {
  const client = new QueryClient({ defaultOptions: { queries: { retry: false } } })
  return render(
    <QueryClientProvider client={client}>
      <MemoryRouter>
        <PoolCaaf />
      </MemoryRouter>
    </QueryClientProvider>,
  )
}

const vazio: CaafPool = {
  meuId: 'caaf-1',
  kpis: { poolTotal: 0, atribuidasAMim: 0, prazoMedioDias: 0, aprovadasNoPeriodo: 0 },
  membros: [{ id: 'caaf-1', rotulo: 'caaf.dev@ufpr.br', carga: 0, cargaAlta: false }],
  content: [],
  _links: {
    self: '/comissoes/caaf',
    'assign-member': '/comissoes/caaf/atribuicoes',
    'batch-approve': '/comissoes/caaf/lote',
  },
}

function comItem(extra: Partial<CaafPool['content'][0]> = {}): CaafPool {
  return {
    ...vazio,
    kpis: { ...vazio.kpis, poolTotal: 1 },
    content: [
      {
        id: 'f1',
        idAluno: 'a1',
        alunoNome: 'Aluno Dev',
        idCurso: 'c1',
        origem: 'PRESENCA_VALIDADA',
        titulo: 'Pool CAAF SEPT — oficina com presença',
        cargaHoraria: 4,
        estado: 'AGUARDANDO_CAAF',
        createdAt: '2026-09-20T12:00:00Z',
        noPool: true,
        comigo: false,
        elegivelLote: true,
        _links: {
          self: '/formativas/f1',
          'assign-member': '/comissoes/caaf/atribuicoes',
          'batch-approve': '/comissoes/caaf/lote',
          revisar: '/formativas/f1/revisar',
        },
        ...extra,
      },
    ],
  }
}

describe('PoolCaaf', () => {
  beforeEach(() => {
    pool.mockReset()
    atribuir.mockReset()
    aprovarLote.mockReset()
  })

  it('mostra loading, empty e 403 honesto', async () => {
    pool.mockReturnValue(new Promise(() => undefined))
    const { unmount } = renderPage()
    expect(screen.getByText('Carregando pool da CAAF…')).toBeTruthy()
    unmount()

    pool.mockResolvedValue(vazio)
    const empty = renderPage()
    await waitFor(() => {
      expect(screen.getByText('Nenhuma atividade aguardando revisão no pool.')).toBeTruthy()
    })
    empty.unmount()

    pool.mockRejectedValue(new ApiError(403, 'negado'))
    renderPage()
    await waitFor(() => {
      expect(screen.getByText('Pool da CAAF indisponível para esta sessão.')).toBeTruthy()
    })
  })

  it('atribui a mim e oferece aprovar em lote só com presença validada', async () => {
    const inicial = comItem()
    const depois: CaafPool = {
      ...inicial,
      kpis: { poolTotal: 0, atribuidasAMim: 1, prazoMedioDias: 1, aprovadasNoPeriodo: 0 },
      content: [
        {
          ...inicial.content[0],
          noPool: false,
          comigo: true,
          idResponsavel: 'caaf-1',
          responsavelRotulo: 'caaf.dev@ufpr.br',
        },
      ],
    }
    pool.mockResolvedValue(inicial)
    atribuir.mockResolvedValue(depois)
    renderPage()
    await waitFor(() => {
      expect(screen.getByText('Pool CAAF SEPT — oficina com presença')).toBeTruthy()
    })
    expect(screen.getByText('No pool')).toBeTruthy()
    expect(screen.getByText('Atribuir a mim')).toBeTruthy()

    fireEvent.click(screen.getByRole('button', { name: 'Atribuir a mim' }))
    await waitFor(() => {
      expect(atribuir).toHaveBeenCalledWith('f1', 'caaf-1')
    })
    await waitFor(() => {
      expect(screen.getByText('Comigo')).toBeTruthy()
    })
    expect(screen.getByText('Atribuídas a mim')).toBeTruthy()
  })

  it('aprova em lote itens elegíveis e desabilita mistos', async () => {
    const misto: CaafPool = {
      ...vazio,
      kpis: { ...vazio.kpis, poolTotal: 2 },
      content: [
        {
          ...comItem().content[0],
          id: 'f1',
          titulo: 'Com presença',
          elegivelLote: true,
          _links: {
            self: '/formativas/f1',
            'assign-member': '/comissoes/caaf/atribuicoes',
            'batch-approve': '/comissoes/caaf/lote',
          },
        },
        {
          ...comItem().content[0],
          id: 'f2',
          titulo: 'Comprovante',
          origem: 'COMPROVANTE',
          elegivelLote: false,
          _links: {
            self: '/formativas/f2',
            'assign-member': '/comissoes/caaf/atribuicoes',
          },
        },
      ],
    }
    pool.mockResolvedValue(misto)
    aprovarLote.mockResolvedValue({ ...misto, content: [misto.content[1]], kpis: { ...misto.kpis, poolTotal: 1 } })
    renderPage()
    await waitFor(() => {
      expect(screen.getByText('Com presença')).toBeTruthy()
    })

    fireEvent.click(screen.getByRole('checkbox', { name: 'Selecionar formativa Com presença' }))
    fireEvent.click(screen.getByRole('checkbox', { name: 'Selecionar formativa Comprovante' }))
    const aprovar = screen.getByRole('button', { name: 'Aprovar selecionados' }) as HTMLButtonElement
    expect(aprovar.disabled).toBe(true)

    fireEvent.click(screen.getByRole('checkbox', { name: 'Selecionar formativa Comprovante' }))
    expect(aprovar.disabled).toBe(false)
    fireEvent.click(aprovar)
    expect(screen.getByRole('dialog', { name: 'Confirmar aprovação em lote' })).toBeTruthy()
    fireEvent.click(screen.getByRole('button', { name: 'Confirmar' }))
    await waitFor(() => {
      expect(aprovarLote).toHaveBeenCalledWith(['f1'])
    })
  })
})
