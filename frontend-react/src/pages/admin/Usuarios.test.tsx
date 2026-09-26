import { QueryClient, QueryClientProvider } from '@tanstack/react-query'
import { fireEvent, render, screen } from '@testing-library/react'
import { describe, expect, it, vi } from 'vitest'
import { ApiError } from '../../api/client'
import { Usuarios } from './Usuarios'

const listarUsuarios = vi.fn()
const resetSenha = vi.fn()

vi.mock('../../api/admin', () => ({
  adminApi: {
    listarUsuarios: (...args: unknown[]) => listarUsuarios(...args),
    criarUsuario: vi.fn(),
    atualizarUsuario: vi.fn(),
    desativarUsuario: vi.fn(),
    resetSenha: (...args: unknown[]) => resetSenha(...args),
  },
  iamApi: { buscarUsuarios: vi.fn() },
}))

function renderPage() {
  const client = new QueryClient({
    defaultOptions: { queries: { retry: false } },
  })
  return render(
    <QueryClientProvider client={client}>
      <Usuarios />
    </QueryClientProvider>,
  )
}

describe('Usuarios (F7.1)', () => {
  it('esconde ações sem _links e mostra 403 honesto', async () => {
    listarUsuarios.mockRejectedValue(new ApiError(403, 'Acesso negado'))
    renderPage()
    expect(await screen.findByText('Você não tem permissão para gerenciar usuários.')).toBeTruthy()
  })

  it('mostra ações da linha só quando o rel existe', async () => {
    listarUsuarios.mockResolvedValue({
      content: [
        {
          id: 'u1',
          nome: 'Admin Dev',
          emailInstitucional: 'admin.dev@ufpr.br',
          grr: 'GRR20240007',
          ativo: true,
          senhaAlterada: true,
          updatedAt: '2026-03-01T12:00:00Z',
          _links: {
            self: '/admin/usuarios/u1',
            atualizar: '/admin/usuarios/u1',
            desativar: '/admin/usuarios/u1/desativar',
            'reset-senha': '/admin/usuarios/u1/reset-senha',
          },
        },
        {
          id: 'u2',
          nome: 'Usuário desligado',
          emailInstitucional: 'inativo@ufpr.br',
          ativo: false,
          senhaAlterada: true,
          updatedAt: '2026-03-01T12:00:00Z',
          _links: {
            self: '/admin/usuarios/u2',
            atualizar: '/admin/usuarios/u2',
          },
        },
      ],
      page: { number: 0, size: 20, totalElements: 2, totalPages: 1 },
      _links: { criar: '/admin/usuarios' },
    })
    renderPage()
    expect(await screen.findByText('Novo usuário')).toBeTruthy()
    expect(screen.getAllByText('Editar')).toHaveLength(2)
    expect(screen.getByText('Desativar')).toBeTruthy()
    expect(screen.getByText('Reset senha')).toBeTruthy()
    expect(screen.getByText('Usuário desligado')).toBeTruthy()
    expect(screen.getAllByText('Inativo')).toHaveLength(1)
  })

  it('reset confirma envio sem campo de senha', async () => {
    listarUsuarios.mockResolvedValue({
      content: [
        {
          id: 'u1',
          nome: 'Alvo',
          emailInstitucional: 'alvo@ufpr.br',
          ativo: true,
          senhaAlterada: true,
          updatedAt: '2026-03-01T12:00:00Z',
          _links: { 'reset-senha': '/admin/usuarios/u1/reset-senha' },
        },
      ],
      page: { number: 0, size: 20, totalElements: 1, totalPages: 1 },
    })
    resetSenha.mockResolvedValue(undefined)
    renderPage()
    fireEvent.click(await screen.findByText('Reset senha'))
    expect(screen.getByText(/Confirmar envio/)).toBeTruthy()
    expect(document.querySelector('input[type="password"]')).toBeNull()
    fireEvent.click(screen.getByText('Confirmar envio'))
    expect(await screen.findByText(/Link de redefinição enviado/)).toBeTruthy()
  })
})
