import { QueryClient, QueryClientProvider } from '@tanstack/react-query'
import { fireEvent, render, screen } from '@testing-library/react'
import { describe, expect, it, vi } from 'vitest'
import { ApiError } from '../../api/client'
import { Alunos } from './Alunos'

const listarCursos = vi.fn()
const listarAlunos = vi.fn()
const opcoesMatricula = vi.fn()
const matricularAluno = vi.fn()

vi.mock('../../api/academico', () => ({
  academicoApi: {
    listarCursos: (...args: unknown[]) => listarCursos(...args),
    listarAlunos: (...args: unknown[]) => listarAlunos(...args),
    criarAluno: vi.fn(),
    atualizarAluno: vi.fn(),
    excluirAluno: vi.fn(),
    resetSenhaAluno: vi.fn(),
    opcoesMatricula: (...args: unknown[]) => opcoesMatricula(...args),
    matricularAluno: (...args: unknown[]) => matricularAluno(...args),
  },
}))

function renderPage() {
  const client = new QueryClient({
    defaultOptions: { queries: { retry: false } },
  })
  return render(
    <QueryClientProvider client={client}>
      <Alunos />
    </QueryClientProvider>,
  )
}

describe('Alunos', () => {
  it('mostra erro honesto e desabilita o form quando listarCursos falha', async () => {
    listarCursos.mockRejectedValue(new ApiError(403, 'Acesso negado'))
    listarAlunos.mockResolvedValue({
      content: [],
      page: { number: 0, size: 20, totalElements: 0, totalPages: 0 },
      _links: { criar: '/academico/alunos' },
    })
    renderPage()
    expect(
      await screen.findByText(/Não foi possível carregar os cursos/),
    ).toBeTruthy()
    const salvar = await screen.findByRole('button', { name: 'Salvar' })
    expect((salvar as HTMLButtonElement).disabled).toBe(true)
    expect(screen.queryByText('Não foi possível carregar os alunos.')).toBeNull()
  })

  it('mostra editar, reset e matrícula só quando o _link existe', async () => {
    listarCursos.mockResolvedValue({
      content: [{ id: 'c1', sigla: 'TADS', nome: 'TADS' }],
      page: { number: 0, size: 20, totalElements: 1, totalPages: 1 },
    })
    listarAlunos.mockResolvedValue({
      content: [
        aluno('a1', { edit: '/academico/alunos/a1', 'reset-password': '/academico/alunos/a1/reset-senha', matricula: '/academico/alunos/a1/matriculas' }),
        aluno('a2', { self: '/academico/alunos/a2' }),
      ],
      page: { number: 0, size: 20, totalElements: 2, totalPages: 1 },
      _links: {},
    })
    renderPage()
    expect(await screen.findByRole('button', { name: 'Editar' })).toBeTruthy()
    expect(screen.getByRole('button', { name: 'Reset senha' })).toBeTruthy()
    expect(screen.getByRole('button', { name: 'Matricular' })).toBeTruthy()
    expect(screen.getByText('Sem permissão para este curso')).toBeTruthy()
    expect(screen.getAllByRole('button', { name: 'Editar' })).toHaveLength(1)
  })

  it('mantém o drawer de matrícula aberto no 422', async () => {
    listarCursos.mockResolvedValue({
      content: [],
      page: { number: 0, size: 20, totalElements: 0, totalPages: 0 },
    })
    listarAlunos.mockResolvedValue({
      content: [
        aluno('a1', { matricula: '/academico/alunos/a1/matriculas' }),
      ],
      page: { number: 0, size: 20, totalElements: 1, totalPages: 1 },
      _links: {},
    })
    opcoesMatricula.mockResolvedValue({
      periodoId: 'p1',
      periodoRotulo: '2026/2',
      disciplinas: [{ id: 'd1', codigo: 'BDD01', nome: 'Banco', vagas: 0 }],
    })
    matricularAluno.mockRejectedValue(
      new ApiError(422, 'A disciplina Banco não tem vagas disponíveis.', undefined, undefined, 'no-vacancies', 'd1'),
    )
    renderPage()
    fireEvent.click(await screen.findByRole('button', { name: 'Matricular' }))
    expect(await screen.findByRole('dialog', { name: 'Matricular Ana' })).toBeTruthy()
    fireEvent.click(await screen.findByRole('checkbox'))
    fireEvent.click(screen.getByRole('button', { name: 'Confirmar matrícula' }))
    expect((await screen.findByRole('alert')).textContent).toContain('não tem vagas')
    expect(screen.getByRole('dialog', { name: 'Matricular Ana' })).toBeTruthy()
  })
})

function aluno(id: string, links: Record<string, string>) {
  return {
    id,
    nome: 'Ana',
    grr: 'GRR20240001',
    emailInstitucional: 'ana@ufpr.br',
    idCurso: 'c1',
    situacao: 'MATRICULADO',
    ativo: true,
    _links: links,
  }
}
