import { QueryClient, QueryClientProvider } from '@tanstack/react-query'
import { render, screen, waitFor } from '@testing-library/react'
import { beforeEach, describe, expect, it, vi } from 'vitest'
import { academicoApi } from '../../api/academico'
import { ApiError } from '../../api/client'
import { estagiosApi } from '../../api/estagios'
import { EstagiosSecretaria } from './EstagiosSecretaria'

vi.mock('../../api/estagios', () => ({
  estagiosApi: {
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

const paginaVazia = {
  content: [],
  page: { number: 0, size: 20, totalElements: 0, totalPages: 0 },
  _links: {},
}

function renderPage() {
  const client = new QueryClient({ defaultOptions: { queries: { retry: false } } })
  return render(
    <QueryClientProvider client={client}>
      <EstagiosSecretaria />
    </QueryClientProvider>,
  )
}

describe('EstagiosSecretaria', () => {
  beforeEach(() => {
    vi.mocked(estagiosApi.listarDoEscopo).mockReset()
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
    vi.mocked(estagiosApi.listarDoEscopo).mockResolvedValue({
      ...paginaVazia,
      content: [
        {
          id: 'e1',
          idAluno: 'aluno-1',
          alunoNome: 'Novo Dev',
          idCurso: 'curso-1',
          empresa: 'Empresa Fictícia',
          supervisor: 'Ana',
          inicio: '2026-04-01',
          fim: '2026-10-30',
          situacao: 'CONCLUIDO',
          documentos: [],
          pareceres: [],
          _links: { self: '/estagios/e1' },
        },
      ],
    })
    renderPage()
    await waitFor(() => {
      expect(screen.getByText('Empresa Fictícia')).toBeTruthy()
    })
    expect(screen.queryByRole('button', { name: 'Salvar' })).toBeNull()
    expect(screen.queryByRole('button', { name: 'Editar' })).toBeNull()
    expect(screen.queryByRole('combobox', { name: 'Aluno' })).toBeNull()
    expect(screen.queryByPlaceholderText(/uuid/i)).toBeNull()
    expect(screen.getByText('Sem orientador')).toBeTruthy()
  })

  it('com novo lista alunos no select e edita só quem tem o rel', async () => {
    vi.mocked(estagiosApi.listarDoEscopo).mockResolvedValue({
      ...paginaVazia,
      _links: { novo: '/estagios' },
      content: [
        {
          id: 'e1',
          idAluno: 'aluno-1',
          alunoNome: 'Novo Dev',
          idCurso: 'curso-1',
          empresa: 'Empresa Nova',
          supervisor: 'Ana',
          inicio: '2026-04-01',
          fim: '2026-10-30',
          situacao: 'ATIVO',
          documentos: [],
          pareceres: [],
          _links: { self: '/estagios/e1', editar: '/estagios/e1' },
        },
      ],
    })
    renderPage()
    await waitFor(() => {
      expect(screen.getByRole('option', { name: 'GRR20240002 — Novo Dev' })).toBeTruthy()
    })
    expect(screen.getByRole('button', { name: 'Editar' })).toBeTruthy()
    expect(screen.queryByPlaceholderText(/uuid/i)).toBeNull()
    expect(screen.getByText('Sem orientador')).toBeTruthy()
  })

  it('mostra vazio, erro e 403', async () => {
    vi.mocked(estagiosApi.listarDoEscopo).mockResolvedValue({
      ...paginaVazia,
      _links: { novo: '/estagios' },
    })
    const { unmount } = renderPage()
    await waitFor(() => {
      expect(screen.getByText('Nenhum estágio registrado nos cursos da secretaria.')).toBeTruthy()
    })
    unmount()

    vi.mocked(estagiosApi.listarDoEscopo).mockRejectedValue(new Error('falha'))
    const erro = renderPage()
    await waitFor(() => {
      expect(screen.getByText(/Não foi possível carregar os estágios/)).toBeTruthy()
    })
    expect(screen.getByRole('button', { name: 'Tentar de novo' })).toBeTruthy()
    erro.unmount()

    vi.mocked(estagiosApi.listarDoEscopo).mockRejectedValue(new ApiError(403, 'sem permissão'))
    renderPage()
    await waitFor(() => {
      expect(screen.getByText('Você não tem permissão para registrar estágios.')).toBeTruthy()
    })
    expect(screen.queryByRole('button', { name: 'Salvar' })).toBeNull()
  })
})
