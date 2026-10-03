import { QueryClient, QueryClientProvider } from '@tanstack/react-query'
import { fireEvent, render, screen } from '@testing-library/react'
import { MemoryRouter } from 'react-router-dom'
import { beforeEach, describe, expect, it, vi } from 'vitest'
import { NovaFormativa } from './NovaFormativa'

const tipos = vi.fn()

vi.mock('../../api/formativas', () => ({
  formativasApi: {
    tipos: (...args: unknown[]) => tipos(...args),
    submeter: vi.fn(),
  },
}))

function renderPage() {
  const client = new QueryClient({ defaultOptions: { queries: { retry: false } } })
  return render(
    <QueryClientProvider client={client}>
      <MemoryRouter>
        <NovaFormativa />
      </MemoryRouter>
    </QueryClientProvider>,
  )
}

describe('NovaFormativa', () => {
  beforeEach(() => {
    tipos.mockReset()
    tipos.mockResolvedValue({
      content: [{ id: 't1', nome: 'Curso de extensão' }],
    })
  })

  it('deixa Enviar desabilitado sem comprovante', async () => {
    renderPage()
    const botao = await screen.findByRole('button', { name: 'Enviar' })
    expect(botao.hasAttribute('disabled')).toBe(true)
    fireEvent.change(screen.getByLabelText('Atividade'), { target: { value: 't1' } })
    fireEvent.change(screen.getByLabelText('Horas'), { target: { value: '8' } })
    expect(botao.hasAttribute('disabled')).toBe(true)
  })

  it('não mostra tipo que não veio da API', async () => {
    renderPage()
    await screen.findByRole('option', { name: 'Curso de extensão' })
    expect(screen.queryByRole('option', { name: 'Tipo de outro curso' })).toBeNull()
    expect(screen.queryByText('Tipo de outro curso')).toBeNull()
  })
})
