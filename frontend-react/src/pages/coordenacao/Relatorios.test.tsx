import { QueryClient, QueryClientProvider } from '@tanstack/react-query'
import { fireEvent, render, screen, waitFor } from '@testing-library/react'
import { MemoryRouter, Route, Routes } from 'react-router-dom'
import { beforeEach, describe, expect, it, vi } from 'vitest'
import { ApiError } from '../../api/client'
import { Relatorios } from './Relatorios'

const obter = vi.fn()

vi.mock('../../api/reports', () => ({
  reportsApi: {
    coordinator: (...args: unknown[]) => obter(...args),
  },
}))

function renderPage(entry = '/coordenacao/relatorios?periodo=2026-2&curso=TADS') {
  const client = new QueryClient({
    defaultOptions: { queries: { retry: false } },
  })
  return render(
    <QueryClientProvider client={client}>
      <MemoryRouter initialEntries={[entry]}>
        <Routes>
          <Route path="/coordenacao/relatorios" element={<Relatorios />} />
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
  },
  kpis: {
    tempoMedioDias: 2.5,
    taxaIndeferimento: 0.35,
    horasValidadas: 120,
    taxaPresenca: 0.8,
    thresholdIndeferimento: 0.2,
  },
  series: {
    evasao: [
      { periodo: '2026/1', ativos: 40, evadidos: 2 },
      { periodo: '2026/2', ativos: 38, evadidos: 5 },
    ],
    formativas: [{ periodo: '2026/2', horasValidadas: 120, aprovadas: 10, indeferidas: 2 }],
    aprovacaoFormativas: [{ periodo: '2026/2', taxaAprovacao: 0.833 }],
  },
  pendencias: [
    { id: 'tcc-1', descricao: 'Banca sem composição - Sistema acadêmico', href: '/tccs/tcc-1' },
  ],
  cargaPorDeliberador: [{ nome: 'Ana', quantidade: 4, tempoMedioDias: 1.2 }],
  _links: {
    self: '/reports/coordinator?periodo=2026-2&curso=TADS',
    'configurar-curso': '/coordenacao/cursos/curso-1/configurar',
    deliberar: '/solicitacoes?to=me',
  },
}

describe('Relatorios (F6.2)', () => {
  beforeEach(() => {
    obter.mockReset()
  })

  it('exibe KPIs, séries e alerta de threshold', async () => {
    obter.mockResolvedValue(payload)
    renderPage()

    await waitFor(() => {
      expect(screen.getByText('Tempo médio')).toBeTruthy()
    })
    expect(obter).toHaveBeenCalledWith({ periodo: '2026-2', curso: 'TADS' })
    expect(screen.getByText('35%')).toBeTruthy()
    expect(screen.getByText('Taxa de indeferimento: 35% (acima do limite 20%)')).toBeTruthy()
    expect(screen.getByRole('heading', { name: 'Evasão por período' })).toBeTruthy()
    expect(screen.getByText(/Maior evasão em 2026\/2/)).toBeTruthy()
    expect(screen.getByText('Banca sem composição - Sistema acadêmico')).toBeTruthy()
    expect(screen.getByRole('link', { name: 'Configurar curso' })).toBeTruthy()
    expect(screen.queryByRole('link', { name: 'Tipos de Solicitação' })).toBeNull()
  })

  it('persiste filtros na URL ao aplicar', async () => {
    obter.mockResolvedValue({
      ...payload,
      filtros: { ...payload.filtros, periodo: '2026-1', periodoRotulo: '2026/1' },
      kpis: { ...payload.kpis, taxaIndeferimento: 0.1 },
      series: { evasao: [], formativas: [], aprovacaoFormativas: [] },
      pendencias: [],
      cargaPorDeliberador: [],
      _links: { self: '/reports/coordinator' },
    })

    renderPage('/coordenacao/relatorios')
    await waitFor(() => expect(obter).toHaveBeenCalled())

    fireEvent.change(screen.getByLabelText('Período'), { target: { value: '2026-1' } })
    fireEvent.change(screen.getByLabelText('Curso'), { target: { value: 'tads' } })
    fireEvent.click(screen.getByRole('button', { name: 'Aplicar' }))

    await waitFor(() => {
      expect(obter).toHaveBeenLastCalledWith({ periodo: '2026-1', curso: 'TADS' })
    })
  })

  it('mostra erro 403 de escopo', async () => {
    obter.mockRejectedValue(new ApiError(403, 'Você não é coordenador deste curso.'))
    renderPage()
    await waitFor(() => {
      expect(
        screen.getByText('Você não tem permissão para ver relatórios deste curso.'),
      ).toBeTruthy()
    })
  })
})
