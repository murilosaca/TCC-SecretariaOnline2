import { QueryClient, QueryClientProvider } from '@tanstack/react-query'
import { fireEvent, render, screen } from '@testing-library/react'
import { MemoryRouter } from 'react-router-dom'
import { beforeEach, describe, expect, it, vi } from 'vitest'
import type { NotificacaoPreferencia } from '../../models/perfil'
import { Notificacoes } from './Notificacoes'

const notificacoes = vi.fn()
const salvarNotificacoes = vi.fn()

vi.mock('../../api/perfil', () => ({
  perfilApi: {
    notificacoes: (...args: unknown[]) => notificacoes(...args),
    salvarNotificacoes: (...args: unknown[]) => salvarNotificacoes(...args),
  },
}))

vi.mock('../../auth/AuthContext', () => ({
  useAuth: () => ({
    links: {
      perfil: '/perfil',
      'perfil-seguranca': '/perfil/seguranca',
      'perfil-notificacoes': '/perfil/notificacoes',
    },
  }),
}))

const atual: NotificacaoPreferencia = {
  canais: {
    CRITICAL: { email: true, inApp: true },
    HIGH: { email: true, inApp: true },
    MEDIUM: { email: true, inApp: true },
    LOW: { email: true, inApp: true },
  },
  dndInicio: null,
  dndFim: null,
  digest: 'IMEDIATO',
  _links: { self: '/me/notifications', update: '/me/notifications' },
}

function renderPage() {
  const client = new QueryClient({ defaultOptions: { queries: { retry: false } } })
  return render(
    <QueryClientProvider client={client}>
      <MemoryRouter>
        <Notificacoes />
      </MemoryRouter>
    </QueryClientProvider>,
  )
}

describe('Notificacoes', () => {
  beforeEach(() => {
    notificacoes.mockReset()
    salvarNotificacoes.mockReset()
    notificacoes.mockResolvedValue(atual)
  })

  it('bloqueia os canais CRITICAL e cancela sem PATCH', async () => {
    renderPage()
    const critico = await screen.findByRole('switch', {
      name: 'E-mail para prioridade CRITICAL',
    })
    expect(critico).toHaveProperty('disabled', true)
    expect(critico.getAttribute('aria-checked')).toBe('true')
    const medio = screen.getByRole('switch', { name: 'E-mail para prioridade MEDIUM' })
    fireEvent.click(medio)
    expect(medio.getAttribute('aria-checked')).toBe('false')
    fireEvent.click(screen.getByRole('button', { name: 'Cancelar' }))
    expect(
      screen.getByRole('switch', { name: 'E-mail para prioridade MEDIUM' }).getAttribute('aria-checked'),
    ).toBe('true')
    expect(salvarNotificacoes).not.toHaveBeenCalled()
  })

  it('mostra horários de não perturbe só com o checkbox marcado', async () => {
    renderPage()
    expect(await screen.findByLabelText('Usar horário de não perturbe')).toBeTruthy()
    expect(screen.queryByLabelText('Início')).toBeNull()
    fireEvent.click(screen.getByLabelText('Usar horário de não perturbe'))
    expect(screen.getByLabelText('Início')).toBeTruthy()
    expect(screen.getByLabelText('Fim')).toBeTruthy()
  })
})
