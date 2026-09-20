import { QueryClient, QueryClientProvider } from '@tanstack/react-query'
import { fireEvent, render, screen } from '@testing-library/react'
import { describe, expect, it, vi } from 'vitest'
import { ApiError } from '../../api/client'
import { Alunos } from './Alunos'
import { Cursos } from './Cursos'

const listarCursos = vi.fn()
const listarAlunos = vi.fn()

vi.mock('../../api/academico', () => ({
  academicoApi: {
    listarCursos: (...args: unknown[]) => listarCursos(...args),
    listarAlunos: (...args: unknown[]) => listarAlunos(...args),
    criarAluno: vi.fn(),
    atualizarAluno: vi.fn(),
    excluirAluno: vi.fn(),
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

  it('desabilita GRR e e-mail institucional no modo edição', async () => {
    listarCursos.mockResolvedValue({
      content: [{ id: 'c1', nome: 'TADS', sigla: 'TADS', codigo: 'TADS-SEPT' }],
      page: { number: 0, size: 100, totalElements: 1, totalPages: 1 },
    })
    listarAlunos.mockResolvedValue({
      content: [
        {
          id: 'a1',
          nome: 'Ana',
          grr: 'GRR20240001',
          emailInstitucional: 'ana@ufpr.br',
          idCurso: 'c1',
          situacao: 'MATRICULADO',
          _links: { self: '/academico/alunos/a1', atualizar: '/academico/alunos/a1' },
        },
      ],
      page: { number: 0, size: 20, totalElements: 1, totalPages: 1 },
      _links: { criar: '/academico/alunos' },
    })
    renderPage()
    fireEvent.click(await screen.findByText('Editar'))
    const grr = screen.getByRole('textbox', { name: /GRR/ }) as HTMLInputElement
    const email = screen.getByRole('textbox', { name: /E-mail institucional/ }) as HTMLInputElement
    expect(grr.disabled).toBe(true)
    expect(email.disabled).toBe(true)
    expect(grr.getAttribute('aria-describedby')).toBe('hint-aluno-grr')
    expect(email.getAttribute('aria-describedby')).toBe('hint-aluno-email')
  })

  it('não compartilha o cache de cursos da tabela com o combo', async () => {
    listarCursos.mockImplementation((page = 0, size = 20) =>
      Promise.resolve({
        content: [],
        page: { number: page, size, totalElements: 0, totalPages: 0 },
      }),
    )
    listarAlunos.mockResolvedValue({
      content: [],
      page: { number: 0, size: 20, totalElements: 0, totalPages: 0 },
    })
    const client = new QueryClient({
      defaultOptions: { queries: { retry: false } },
    })
    render(
      <QueryClientProvider client={client}>
        <Cursos />
        <Alunos />
      </QueryClientProvider>,
    )
    await screen.findByRole('heading', { name: 'Cursos' })
    await screen.findByRole('heading', { name: 'Alunos' })
    const calls = listarCursos.mock.calls
    expect(calls.some((args) => (args[0] ?? 0) === 0 && (args[1] ?? 20) === 20)).toBe(true)
    expect(calls.some((args) => args[0] === 0 && args[1] === 100)).toBe(true)
  })
})
