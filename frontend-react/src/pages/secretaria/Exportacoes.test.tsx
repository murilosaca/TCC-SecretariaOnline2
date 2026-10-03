import { fireEvent, render, screen, waitFor } from '@testing-library/react'
import { QueryClient, QueryClientProvider } from '@tanstack/react-query'
import { MemoryRouter } from 'react-router-dom'
import { beforeEach, describe, expect, it, vi } from 'vitest'
import { Exportacoes } from './Exportacoes'

const listar = vi.fn()
const solicitar = vi.fn()
const download = vi.fn()

vi.mock('../../api/exportacoes', () => ({
  exportacoesApi: {
    listar: (...args: unknown[]) => listar(...args),
    solicitar: (...args: unknown[]) => solicitar(...args),
    obter: vi.fn(),
    download: (...args: unknown[]) => download(...args),
  },
}))

function renderTela() {
  const client = new QueryClient({ defaultOptions: { queries: { retry: false } } })
  return render(
    <QueryClientProvider client={client}>
      <MemoryRouter>
        <Exportacoes />
      </MemoryRouter>
    </QueryClientProvider>,
  )
}

describe('Exportacoes', () => {
  beforeEach(() => {
    listar.mockReset()
    solicitar.mockReset()
    download.mockReset()
  })

  it('solicita o job e só baixa quando o link existe', async () => {
    listar.mockResolvedValue({
      kinds: [{ kind: 'alunos', titulo: 'Alunos' }],
      content: [
        {
          id: 'e1',
          kind: 'alunos',
          titulo: 'Alunos',
          status: 'PROCESSANDO',
          nomeArquivo: null,
          expiresAt: null,
          mensagem: null,
          createdAt: '2026-10-03T00:00:00Z',
          _links: { self: '/exportacoes/e1' },
        },
      ],
      _links: { self: '/exportacoes' },
    })
    solicitar.mockResolvedValue({
      id: 'e2',
      kind: 'alunos',
      titulo: 'Alunos',
      status: 'PROCESSANDO',
      nomeArquivo: null,
      expiresAt: null,
      mensagem: null,
      createdAt: '2026-10-03T00:00:00Z',
      _links: { self: '/exportacoes/e2' },
    })
    renderTela()
    fireEvent.click(await screen.findByRole('button', { name: 'Gerar' }))
    await waitFor(() => expect(solicitar).toHaveBeenCalledWith('alunos'))
    expect(download).not.toHaveBeenCalled()
    expect(screen.queryByRole('button', { name: 'Baixar' })).toBeNull()
    expect(await screen.findByText('processando')).toBeTruthy()
  })
})
