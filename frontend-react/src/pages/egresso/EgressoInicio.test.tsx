import { QueryClient, QueryClientProvider } from '@tanstack/react-query'
import { render, screen, waitFor } from '@testing-library/react'
import { MemoryRouter } from 'react-router-dom'
import { beforeEach, describe, expect, it, vi } from 'vitest'
import { ApiError } from '../../api/client'
import { EgressoInicio } from './EgressoInicio'

const painel = vi.fn()

vi.mock('../../api/egressos', () => ({
  egressosApi: {
    painel: (...args: unknown[]) => painel(...args),
    baixar: vi.fn(),
  },
}))

function renderPage() {
  const client = new QueryClient({ defaultOptions: { queries: { retry: false } } })
  return render(
    <QueryClientProvider client={client}>
      <MemoryRouter>
        <EgressoInicio />
      </MemoryRouter>
    </QueryClientProvider>,
  )
}

const vazio = {
  nome: 'Egresso Dev',
  curso: 'Análise e Desenvolvimento de Sistemas',
  concluidoEm: null,
  kpis: { horasFormativasValidadas: 0, certificadosEmitidos: 0, situacaoDiploma: null },
  diploma: null,
  colacao: null,
  certificados: [],
  _links: { self: '/egressos/me' },
}

describe('EgressoInicio', () => {
  beforeEach(() => {
    painel.mockReset()
  })

  it('mostra loading, painel read-only e empty de certificados', async () => {
    painel.mockReturnValue(new Promise(() => undefined))
    const { unmount } = renderPage()
    expect(document.querySelector('[aria-busy="true"]')).toBeTruthy()
    unmount()

    painel.mockResolvedValue(vazio)
    renderPage()
    await waitFor(() => {
      expect(screen.getByRole('heading', { level: 1, name: 'Olá, Egresso Dev' })).toBeTruthy()
    })
    expect(screen.getByText('Painel somente leitura.')).toBeTruthy()
    expect(screen.getByText('Data de conclusão indisponível')).toBeTruthy()
    expect(screen.getByText('O registro do diploma ainda não está disponível.')).toBeTruthy()
    expect(screen.getByText('Nenhum certificado emitido durante o curso.')).toBeTruthy()
    expect(screen.getByText('Dados de colação indisponíveis até o registro do diploma.')).toBeTruthy()
    expect(screen.getByText('Indisponível')).toBeTruthy()
    expect(screen.queryByText('Nova solicitação')).toBeNull()
    expect(screen.queryByText('Nova formativa')).toBeNull()
    expect(screen.getByRole('heading', { level: 2, name: 'Diploma' })).toBeTruthy()
    expect(screen.getByRole('heading', { level: 2, name: 'Certificados' })).toBeTruthy()
    expect(screen.getByRole('heading', { level: 2, name: 'Colação' })).toBeTruthy()
  })

  it('mostra Reemitir PDF só quando _links.reemitir existe', async () => {
    painel.mockResolvedValue({
      ...vazio,
      kpis: { ...vazio.kpis, certificadosEmitidos: 2 },
      certificados: [
        {
          id: 'c1',
          titulo: 'Oficina',
          tipo: 'EVENTO',
          emitidoEm: '2026-06-01T12:00:00Z',
          hashSha256: 'a'.repeat(64),
          _links: { reemitir: '/egressos/me/certificados/c1/reemissao' },
        },
        {
          id: 'c2',
          titulo: 'Sem reemissão',
          tipo: 'EVENTO',
          emitidoEm: '2026-06-01T12:00:00Z',
          hashSha256: 'b'.repeat(64),
          _links: {},
        },
      ],
    })
    renderPage()
    await waitFor(() => {
      expect(screen.getByText('Oficina')).toBeTruthy()
    })
    const botao = screen.getByRole('button', { name: 'Baixar certificado de Oficina' })
    expect(botao.className).toContain('secondary')
    expect(screen.queryByRole('button', { name: 'Baixar certificado de Sem reemissão' })).toBeNull()
    expect(screen.queryByText('Nova solicitação')).toBeNull()
  })

  it('mostra erro com retry e 403 sem CTA de criação', async () => {
    painel.mockRejectedValue(new Error('falha'))
    const { unmount } = renderPage()
    await waitFor(() => {
      expect(screen.getByText(/Não foi possível carregar o painel/)).toBeTruthy()
    })
    expect(screen.getByRole('button', { name: 'Tentar de novo' })).toBeTruthy()
    unmount()

    painel.mockRejectedValue(new ApiError(403, 'negado'))
    renderPage()
    await waitFor(() => {
      expect(screen.getByText('Você não tem permissão para acessar este recurso.')).toBeTruthy()
    })
    expect(screen.queryByText('Nova solicitação')).toBeNull()
  })
})
