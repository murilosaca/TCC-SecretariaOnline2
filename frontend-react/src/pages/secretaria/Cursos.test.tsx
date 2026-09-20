import { QueryClient, QueryClientProvider } from '@tanstack/react-query'
import { render, screen } from '@testing-library/react'
import { describe, expect, it, vi } from 'vitest'
import { Cursos } from './Cursos'

const listarCursos = vi.fn()

vi.mock('../../api/academico', () => ({
  academicoApi: {
    listarCursos: (...args: unknown[]) => listarCursos(...args),
    criarCurso: vi.fn(),
    atualizarCurso: vi.fn(),
    excluirCurso: vi.fn(),
  },
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
    renderPage()
    expect(await screen.findByText('Novo curso')).toBeTruthy()
    expect(screen.getByText('Editar')).toBeTruthy()
    expect(screen.getByText('Excluir')).toBeTruthy()
    expect(screen.getByText('1')).toBeTruthy()
  })
})
