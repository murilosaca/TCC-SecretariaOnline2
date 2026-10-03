import { fireEvent, render, screen } from '@testing-library/react'
import { QueryClient, QueryClientProvider } from '@tanstack/react-query'
import { MemoryRouter } from 'react-router-dom'
import { describe, expect, it, vi } from 'vitest'
import { CommandPalette } from './CommandPalette'
import { buscarGlobal } from '../api/busca'

vi.mock('../api/busca', () => ({
  buscarGlobal: vi.fn(),
}))

function renderPaleta() {
  const client = new QueryClient({ defaultOptions: { queries: { retry: false } } })
  return render(
    <QueryClientProvider client={client}>
      <MemoryRouter>
        <CommandPalette aberto onFechar={() => undefined} />
      </MemoryRouter>
    </QueryClientProvider>,
  )
}

describe('CommandPalette', () => {
  it('não chama a API com um caractere', () => {
    vi.useFakeTimers()
    try {
      renderPaleta()
      fireEvent.change(screen.getByLabelText('Termo da busca'), { target: { value: 'a' } })
      vi.advanceTimersByTime(250)
      expect(buscarGlobal).not.toHaveBeenCalled()
    } finally {
      vi.useRealTimers()
    }
  })

  it('não mostra o grupo Usuários quando a busca do aluno volta vazia', async () => {
    vi.mocked(buscarGlobal).mockResolvedValue({
      q: 'jo',
      alunos: [{ id: '1', titulo: 'João da Silva', subtitulo: 'GRR20240001', href: '/perfil' }],
      solicitacoes: [],
      eventos: [],
      usuarios: [],
    })
    renderPaleta()
    fireEvent.change(screen.getByLabelText('Termo da busca'), { target: { value: 'jo' } })
    expect(await screen.findByRole('heading', { name: 'Alunos' })).toBeTruthy()
    expect(screen.queryByRole('heading', { name: 'Usuários' })).toBeNull()
  })
})
