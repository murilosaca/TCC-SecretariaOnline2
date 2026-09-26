import { QueryClient, QueryClientProvider } from '@tanstack/react-query'
import { fireEvent, render, screen } from '@testing-library/react'
import { describe, expect, it, vi } from 'vitest'
import { academicoApi } from '../../api/academico'
import { ApiError } from '../../api/client'
import { Cursos } from './Cursos'

const listarCursos = vi.fn()
const buscarUsuarios = vi.fn()

vi.mock('../../api/academico', () => ({
  academicoApi: {
    listarCursos: (...args: unknown[]) => listarCursos(...args),
    criarCurso: vi.fn(),
    atualizarCurso: vi.fn(),
    excluirCurso: vi.fn(),
  },
}))

vi.mock('../../api/admin', () => ({
  iamApi: {
    buscarUsuarios: (...args: unknown[]) => buscarUsuarios(...args),
  },
  adminApi: {},
}))

function renderPage() {
  const client = new QueryClient({
    defaultOptions: { queries: { retry: false } },
  })
  return render(
    <QueryClientProvider client={client}>
      <Cursos />
    </QueryClientProvider>,
  )
}

describe('Cursos', () => {
  it('esconde Novo/Editar/Excluir sem os rels', async () => {
    listarCursos.mockResolvedValue({
      content: [
        {
          id: 'c1',
          nome: 'TADS',
          sigla: 'TADS',
          codigo: 'TADS-SEPT',
          horasFormativasMinimas: 120,
          secretariosIds: [],
          ativo: true,
          _links: { self: '/academico/cursos/c1' },
        },
      ],
      page: { number: 0, size: 20, totalElements: 1, totalPages: 1 },
    })
    buscarUsuarios.mockResolvedValue({
      content: [],
      page: { number: 0, size: 20, totalElements: 0, totalPages: 0 },
    })
    renderPage()
    expect(await screen.findByText('TADS-SEPT')).toBeTruthy()
    expect(screen.queryByText('Novo curso')).toBeNull()
    expect(screen.queryByText('Editar')).toBeNull()
    expect(screen.queryByText('Excluir')).toBeNull()
  })

  it('mostra Novo e ações da linha quando os rels existem', async () => {
    listarCursos.mockResolvedValue({
      content: [
        {
          id: 'c1',
          nome: 'TADS',
          sigla: 'TADS',
          codigo: 'TADS-SEPT',
          horasFormativasMinimas: 120,
          secretariosIds: ['u1'],
          ativo: true,
          _links: {
            self: '/academico/cursos/c1',
            atualizar: '/academico/cursos/c1',
            excluir: '/academico/cursos/c1',
          },
        },
      ],
      page: { number: 0, size: 20, totalElements: 1, totalPages: 1 },
      _links: { criar: '/academico/cursos' },
    })
    buscarUsuarios.mockResolvedValue({
      content: [
        { id: 'u1', nome: 'Sec', emailInstitucional: 'sec@ufpr.br', grr: 'GRR20240005' },
      ],
      page: { number: 0, size: 20, totalElements: 1, totalPages: 1 },
    })
    renderPage()
    expect(await screen.findByText('Novo curso')).toBeTruthy()
    expect(screen.getByText('Editar')).toBeTruthy()
    expect(screen.getByText('Excluir')).toBeTruthy()
    expect(screen.getByText('1')).toBeTruthy()
    expect(screen.getByText('Coordenador')).toBeTruthy()
    expect(screen.getByLabelText('Buscar secretários')).toBeTruthy()
    expect(screen.queryByPlaceholderText('uuid, uuid')).toBeNull()
  })

  it('mostra 403 honesto quando a lista é recusada', async () => {
    listarCursos.mockRejectedValue(new ApiError(403, 'Acesso negado'))
    renderPage()
    expect(await screen.findByText('Você não tem permissão para gerenciar cursos.')).toBeTruthy()
    expect(screen.queryByText('Não foi possível carregar os cursos.')).toBeNull()
  })

  it('mostra o detail do problem+json ao falhar a exclusão', async () => {
    listarCursos.mockResolvedValue({
      content: [
        {
          id: 'c1',
          nome: 'TADS',
          sigla: 'TADS',
          codigo: 'TADS-SEPT',
          horasFormativasMinimas: 120,
          secretariosIds: [],
          ativo: true,
          _links: {
            self: '/academico/cursos/c1',
            excluir: '/academico/cursos/c1',
          },
        },
      ],
      page: { number: 0, size: 20, totalElements: 1, totalPages: 1 },
    })
    buscarUsuarios.mockResolvedValue({
      content: [],
      page: { number: 0, size: 20, totalElements: 0, totalPages: 0 },
    })
    vi.mocked(academicoApi.excluirCurso).mockRejectedValue(
      new ApiError(409, 'Curso possui alunos ou disciplinas vinculados; desvincule antes de excluir.'),
    )
    renderPage()
    fireEvent.click(await screen.findByText('Excluir'))
    expect(
      await screen.findByText('Curso possui alunos ou disciplinas vinculados; desvincule antes de excluir.'),
    ).toBeTruthy()
  })
})
