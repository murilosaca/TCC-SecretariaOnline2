import { fireEvent, render, screen, waitFor } from '@testing-library/react'
import { QueryClient, QueryClientProvider } from '@tanstack/react-query'
import { MemoryRouter } from 'react-router-dom'
import { beforeEach, describe, expect, it, vi } from 'vitest'
import { atendimentosApi } from '../../api/atendimentos'
import { MeusAtendimentos } from './MeusAtendimentos'

vi.mock('../../api/atendimentos', () => ({
  atendimentosApi: {
    listarMeus: vi.fn(),
    darCiencia: vi.fn(),
  },
}))

function renderPage(entry = '/meus-atendimentos') {
  const client = new QueryClient({ defaultOptions: { queries: { retry: false } } })
  return render(
    <QueryClientProvider client={client}>
      <MemoryRouter initialEntries={[entry]}>
        <MeusAtendimentos />
      </MemoryRouter>
    </QueryClientProvider>,
  )
}

describe('MeusAtendimentos', () => {
  beforeEach(() => {
    vi.mocked(atendimentosApi.listarMeus).mockReset()
    vi.mocked(atendimentosApi.darCiencia).mockReset()
  })

  it('ciência só aparece com _links.acknowledge', async () => {
    vi.mocked(atendimentosApi.listarMeus).mockResolvedValue({
      content: [
        {
          id: 'a1',
          alunoId: 'al1',
          categoriaId: 'c1',
          assunto: 'Com link',
          resposta: 'ok',
          estado: 'PENDENTE_CIENCIA',
          registradoEm: '2026-10-01T12:00:00Z',
          temAnexo: false,
          _links: { acknowledge: '/atendimentos/a1/acknowledge' },
        },
        {
          id: 'a2',
          alunoId: 'al1',
          categoriaId: 'c1',
          assunto: 'Sem link',
          resposta: 'ok',
          estado: 'PENDENTE_CIENCIA',
          registradoEm: '2026-10-01T12:00:00Z',
          temAnexo: false,
          _links: {},
        },
      ],
      page: { number: 0, size: 20, totalElements: 2, totalPages: 1 },
    })
    vi.mocked(atendimentosApi.darCiencia).mockResolvedValue({
      id: 'a1',
      alunoId: 'al1',
      categoriaId: 'c1',
      assunto: 'Com link',
      resposta: 'ok',
      estado: 'CIENCIA_DADA',
      registradoEm: '2026-10-01T12:00:00Z',
      temAnexo: false,
      _links: {},
    })
    renderPage()
    expect(await screen.findByText('Com link')).toBeTruthy()
    const botoes = screen.getAllByRole('button', { name: 'Estou ciente' })
    expect(botoes).toHaveLength(1)
    fireEvent.click(botoes[0])
    await waitFor(() => {
      expect(atendimentosApi.darCiencia).toHaveBeenCalledWith('/atendimentos/a1/acknowledge')
    })
  })

  it('filtro pendente vazio mostra a mensagem institucional', async () => {
    vi.mocked(atendimentosApi.listarMeus).mockResolvedValue({
      content: [],
      page: { number: 0, size: 20, totalElements: 0, totalPages: 0 },
    })
    renderPage('/meus-atendimentos?status=PENDENTE_CIENCIA')
    expect(await screen.findByText('Nenhum atendimento pendente.')).toBeTruthy()
    expect(atendimentosApi.listarMeus).toHaveBeenCalledWith('PENDENTE_CIENCIA')
  })
})
