import { render, screen } from '@testing-library/react'
import { QueryClient, QueryClientProvider } from '@tanstack/react-query'
import { MemoryRouter, Route, Routes } from 'react-router-dom'
import { beforeEach, describe, expect, it, vi } from 'vitest'
import { ApiError } from '../../api/client'
import { eventosApi } from '../../api/eventos'
import type { HostSessao } from '../../models/evento'
import { ProfessorEventoOperacao } from './ProfessorEventoOperacao'

vi.mock('../../api/eventos', () => ({
  eventosApi: {
    sessaoHost: vi.fn(),
    abrirJanelaEntrada: vi.fn(),
    abrirJanelaSaida: vi.fn(),
    renovarQr: vi.fn(),
    encerrar: vi.fn(),
  },
}))

function sessao(parcial: Partial<HostSessao> = {}): HostSessao {
  return {
    eventoId: 'e1',
    titulo: 'Oficina',
    attendanceMode: 'SECRET_DUAL',
    estado: 'EM_ANDAMENTO',
    janelaAtiva: true,
    janelaExpira: null,
    pin: '123456',
    presentes: 1,
    inelegiveis: [],
    ...parcial,
  }
}

function renderPainel() {
  const client = new QueryClient({ defaultOptions: { queries: { retry: false } } })
  return render(
    <QueryClientProvider client={client}>
      <MemoryRouter initialEntries={['/professor/eventos/e1/operacao']}>
        <Routes>
          <Route path="/professor/eventos/:id/operacao" element={<ProfessorEventoOperacao />} />
        </Routes>
      </MemoryRouter>
    </QueryClientProvider>,
  )
}

describe('ProfessorEventoOperacao', () => {
  beforeEach(() => {
    vi.mocked(eventosApi.sessaoHost).mockReset()
  })

  it('mostra carregando, lista vazia e nomes inelegíveis', async () => {
    let resolveSessao: (valor: HostSessao) => void = () => undefined
    vi.mocked(eventosApi.sessaoHost).mockReturnValue(
      new Promise((resolve) => {
        resolveSessao = resolve
      }),
    )
    renderPainel()
    expect(screen.getByText('Carregando painel…').textContent).toContain('Carregando painel')

    resolveSessao(sessao())
    expect((await screen.findByText('Nenhum aluno inelegível.')).textContent).toContain('Nenhum aluno inelegível')
    expect(screen.getByText('Inelegíveis: 0').textContent).toContain('Inelegíveis: 0')

    vi.mocked(eventosApi.sessaoHost).mockResolvedValue(sessao({ inelegiveis: ['Bia', 'Sem conta'], presentes: 2 }))
    renderPainel()
    expect((await screen.findByText('Bia')).textContent).toContain('Bia')
    expect(screen.getByText('Sem conta').textContent).toContain('Sem conta')
    expect(screen.getByText('Inelegíveis: 2').textContent).toContain('Inelegíveis: 2')
  })

  it('mostra erro ao falhar a sessão', async () => {
    vi.mocked(eventosApi.sessaoHost).mockRejectedValue(new ApiError(500, 'falha'))
    renderPainel()
    expect((await screen.findByRole('alert')).textContent).toContain('Não foi possível carregar a sessão de operação.')
  })
})
