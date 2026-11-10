import { QueryClient, QueryClientProvider } from '@tanstack/react-query'
import { fireEvent, render, screen } from '@testing-library/react'
import { MemoryRouter } from 'react-router-dom'
import { beforeEach, describe, expect, it, vi } from 'vitest'
import { PublicarComunicado } from './PublicarComunicado'

const audiencias = vi.fn()
const publicar = vi.fn()

vi.mock('../../api/comunicacoes', () => ({
  comunicacoesApi: {
    audiencias: (...args: unknown[]) => audiencias(...args),
    publicar: (...args: unknown[]) => publicar(...args),
  },
}))

function renderPage() {
  const client = new QueryClient({ defaultOptions: { queries: { retry: false } } })
  return render(
    <QueryClientProvider client={client}>
      <MemoryRouter>
        <PublicarComunicado />
      </MemoryRouter>
    </QueryClientProvider>,
  )
}

describe('PublicarComunicado', () => {
  beforeEach(() => {
    audiencias.mockReset()
    publicar.mockReset()
    audiencias.mockResolvedValue({
      content: [
        { tipo: 'TURMA', id: 't1', rotulo: 'Turma ADS 2026/1' },
        { tipo: 'CURSO', id: 'c1', rotulo: 'Curso TADS' },
      ],
    })
  })

  it('deixa Publicar desabilitado sem título', async () => {
    renderPage()
    const botao = await screen.findByRole('button', { name: 'Publicar' })
    expect(botao.hasAttribute('disabled')).toBe(true)
    fireEvent.change(screen.getByLabelText('Corpo'), { target: { value: 'Texto do aviso' } })
    expect(botao.hasAttribute('disabled')).toBe(true)
    fireEvent.change(screen.getByLabelText('Título'), { target: { value: 'Aviso' } })
    expect(botao.hasAttribute('disabled')).toBe(false)
  })

  it('não lista audiência de broadcast', async () => {
    renderPage()
    await screen.findByRole('option', { name: 'Turma ADS 2026/1' })
    expect(screen.getByRole('option', { name: 'Curso TADS' })).toBeTruthy()
    expect(screen.queryByRole('option', { name: /todos os alunos|universidade/i })).toBeNull()
    expect(screen.queryByText(/todos os alunos da universidade/i)).toBeNull()
  })

  it('atualiza a pré-visualização de Markdown', async () => {
    renderPage()
    fireEvent.change(await screen.findByLabelText('Corpo'), {
      target: { value: '## Título\n**negrito**\n*itálico*\n- item' },
    })
    expect(screen.getByRole('heading', { level: 2, name: 'Título' })).toBeTruthy()
    expect(screen.getByText('negrito').closest('strong')).toBeTruthy()
    expect(screen.getByText('itálico').closest('em')).toBeTruthy()
    expect(screen.getByText('item').closest('ul')).toBeTruthy()
    const preview = screen.getByRole('region', { name: 'Pré-visualização' })
    expect(preview.querySelector('textarea')).toBeNull()
  })

  it('toolbar insere Markdown no corpo', async () => {
    renderPage()
    const corpo = (await screen.findByLabelText('Corpo')) as HTMLTextAreaElement
    fireEvent.change(corpo, { target: { value: 'aviso' } })
    corpo.setSelectionRange(0, 5)
    fireEvent.click(screen.getByRole('button', { name: 'Negrito' }))
    expect(corpo.value).toBe('**aviso**')
    expect(screen.getByText('aviso').closest('strong')).toBeTruthy()
  })
})
