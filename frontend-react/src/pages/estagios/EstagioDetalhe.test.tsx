import { QueryClient, QueryClientProvider } from '@tanstack/react-query'
import { fireEvent, render, screen, waitFor } from '@testing-library/react'
import { MemoryRouter, Route, Routes } from 'react-router-dom'
import { beforeEach, describe, expect, it, vi } from 'vitest'
import { ApiError } from '../../api/client'
import { EstagioDetalhe } from './EstagioDetalhe'

const obter = vi.fn()

vi.mock('../../api/estagios', () => ({
  estagiosApi: {
    obter: (...args: unknown[]) => obter(...args),
    enviarDocumento: vi.fn(),
    parecer: vi.fn(),
    encerrar: vi.fn(),
    baixar: vi.fn(),
  },
}))

vi.mock('../../auth/AuthContext', () => ({
  useAuth: () => ({ links: { estagios: '/estagios' } }),
}))

function renderDetalhe() {
  const client = new QueryClient({ defaultOptions: { queries: { retry: false } } })
  return render(
    <QueryClientProvider client={client}>
      <MemoryRouter initialEntries={['/estagios/e1']}>
        <Routes>
          <Route path="/estagios/:id" element={<EstagioDetalhe />} />
        </Routes>
      </MemoryRouter>
    </QueryClientProvider>,
  )
}

const base = {
  id: 'e1',
  idAluno: 'a1',
  alunoNome: 'Aluno Dev',
  idCurso: 'c1',
  idOrientador: 'p1',
  orientadorRotulo: 'professor.dev@ufpr.br',
  empresa: 'Empresa Fictícia SEPT',
  supervisor: 'Carla Supervisora',
  inicio: '2026-03-02',
  fim: '2026-11-30',
  situacao: 'ATIVO',
  documentos: [
    {
      id: 'd1',
      tipo: 'TCE',
      obrigatorio: true,
      estado: 'PENDENTE',
      _links: { upload: '/estagios/e1/documentos' },
    },
    {
      id: 'd2',
      tipo: 'RELATORIO_FINAL',
      obrigatorio: true,
      estado: 'APROVADO',
      nomeArquivo: 'relatorio.pdf',
      _links: {},
    },
  ],
  pareceres: [],
  _links: { self: '/estagios/e1' },
}

describe('EstagioDetalhe', () => {
  beforeEach(() => {
    obter.mockReset()
  })

  it('mostra loading e erro', async () => {
    obter.mockReturnValue(new Promise(() => undefined))
    const { unmount } = renderDetalhe()
    expect(screen.getByText('Carregando estágio…')).toBeTruthy()
    unmount()

    obter.mockRejectedValue(new ApiError(404, 'ausente'))
    renderDetalhe()
    await waitFor(() => {
      expect(screen.getByText(/Estágio não encontrado ou indisponível/)).toBeTruthy()
    })
  })

  it('mostra cards de documento e tabela de pareceres sem abas', async () => {
    obter.mockResolvedValue(base)
    const { unmount } = renderDetalhe()
    await waitFor(() => {
      expect(screen.getByRole('heading', { name: 'TCE' })).toBeTruthy()
    })
    expect(screen.getByRole('heading', { name: 'Detalhes' })).toBeTruthy()
    expect(screen.getByRole('heading', { name: 'Relatório final' })).toBeTruthy()
    expect(screen.getByRole('heading', { name: 'Pareceres' })).toBeTruthy()
    expect(screen.queryByRole('heading', { name: 'Documentos' })).toBeNull()
    expect(screen.queryByRole('tab', { name: 'Documentos' })).toBeNull()
    expect(screen.getAllByRole('button', { name: 'Enviar' })).toHaveLength(1)
    expect(screen.getByText('Aguarde')).toBeTruthy()
    expect(screen.getByText(/Envie o PDF deste documento/)).toBeTruthy()
    expect(screen.queryByRole('button', { name: 'Arquivar estágio' })).toBeNull()
    expect(screen.queryByRole('button', { name: 'Aprovar' })).toBeNull()
    expect(screen.getByText('Nenhum parecer registrado ainda.')).toBeTruthy()
    expect(screen.getByRole('columnheader', { name: 'Documento' })).toBeTruthy()
    unmount()

    obter.mockResolvedValue({
      ...base,
      _links: { self: '/estagios/e1', arquivar: '/estagios/e1/encerrar' },
      documentos: [
        {
          ...base.documentos[1],
          estado: 'AGUARDANDO_PARECER',
          _links: {
            download: '/estagios/e1/documentos/d2/arquivo',
            revisar: '/estagios/e1',
            aprovar: '/estagios/e1/documentos/d2/parecer',
            reprovar: '/estagios/e1/documentos/d2/parecer',
          },
        },
      ],
    })
    renderDetalhe()
    await waitFor(() => {
      expect(screen.getByRole('button', { name: 'Aprovar' })).toBeTruthy()
    })
    expect(screen.getByRole('button', { name: 'Reprovar' })).toBeTruthy()
    expect(screen.getByRole('button', { name: 'Arquivar estágio' })).toBeTruthy()
    expect(screen.getByRole('button', { name: 'Visualizar' })).toBeTruthy()
    expect(screen.queryByRole('button', { name: 'Enviar' })).toBeNull()
  })
})
