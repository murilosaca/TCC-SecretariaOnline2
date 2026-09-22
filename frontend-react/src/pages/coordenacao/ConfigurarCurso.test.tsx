import { QueryClient, QueryClientProvider } from '@tanstack/react-query'
import { fireEvent, render, screen, waitFor } from '@testing-library/react'
import { MemoryRouter, Route, Routes } from 'react-router-dom'
import { beforeEach, describe, expect, it, vi } from 'vitest'
import { ApiError } from '../../api/client'
import { ConfigurarCurso } from './ConfigurarCurso'

const obterConfig = vi.fn()
const atualizarConfig = vi.fn()

vi.mock('../../api/coordenacao', () => ({
  coordenacaoApi: {
    obterConfig: (...args: unknown[]) => obterConfig(...args),
    atualizarConfig: (...args: unknown[]) => atualizarConfig(...args),
  },
}))

function renderPage(id = 'curso-tads') {
  const client = new QueryClient({ defaultOptions: { queries: { retry: false } } })
  return render(
    <QueryClientProvider client={client}>
      <MemoryRouter initialEntries={[`/coordenacao/cursos/${id}/configurar`]}>
        <Routes>
          <Route path="/coordenacao/cursos/:id/configurar" element={<ConfigurarCurso />} />
        </Routes>
      </MemoryRouter>
    </QueryClientProvider>,
  )
}

const atual = {
  id: 'curso-tads',
  nome: 'Análise e Desenvolvimento de Sistemas',
  sigla: 'TADS',
  horasFormativasMinimas: 120,
  duracaoCalendario: 15,
  bancaMembrosExternos: 1,
  bancaModalidade: 'PRESENCIAL' as const,
  regimento: 'Regimento vigente',
  _links: {
    self: '/coordenacao/cursos/curso-tads/config',
    update: '/coordenacao/cursos/curso-tads/config',
  },
}

describe('ConfigurarCurso', () => {
  beforeEach(() => {
    obterConfig.mockReset()
    atualizarConfig.mockReset()
  })

  it('carrega os valores atuais e mantém Salvar desabilitado', async () => {
    obterConfig.mockResolvedValue(atual)
    renderPage()
    expect(await screen.findByDisplayValue('120')).toBeTruthy()
    expect(screen.getByDisplayValue('Regimento vigente')).toBeTruthy()
    expect((screen.getByRole('button', { name: 'Salvar' }) as HTMLButtonElement).disabled).toBe(true)
  })

  it('envia PATCH ao salvar horas e mostra o toast', async () => {
    obterConfig.mockResolvedValue(atual)
    atualizarConfig.mockResolvedValue({
      ...atual,
      horasFormativasMinimas: 150,
    })
    renderPage()
    const horas = await screen.findByLabelText('Horas formativas mínimas')
    fireEvent.change(horas, { target: { value: '150' } })
    fireEvent.click(screen.getByRole('button', { name: 'Salvar' }))
    await waitFor(() => {
      expect(atualizarConfig).toHaveBeenCalledWith('/coordenacao/cursos/curso-tads/config', {
        horasFormativasMinimas: 150,
        duracaoCalendario: 15,
        bancaMembrosExternos: 1,
        bancaModalidade: 'PRESENCIAL',
        regimento: 'Regimento vigente',
      })
    })
    expect(await screen.findByText('Configuração salva')).toBeTruthy()
  })

  it('cancela com confirmação sem chamar a API', async () => {
    obterConfig.mockResolvedValue(atual)
    renderPage()
    const membros = await screen.findByLabelText('Membros externos')
    fireEvent.change(membros, { target: { value: '2' } })
    fireEvent.click(screen.getByRole('button', { name: 'Cancelar' }))
    expect(screen.getByText('Deseja descartar as alterações?')).toBeTruthy()
    fireEvent.click(screen.getByRole('button', { name: 'Descartar' }))
    expect((screen.getByLabelText('Membros externos') as HTMLSelectElement).value).toBe('1')
    expect(atualizarConfig).not.toHaveBeenCalled()
  })

  it('bloqueia salvar com horas inválidas', async () => {
    obterConfig.mockResolvedValue(atual)
    renderPage()
    const horas = await screen.findByLabelText('Horas formativas mínimas')
    fireEvent.change(horas, { target: { value: '-10' } })
    expect(screen.getByText('Valor deve ser entre 0 e 1000')).toBeTruthy()
    expect((screen.getByRole('button', { name: 'Salvar' }) as HTMLButtonElement).disabled).toBe(true)
    expect(atualizarConfig).not.toHaveBeenCalled()
  })

  it('mostra alerta de curso alheio no 403', async () => {
    obterConfig.mockRejectedValue(new ApiError(403, 'Você não é coordenador deste curso.'))
    renderPage('curso-ec')
    expect(await screen.findByText('Você não é coordenador deste curso')).toBeTruthy()
    expect(screen.queryByLabelText('Horas formativas mínimas')).toBeNull()
  })
})
