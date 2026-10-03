import { QueryClient, QueryClientProvider } from '@tanstack/react-query'
import { fireEvent, render, screen, waitFor } from '@testing-library/react'
import { MemoryRouter } from 'react-router-dom'
import { beforeEach, describe, expect, it, vi } from 'vitest'
import { Comunicacao } from './Comunicacao'
import type { HubComunicacao } from '../../models/comunicacao'

const listar = vi.fn()
const marcarLida = vi.fn()

vi.mock('../../api/comunicacoes', () => ({
  comunicacoesApi: {
    listar: (...args: unknown[]) => listar(...args),
    marcarLida: (...args: unknown[]) => marcarLida(...args),
  },
}))

const badges = { todos: 1, institucional: 1, turma: 0, inbox: 0 }

function hub(content: HubComunicacao['content']): HubComunicacao {
  return {
    content,
    badges,
    page: { number: 0, size: 20, totalElements: content.length, totalPages: content.length ? 1 : 0 },
  }
}

function renderPage() {
  const client = new QueryClient({ defaultOptions: { queries: { retry: false } } })
  return render(
    <QueryClientProvider client={client}>
      <MemoryRouter>
        <Comunicacao />
      </MemoryRouter>
    </QueryClientProvider>,
  )
}

describe('Comunicacao', () => {
  beforeEach(() => {
    listar.mockReset()
    marcarLida.mockReset()
  })

  it('mostra empty state quando o filtro não devolve nada', async () => {
    const institucional = {
      id: '1',
      tipo: 'INSTITUCIONAL' as const,
      titulo: 'Aviso institucional',
      corpo: 'texto',
      prioridade: 'MEDIUM' as const,
      data: '2026-10-03T12:00:00Z',
      lida: false,
      _links: { self: '/communications/1', 'marcar-lido': '/communications/1/read' },
    }
    listar.mockImplementation(async (filtro: { lido?: string; tipo?: string }) => {
      if (filtro.lido === 'false' && filtro.tipo === 'INSTITUCIONAL') {
        return hub([])
      }
      return hub([institucional])
    })
    renderPage()
    await waitFor(() => {
      expect(screen.getByText('Aviso institucional')).toBeTruthy()
    })
    fireEvent.change(screen.getByLabelText('Lido'), { target: { value: 'false' } })
    fireEvent.change(screen.getByLabelText('Tipo'), { target: { value: 'INSTITUCIONAL' } })
    await waitFor(() => {
      expect(screen.getByText('Nenhuma comunicação encontrada.')).toBeTruthy()
    })
    expect(screen.queryByText('Aviso institucional')).toBeNull()
    expect(listar).toHaveBeenLastCalledWith({ aba: 'TODOS', lido: 'false', tipo: 'INSTITUCIONAL' })
  })

  it('marca lido só quando existe _links.marcar-lido', async () => {
    listar.mockResolvedValue(
      hub([
        {
          id: '1',
          tipo: 'TURMA',
          titulo: 'Não lida',
          corpo: 'corpo',
          prioridade: 'HIGH',
          data: '2026-10-03T12:00:00Z',
          lida: false,
          _links: { self: '/communications/1', 'marcar-lido': '/communications/1/read' },
        },
        {
          id: '2',
          tipo: 'TURMA',
          titulo: 'Sem link',
          corpo: 'corpo',
          prioridade: 'LOW',
          data: '2026-10-03T12:00:00Z',
          lida: false,
          _links: { self: '/communications/2' },
        },
      ]),
    )
    marcarLida.mockResolvedValue(undefined)
    renderPage()
    await waitFor(() => {
      expect(screen.getByRole('button', { name: /Sem link/ })).toBeTruthy()
    })
    fireEvent.click(screen.getByRole('button', { name: /Sem link/ }))
    expect(marcarLida).not.toHaveBeenCalled()
    fireEvent.click(screen.getByRole('button', { name: /Não lida/ }))
    await waitFor(() => {
      expect(marcarLida).toHaveBeenCalledTimes(1)
    })
    expect(marcarLida).toHaveBeenCalledWith('/communications/1/read')
  })

  it('mostra CTA da inbox só com _links.acao', async () => {
    listar.mockResolvedValue(
      hub([
        {
          id: '1',
          tipo: 'INBOX',
          titulo: 'Dar ciência',
          corpo: 'pendente',
          prioridade: 'HIGH',
          data: '2026-10-03T12:00:00Z',
          lida: false,
          _links: { self: '/communications/1', acao: '/perfil' },
        },
        {
          id: '2',
          tipo: 'TURMA',
          titulo: 'Sem ação',
          corpo: 'aviso',
          prioridade: 'LOW',
          data: '2026-10-03T12:00:00Z',
          lida: true,
          _links: { self: '/communications/2' },
        },
      ]),
    )
    renderPage()
    await waitFor(() => {
      expect(screen.getByRole('link', { name: 'Ver ação' })).toBeTruthy()
    })
    expect(screen.getAllByRole('link', { name: 'Ver ação' })).toHaveLength(1)
    expect(screen.getByRole('link', { name: 'Ver ação' }).getAttribute('href')).toBe('/perfil')
  })
})
