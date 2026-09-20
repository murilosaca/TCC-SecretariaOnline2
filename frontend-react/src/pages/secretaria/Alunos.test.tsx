import { QueryClient, QueryClientProvider } from '@tanstack/react-query'
import { render, screen } from '@testing-library/react'
import { describe, expect, it, vi } from 'vitest'
import { ApiError } from '../../api/client'
import { Alunos } from './Alunos'

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
})
