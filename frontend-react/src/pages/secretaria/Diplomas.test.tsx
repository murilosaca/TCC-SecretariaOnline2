import { QueryClient, QueryClientProvider } from '@tanstack/react-query'
import { fireEvent, render, screen, waitFor } from '@testing-library/react'
import { beforeEach, describe, expect, it, vi } from 'vitest'
import { Diplomas } from './Diplomas'

const elegiveis = vi.fn()
const confirmar = vi.fn()
const listar = vi.fn()
const confirmarEntrega = vi.fn()
const uploadPdf = vi.fn()
const listarCursos = vi.fn()
const listarPeriodos = vi.fn()

vi.mock('../../api/diplomas', () => ({
  diplomasApi: {
    elegiveis: (...args: unknown[]) => elegiveis(...args),
    confirmar: (...args: unknown[]) => confirmar(...args),
    listar: (...args: unknown[]) => listar(...args),
    confirmarEntrega: (...args: unknown[]) => confirmarEntrega(...args),
    uploadPdf: (...args: unknown[]) => uploadPdf(...args),
  },
}))

vi.mock('../../api/academico', () => ({
  academicoApi: {
    listarCursos: (...args: unknown[]) => listarCursos(...args),
    listarPeriodos: (...args: unknown[]) => listarPeriodos(...args),
  },
}))

function renderPage() {
  const client = new QueryClient({
    defaultOptions: { queries: { retry: false } },
  })
  return render(
    <QueryClientProvider client={client}>
      <Diplomas />
    </QueryClientProvider>,
  )
}

describe('Diplomas', () => {
  beforeEach(() => {
    elegiveis.mockReset()
    confirmar.mockReset()
    listar.mockReset()
    confirmarEntrega.mockReset()
    uploadPdf.mockReset()
    listarCursos.mockReset()
    listarPeriodos.mockReset()
    listarCursos.mockResolvedValue({
      content: [{ id: 'c1', nome: 'TADS', sigla: 'TADS', codigo: 'TADS', horasFormativasMinimas: 120, ativo: true }],
      page: { number: 0, size: 100, totalElements: 1, totalPages: 1 },
    })
    listarPeriodos.mockResolvedValue({
      content: [{ id: 'p1', ano: 2026, semestre: 2, inicio: '2026-08-01', fim: '2026-12-20', ativo: true }],
      page: { number: 0, size: 100, totalElements: 1, totalPages: 1 },
    })
    listar.mockResolvedValue({
      content: [],
      page: { number: 0, size: 50, totalElements: 0, totalPages: 0 },
    })
  })

  it('desabilita inelegível e só confirma com _links.confirm', async () => {
    elegiveis.mockResolvedValue({
      cursoId: 'c1',
      periodoId: 'p1',
      content: [
        { alunoId: 'a1', nome: 'Ana', grr: 'GRR20260001', elegivel: true, bloqueio: null },
        {
          alunoId: 'a2',
          nome: 'Bruno',
          grr: 'GRR20260002',
          elegivel: false,
          bloqueio: { razao: 'TCC não aprovado', detalhe: null },
        },
      ],
      _links: { self: '/diplomas/elegiveis', confirm: '/diplomas' },
    })
    renderPage()

    await screen.findByRole('option', { name: /TADS — TADS/ })
    await screen.findByRole('option', { name: '2026/2' })
    fireEvent.change(screen.getByLabelText('Curso'), { target: { value: 'c1' } })
    fireEvent.change(screen.getByLabelText('Período letivo'), { target: { value: 'p1' } })

    await waitFor(() => expect(elegiveis).toHaveBeenCalled())
    const checks = await screen.findAllByRole('checkbox')
    expect((checks[0] as HTMLInputElement).disabled).toBe(false)
    expect((checks[1] as HTMLInputElement).disabled).toBe(true)
    expect(screen.getByText('TCC não aprovado')).toBeTruthy()

    fireEvent.click(checks[0])
    fireEvent.click(screen.getByRole('button', { name: /Continuar/ }))
    expect(screen.getByRole('heading', { name: 'Confirmar colação' })).toBeTruthy()
  })

  it('sem _links.confirm não libera Continuar', async () => {
    elegiveis.mockResolvedValue({
      cursoId: 'c1',
      periodoId: 'p1',
      content: [{ alunoId: 'a1', nome: 'Ana', grr: 'GRR20260001', elegivel: true, bloqueio: null }],
      _links: { self: '/diplomas/elegiveis' },
    })
    renderPage()
    await screen.findByRole('option', { name: /TADS — TADS/ })
    await screen.findByRole('option', { name: '2026/2' })
    fireEvent.change(screen.getByLabelText('Curso'), { target: { value: 'c1' } })
    fireEvent.change(screen.getByLabelText('Período letivo'), { target: { value: 'p1' } })
    await waitFor(() => expect(elegiveis).toHaveBeenCalled())
    const check = await screen.findByRole('checkbox')
    fireEvent.click(check)
    expect((screen.getByRole('button', { name: /Continuar/ }) as HTMLButtonElement).disabled).toBe(true)
  })
})
