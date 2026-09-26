import { QueryClient, QueryClientProvider } from '@tanstack/react-query'
import { fireEvent, render, screen, waitFor } from '@testing-library/react'
import { MemoryRouter, Route, Routes } from 'react-router-dom'
import { beforeEach, describe, expect, it, vi } from 'vitest'
import { ApiError } from '../../api/client'
import { Estatisticas } from './Estatisticas'

const obter = vi.fn()

vi.mock('../../api/reports', () => ({
  reportsApi: {
    secretary: (...args: unknown[]) => obter(...args),
  },
}))

function renderPage(entry = '/secretaria/estatisticas?periodo=2026-2&curso=TADS') {
  const client = new QueryClient({
    defaultOptions: { queries: { retry: false } },
  })
  return render(
    <QueryClientProvider client={client}>
      <MemoryRouter initialEntries={[entry]}>
        <Routes>
          <Route path="/secretaria/estatisticas" element={<Estatisticas />} />
        </Routes>
      </MemoryRouter>
    </QueryClientProvider>,
  )
}

const payload = {
  filtros: {
    periodo: '2026-2',
    periodoRotulo: '2026/2',
    curso: 'TADS',
    cursoId: 'curso-1',
    cursoNome: 'TADS',
    cursos: [{ id: 'curso-1', sigla: 'TADS', nome: 'TADS' }],
  },
  solicitacoesPorTipo: [
    { tipoCodigo: 'DECLARACAO_VINCULO', tipoNome: 'Declaração de vínculo', quantidade: 3 },
    { tipoCodigo: 'DECLARACAO_SIMPLES', tipoNome: 'Declaração simples', quantidade: 1 },
  ],
  solicitacoesPorEstado: [
    { estado: 'ABERTA', quantidade: 2 },
    { estado: 'DELIBERADA', quantidade: 2 },
  ],
  presencas: [{ periodo: '2026/2', confirmadas: 8, registradas: 10 }],
  horasFormativas: [{ periodo: '2026/2', horasValidadas: 40 }],
  itensSolicitacao: [
    {
      id: 's-1',
      protocolo: '2026-0001',
      tipoCodigo: 'DECLARACAO_VINCULO',
      tipoNome: 'Declaração de vínculo',
      estado: 'ABERTA',
      createdAt: '2026-08-01T12:00:00Z',
      cursoSigla: 'TADS',
    },
    {
      id: 's-2',
      protocolo: '2026-0002',
      tipoCodigo: 'DECLARACAO_VINCULO',
      tipoNome: 'Declaração de vínculo',
      estado: 'DELIBERADA',
      createdAt: '2026-08-02T12:00:00Z',
      cursoSigla: 'TADS',
    },
    {
      id: 's-3',
      protocolo: '2026-0003',
      tipoCodigo: 'DECLARACAO_SIMPLES',
      tipoNome: 'Declaração simples',
      estado: 'ABERTA',
      createdAt: '2026-08-03T12:00:00Z',
      cursoSigla: 'TADS',
    },
  ],
  _links: {
    self: '/reports/secretary?periodo=2026-2&curso=TADS',
    estatisticas: '/secretaria/estatisticas',
  },
}

describe('Estatisticas (F5.18)', () => {
  beforeEach(() => {
    obter.mockReset()
  })

  it('exibe os 4 gráficos com resumo textual', async () => {
    obter.mockResolvedValue(payload)
    renderPage()

    await waitFor(() => {
      expect(screen.getByRole('heading', { name: 'Solicitações por tipo' })).toBeTruthy()
    })
    expect(obter).toHaveBeenCalledWith({ periodo: '2026-2', curso: 'TADS' })
    expect(screen.getByRole('heading', { name: 'Solicitações por estado' })).toBeTruthy()
    expect(screen.getByRole('heading', { name: 'Presenças confirmadas' })).toBeTruthy()
    expect(screen.getByRole('heading', { name: 'Horas formativas validadas' })).toBeTruthy()
    expect(screen.getByText(/Tipo mais frequente: Declaração de vínculo/)).toBeTruthy()
    expect(screen.getByText(/Presenças confirmadas: 8 de 10/)).toBeTruthy()
  })

  it('persiste filtros na URL ao aplicar', async () => {
    obter.mockResolvedValue({
      ...payload,
      filtros: { ...payload.filtros, periodo: '2026-1', periodoRotulo: '2026/1', curso: null },
      solicitacoesPorTipo: [],
      solicitacoesPorEstado: [],
      itensSolicitacao: [],
    })

    renderPage('/secretaria/estatisticas')
    await waitFor(() => expect(obter).toHaveBeenCalled())

    fireEvent.change(screen.getByLabelText('Período'), { target: { value: '2026-1' } })
    fireEvent.change(screen.getByLabelText('Curso'), { target: { value: 'tads' } })
    fireEvent.click(screen.getByRole('button', { name: 'Aplicar' }))

    await waitFor(() => {
      expect(obter).toHaveBeenLastCalledWith({ periodo: '2026-1', curso: 'TADS' })
    })
  })

  it('atualiza drill-down ao escolher tipo', async () => {
    obter.mockResolvedValue(payload)
    renderPage()

    await waitFor(() => {
      expect(screen.getByRole('button', { name: 'Declaração de vínculo' })).toBeTruthy()
    })
    fireEvent.click(screen.getByRole('button', { name: 'Declaração de vínculo' }))
    expect(screen.getByRole('heading', { name: /Drill-down · Declaração de vínculo/ })).toBeTruthy()
    expect(screen.getByText('2026-0001')).toBeTruthy()
    expect(screen.getByText('2026-0002')).toBeTruthy()
    expect(screen.queryByText('2026-0003')).toBeNull()
  })

  it('mostra erro 403 de escopo', async () => {
    obter.mockRejectedValue(new ApiError(403, 'Curso fora do escopo da sua secretaria.'))
    renderPage()
    await waitFor(() => {
      expect(
        screen.getByText('Você não tem permissão para ver estatísticas destes cursos.'),
      ).toBeTruthy()
    })
  })
})
