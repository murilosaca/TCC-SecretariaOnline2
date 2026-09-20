import { QueryClient, QueryClientProvider } from '@tanstack/react-query'
import { fireEvent, render, screen } from '@testing-library/react'
import { describe, expect, it, vi } from 'vitest'
import { Disciplinas } from './Disciplinas'

const listarCursos = vi.fn()
const listarDisciplinas = vi.fn()

vi.mock('../../api/academico', () => ({
  academicoApi: {
    listarCursos: (...args: unknown[]) => listarCursos(...args),
    listarDisciplinas: (...args: unknown[]) => listarDisciplinas(...args),
    criarDisciplina: vi.fn(),
    atualizarDisciplina: vi.fn(),
    excluirDisciplina: vi.fn(),
  },
}))

function renderPage() {
  const client = new QueryClient({
    defaultOptions: { queries: { retry: false } },
  })
  return render(
    <QueryClientProvider client={client}>
      <Disciplinas />
    </QueryClientProvider>,
  )
}

describe('Disciplinas', () => {
  it('desabilita o curso no modo edição', async () => {
    listarCursos.mockResolvedValue({
      content: [{ id: 'c1', nome: 'TADS', sigla: 'TADS', codigo: 'TADS-SEPT' }],
      page: { number: 0, size: 100, totalElements: 1, totalPages: 1 },
    })
    listarDisciplinas.mockResolvedValue({
      content: [
        {
          id: 'd1',
          idCurso: 'c1',
          codigo: 'TADS001',
          nome: 'Algoritmos',
          periodo: 1,
          cargaHorariaTotal: 60,
          creditos: 4,
          _links: { self: '/academico/disciplinas/d1', atualizar: '/academico/disciplinas/d1' },
        },
      ],
      page: { number: 0, size: 20, totalElements: 1, totalPages: 1 },
      _links: { criar: '/academico/disciplinas' },
    })
    renderPage()
    fireEvent.click(await screen.findByText('Editar'))
    const curso = screen.getByRole('combobox', { name: /Curso/ }) as HTMLSelectElement
    expect(curso.disabled).toBe(true)
    expect(curso.getAttribute('aria-describedby')).toBe('hint-disciplina-curso')
  })
})
