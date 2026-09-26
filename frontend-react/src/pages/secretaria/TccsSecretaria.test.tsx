import { QueryClient, QueryClientProvider } from '@tanstack/react-query'
import { render, screen, waitFor } from '@testing-library/react'
import { beforeEach, describe, expect, it, vi } from 'vitest'
import { academicoApi } from '../../api/academico'
import { ApiError } from '../../api/client'
import { tccsApi } from '../../api/tccs'
import { TccsSecretaria } from './TccsSecretaria'

vi.mock('../../api/tccs', () => ({
  tccsApi: {
    listarDoEscopo: vi.fn(),
    criar: vi.fn(),
    atualizar: vi.fn(),
  },
}))

vi.mock('../../api/academico', () => ({
  academicoApi: {
    listarAlunos: vi.fn(),
  },
}))

vi.mock('../../api/admin', () => ({
  iamApi: {
    buscarUsuarios: vi.fn().mockResolvedValue({
      content: [],
      page: { number: 0, size: 20, totalElements: 0, totalPages: 0 },
      _links: {},
    }),
  },
}))

const paginaVazia = {
  content: [],
  page: { number: 0, size: 20, totalElements: 0, totalPages: 0 },
  _links: {},
}

function renderPage() {
  const client = new QueryClient({ defaultOptions: { queries: { retry: false } } })
  return render(
    <QueryClientProvider client={client}>
      <TccsSecretaria />
    </QueryClientProvider>,
  )
}

describe('TccsSecretaria', () => {
  beforeEach(() => {
    vi.mocked(tccsApi.listarDoEscopo).mockReset()
    vi.mocked(academicoApi.listarAlunos).mockReset()
    vi.mocked(academicoApi.listarAlunos).mockResolvedValue({
      content: [
        {
          id: 'aluno-1',
          nome: 'Novo Dev',
          grr: 'GRR20240002',
          emailInstitucional: 'novo.dev@ufpr.br',
          idCurso: 'curso-1',
          situacao: 'MATRICULADO',
          ativo: true,
        },
      ],
      page: { number: 0, size: 100, totalElements: 1, totalPages: 1 },
      _links: {},
    })
  })

  it('sem o rel novo não oferece formulário nem pede UUID', async () => {
    vi.mocked(tccsApi.listarDoEscopo).mockResolvedValue({
      ...paginaVazia,
      content: [
        {
          id: 't1',
          idAluno: 'aluno-1',
          alunoNome: 'Novo Dev',
          idCurso: 'curso-1',
          titulo: 'TCC Concluído',
          situacao: 'CONCLUIDO',
          estado: 'APROVADO',
          dataDefesa: '2026-11-12',
          dataEntrega: '2026-10-03',
          orientadorRotulo: 'prof@ufpr.br',
          membros: [
            { id: 'm1', idUsuario: 'prof-1', rotulo: 'prof@ufpr.br', papel: 'ORIENTADOR' },
          ],
          avaliacoes: [],
          _links: { self: '/tccs/t1' },
        },
      ],
    })
    renderPage()
    await waitFor(() => {
      expect(screen.getByText('TCC Concluído')).toBeTruthy()
    })
    expect(screen.queryByRole('button', { name: 'Salvar' })).toBeNull()
    expect(screen.queryByRole('button', { name: 'Editar' })).toBeNull()
    expect(screen.queryByRole('combobox', { name: 'Aluno' })).toBeNull()
    expect(screen.queryByPlaceholderText(/uuid/i)).toBeNull()
    expect(screen.getByText('prof@ufpr.br')).toBeTruthy()
  })

  it('com novo lista alunos no select e edita só quem tem o rel', async () => {
    vi.mocked(tccsApi.listarDoEscopo).mockResolvedValue({
      ...paginaVazia,
      _links: { novo: '/tccs' },
      content: [
        {
          id: 't1',
          idAluno: 'aluno-1',
          alunoNome: 'Novo Dev',
          idCurso: 'curso-1',
          titulo: 'TCC Ativo',
          situacao: 'ATIVO',
          estado: 'EM_ELABORACAO',
          dataDefesa: '2026-11-12',
          dataEntrega: '2026-10-03',
          orientadorRotulo: 'prof@ufpr.br',
          membros: [
            { id: 'm1', idUsuario: 'prof-1', rotulo: 'prof@ufpr.br', papel: 'ORIENTADOR' },
          ],
          avaliacoes: [],
          _links: { self: '/tccs/t1', editar: '/tccs/t1' },
        },
      ],
    })
    renderPage()
    await waitFor(() => {
      expect(screen.getByRole('option', { name: 'GRR20240002 — Novo Dev' })).toBeTruthy()
    })
    expect(screen.getByRole('button', { name: 'Editar' })).toBeTruthy()
    expect(screen.getByLabelText('Buscar orientador')).toBeTruthy()
    expect(screen.queryByPlaceholderText(/uuid/i)).toBeNull()
  })

  it('mostra vazio, erro e 403', async () => {
    vi.mocked(tccsApi.listarDoEscopo).mockResolvedValue({
      ...paginaVazia,
      _links: { novo: '/tccs' },
    })
    const { unmount } = renderPage()
    await waitFor(() => {
      expect(screen.getByText('Nenhum TCC registrado nos cursos da secretaria.')).toBeTruthy()
    })
    unmount()

    vi.mocked(tccsApi.listarDoEscopo).mockRejectedValue(new Error('falha'))
    const erro = renderPage()
    await waitFor(() => {
      expect(screen.getByText(/Não foi possível carregar os TCCs/)).toBeTruthy()
    })
    expect(screen.getByRole('button', { name: 'Tentar de novo' })).toBeTruthy()
    erro.unmount()

    vi.mocked(tccsApi.listarDoEscopo).mockRejectedValue(new ApiError(403, 'sem permissão'))
    renderPage()
    await waitFor(() => {
      expect(screen.getByText('Você não tem permissão para registrar TCCs.')).toBeTruthy()
    })
    expect(screen.queryByRole('button', { name: 'Salvar' })).toBeNull()
  })
})
