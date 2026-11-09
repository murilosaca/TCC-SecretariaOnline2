import { render, screen } from '@testing-library/react'
import { QueryClient, QueryClientProvider } from '@tanstack/react-query'
import { MemoryRouter } from 'react-router-dom'
import { beforeEach, describe, expect, it, vi } from 'vitest'
import { ApiError } from '../../api/client'
import { eventosApi } from '../../api/eventos'
import { ProfessorEventoNova } from './ProfessorEventoNova'

vi.mock('../../api/eventos', () => ({
  eventosApi: {
    cursosAlvo: vi.fn(),
    criar: vi.fn(),
  },
}))

function renderForm() {
  const client = new QueryClient({ defaultOptions: { queries: { retry: false } } })
  return render(
    <QueryClientProvider client={client}>
      <MemoryRouter>
        <ProfessorEventoNova />
      </MemoryRouter>
    </QueryClientProvider>,
  )
}

describe('ProfessorEventoNova', () => {
  beforeEach(() => {
    vi.mocked(eventosApi.cursosAlvo).mockReset()
  })

  it('mostra carregando, vazio e erro dos cursos', async () => {
    let resolveCursos: (valor: { id: string; sigla: string; nome: string }[]) => void = () => undefined
    vi.mocked(eventosApi.cursosAlvo).mockReturnValue(
      new Promise((resolve) => {
        resolveCursos = resolve
      }),
    )
    const primeiro = renderForm()
    expect(screen.getByText('Carregando cursos…').textContent).toContain('Carregando cursos')
    resolveCursos([])
    expect((await screen.findByText('Nenhum curso ativo para o público-alvo.')).textContent).toContain(
      'Nenhum curso ativo',
    )
    primeiro.unmount()

    vi.mocked(eventosApi.cursosAlvo).mockRejectedValue(new ApiError(500, 'falha'))
    renderForm()
    expect((await screen.findByRole('alert')).textContent).toContain('Não foi possível carregar os cursos')
  })
})
