import { fireEvent, render, screen, waitFor } from '@testing-library/react'
import { QueryClient, QueryClientProvider } from '@tanstack/react-query'
import { MemoryRouter } from 'react-router-dom'
import { beforeEach, describe, expect, it, vi } from 'vitest'
import { academicoApi } from '../../api/academico'
import { egressosApi } from '../../api/egressos'
import { Egressos } from './Egressos'

vi.mock('../../api/egressos', () => ({
  egressosApi: {
    listar: vi.fn(),
    exportarCsv: vi.fn(),
  },
}))

vi.mock('../../api/academico', () => ({
  academicoApi: {
    listarCursos: vi.fn(),
  },
}))

function renderPage() {
  const client = new QueryClient({ defaultOptions: { queries: { retry: false } } })
  return render(
    <QueryClientProvider client={client}>
      <MemoryRouter initialEntries={['/secretaria/egressos']}>
        <Egressos />
      </MemoryRouter>
    </QueryClientProvider>,
  )
}

describe('Egressos', () => {
  beforeEach(() => {
    vi.mocked(egressosApi.listar).mockReset()
    vi.mocked(egressosApi.exportarCsv).mockReset()
    vi.mocked(academicoApi.listarCursos).mockReset()
    vi.mocked(academicoApi.listarCursos).mockResolvedValue({
      content: [{ id: 'c1', nome: 'TADS', sigla: 'TADS', codigo: 'TADS', horasFormativasMinimas: 120, ativo: true }],
      page: { number: 0, size: 100, totalElements: 1, totalPages: 1 },
    })
    vi.stubGlobal('URL', {
      createObjectURL: vi.fn(() => 'blob:egressos'),
      revokeObjectURL: vi.fn(),
    })
  })

  it('lista sem Novo e exporta CSV síncrono', async () => {
    vi.mocked(egressosApi.listar).mockResolvedValue({
      content: [
        {
          alunoId: 'a1',
          nome: 'Ana Colada',
          cursoId: 'c1',
          cursoSigla: 'TADS',
          dataColacao: '2026-07-15T12:00:00Z',
          anoColacao: 2026,
          situacaoDiploma: 'PENDENTE',
          numeroDiploma: 'UFPR-1',
          _links: {},
        },
      ],
      page: { number: 0, size: 20, totalElements: 1, totalPages: 1 },
      _links: {},
    })
    vi.mocked(egressosApi.exportarCsv).mockResolvedValue(new Blob(['csv'], { type: 'text/csv' }))
    renderPage()
    expect(await screen.findByText('Ana Colada')).toBeTruthy()
    expect(screen.queryByRole('link', { name: 'Novo' })).toBeNull()
    expect(screen.queryByRole('button', { name: 'Novo egresso' })).toBeNull()
    expect(screen.queryByText(/100 primeiros/)).toBeNull()
    fireEvent.click(screen.getByRole('button', { name: 'Exportar CSV' }))
    await waitFor(() => {
      expect(egressosApi.exportarCsv).toHaveBeenCalledWith({
        cursoId: undefined,
        ano: undefined,
        situacao: undefined,
        page: 0,
        size: 100,
      })
    })
    expect(egressosApi.listar).toHaveBeenCalledWith(
      expect.objectContaining({ page: 0, size: 20 }),
    )
    expect(screen.queryByText(/job/i)).toBeNull()
  })

  it('avisa que o CSV traz só os 100 primeiros quando o total passa de 100', async () => {
    vi.mocked(egressosApi.listar).mockResolvedValue({
      content: [
        {
          alunoId: 'a1',
          nome: 'Ana Colada',
          cursoId: 'c1',
          cursoSigla: 'TADS',
          dataColacao: '2026-07-15T12:00:00Z',
          anoColacao: 2026,
          situacaoDiploma: 'PENDENTE',
          numeroDiploma: 'UFPR-1',
          _links: {},
        },
      ],
      page: { number: 0, size: 20, totalElements: 101, totalPages: 6 },
      _links: {},
    })
    renderPage()
    expect(await screen.findByText('O CSV traz só os 100 primeiros do filtro.')).toBeTruthy()
    expect(screen.getByText('Há mais nesta lista.')).toBeTruthy()
    expect(screen.getByText('Página 1 de 6')).toBeTruthy()
    expect(screen.queryByRole('button', { name: 'Novo egresso' })).toBeNull()
  })
})
