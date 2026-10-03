import { QueryClient, QueryClientProvider } from '@tanstack/react-query'
import { fireEvent, render, screen, waitFor } from '@testing-library/react'
import { MemoryRouter } from 'react-router-dom'
import { beforeEach, describe, expect, it, vi } from 'vitest'
import type { Perfil as PerfilDto } from '../../models/perfil'
import { Perfil } from './Perfil'

const obter = vi.fn()
const atualizar = vi.fn()

vi.mock('../../api/perfil', () => ({
  perfilApi: {
    obter: (...args: unknown[]) => obter(...args),
    atualizar: (...args: unknown[]) => atualizar(...args),
    enviarFoto: vi.fn(),
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

const atual: PerfilDto = {
  id: 'usuario-1',
  nome: 'Aluno Dev',
  nomeSocial: '',
  telefone: '',
  emailPessoal: 'ana@example.com',
  emailInstitucional: 'aluno.dev@ufpr.br',
  grr: 'GRR20240001',
  identidadeGenero: '',
  _links: {
    self: '/me',
    update: '/me',
    foto: '/me/foto',
  },
}

function renderPage() {
  const client = new QueryClient({ defaultOptions: { queries: { retry: false } } })
  return render(
    <QueryClientProvider client={client}>
      <MemoryRouter>
        <Perfil />
      </MemoryRouter>
    </QueryClientProvider>,
  )
}

describe('Perfil', () => {
  beforeEach(() => {
    obter.mockReset()
    atualizar.mockReset()
    obter.mockResolvedValue(atual)
  })

  it('mantém o GRR somente leitura', async () => {
    renderPage()
    const grr = (await screen.findByLabelText('GRR')) as HTMLInputElement
    expect(grr.value).toBe('GRR20240001')
    expect(grr.disabled).toBe(true)
    expect(grr.readOnly).toBe(true)
    const institucional = screen.getByLabelText('E-mail institucional') as HTMLInputElement
    expect(institucional.value).toBe('aluno.dev@ufpr.br')
    expect(institucional.disabled).toBe(true)
  })

  it('cancela o dirty state sem chamar PATCH', async () => {
    renderPage()
    const telefone = (await screen.findByLabelText('Telefone')) as HTMLInputElement
    expect((screen.getByRole('button', { name: 'Salvar' }) as HTMLButtonElement).disabled).toBe(true)
    fireEvent.change(telefone, { target: { value: '41988887777' } })
    expect((screen.getByRole('button', { name: 'Salvar' }) as HTMLButtonElement).disabled).toBe(false)
    fireEvent.click(screen.getByRole('button', { name: 'Cancelar' }))
    expect((screen.getByLabelText('Telefone') as HTMLInputElement).value).toBe('')
    expect(atualizar).not.toHaveBeenCalled()
  })

  it('envia só o telefone alterado e não inclui o GRR', async () => {
    atualizar.mockResolvedValue({ ...atual, telefone: '41988887777' })
    renderPage()
    const telefone = await screen.findByLabelText('Telefone')
    fireEvent.change(telefone, { target: { value: '41988887777' } })
    fireEvent.click(screen.getByRole('button', { name: 'Salvar' }))
    await waitFor(() => {
      expect(atualizar).toHaveBeenCalledWith('/me', { telefone: '41988887777' })
    })
    expect(await screen.findByText('Perfil atualizado com sucesso.')).toBeTruthy()
  })
})
