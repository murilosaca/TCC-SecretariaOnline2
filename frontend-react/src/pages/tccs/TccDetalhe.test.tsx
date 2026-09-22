import { QueryClient, QueryClientProvider } from '@tanstack/react-query'
import { render, screen, waitFor } from '@testing-library/react'
import { MemoryRouter, Route, Routes } from 'react-router-dom'
import { beforeEach, describe, expect, it, vi } from 'vitest'
import { TccDetalhe } from './TccDetalhe'

const obter = vi.fn()

vi.mock('../../api/tccs', () => ({
  tccsApi: {
    obter: (...args: unknown[]) => obter(...args),
    enviarVersaoFinal: vi.fn(),
    avaliar: vi.fn(),
    baixar: vi.fn(),
  },
}))

vi.mock('../../auth/AuthContext', () => ({
  useAuth: () => ({ links: { tccs: '/tccs' } }),
}))

function renderDetalhe() {
  const client = new QueryClient({ defaultOptions: { queries: { retry: false } } })
  return render(
    <QueryClientProvider client={client}>
      <MemoryRouter initialEntries={['/tccs/t1']}>
        <Routes>
          <Route path="/tccs/:id" element={<TccDetalhe />} />
        </Routes>
      </MemoryRouter>
    </QueryClientProvider>,
  )
}

function isoDaqui(dias: number): string {
  const data = new Date()
  data.setDate(data.getDate() + dias)
  const mes = String(data.getMonth() + 1).padStart(2, '0')
  const dia = String(data.getDate()).padStart(2, '0')
  return `${data.getFullYear()}-${mes}-${dia}`
}

const base = {
  id: 't1',
  titulo: 'Plataforma SEPT',
  situacao: 'ATIVO',
  estado: 'EM_ELABORACAO',
  dataDefesa: '2026-11-12',
  dataEntrega: isoDaqui(40),
  orientadorRotulo: 'professor.dev@ufpr.br',
  membros: [{ id: 'm1', idUsuario: 'u1', rotulo: 'professor.dev@ufpr.br', papel: 'ORIENTADOR' }],
  avaliacoes: [],
  _links: { self: '/tccs/t1' },
}

describe('TccDetalhe', () => {
  beforeEach(() => {
    obter.mockReset()
  })

  it('mostra upload só com upload-final e esconde avaliação', async () => {
    obter.mockResolvedValue({
      ...base,
      _links: { self: '/tccs/t1', 'upload-final': '/tccs/t1/versao-final' },
    })
    renderDetalhe()
    await waitFor(() => {
      expect(screen.getByText('Enviar versão final')).toBeTruthy()
    })
    expect(screen.queryByText('Indeferir')).toBeNull()
    expect(screen.queryByText('Prazo próximo')).toBeNull()
    expect(screen.getByText('Orientador')).toBeTruthy()
  })

  it('mostra avaliação e prazo próximo quando os dados permitem', async () => {
    obter.mockResolvedValue({
      ...base,
      estado: 'SUBMETIDO',
      dataEntrega: isoDaqui(5),
      nomeArquivo: 'tcc.pdf',
      _links: {
        self: '/tccs/t1',
        avaliar: '/tccs/t1/avaliacoes',
        download: '/tccs/t1/arquivo',
      },
    })
    renderDetalhe()
    await waitFor(() => {
      expect(screen.getByText('Indeferir')).toBeTruthy()
    })
    expect(screen.getByText('Aprovar')).toBeTruthy()
    expect(screen.getByText('Solicitar correções')).toBeTruthy()
    expect(screen.getByText('Baixar TCC')).toBeTruthy()
    expect(screen.getByText('Prazo próximo')).toBeTruthy()
    expect(screen.queryByText('Enviar versão final')).toBeNull()
  })
})