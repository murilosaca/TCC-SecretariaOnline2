import { fireEvent, render, screen } from '@testing-library/react'
import { QueryClient, QueryClientProvider } from '@tanstack/react-query'
import { MemoryRouter } from 'react-router-dom'
import { beforeEach, describe, expect, it, vi } from 'vitest'
import { Importacoes } from './Importacoes'

const enviar = vi.fn()
const obter = vi.fn()
const confirmar = vi.fn()

vi.mock('../../api/importacoes', () => ({
  importacoesApi: {
    modelo: vi.fn(),
    enviar: (...args: unknown[]) => enviar(...args),
    obter: (...args: unknown[]) => obter(...args),
    confirmar: (...args: unknown[]) => confirmar(...args),
  },
}))

function renderTela() {
  const client = new QueryClient({ defaultOptions: { queries: { retry: false } } })
  return render(
    <QueryClientProvider client={client}>
      <MemoryRouter>
        <Importacoes />
      </MemoryRouter>
    </QueryClientProvider>,
  )
}

describe('Importacoes', () => {
  beforeEach(() => {
    enviar.mockReset()
    obter.mockReset()
    confirmar.mockReset()
  })

  it('desabilita confirmar quando há erros', async () => {
    enviar.mockResolvedValue({ id: 'job-1' })
    obter.mockResolvedValue({
      id: 'job-1',
      kind: 'alunos',
      status: 'VALIDATED',
      nomeArquivo: 'alunos.csv',
      checksumSha256: 'abc',
      totalLinhas: 1,
      validCount: 0,
      errorCount: 2,
      warningCount: 0,
      importadas: 0,
      mensagem: null,
      createdAt: '2026-10-03T00:00:00Z',
      updatedAt: '2026-10-03T00:00:00Z',
      linhas: [{ numero: 2, status: 'INVALID', mensagem: 'GRR inválido', valores: {} }],
      _links: { self: '/importacoes/job-1' },
    })
    renderTela()
    const arquivo = new File(['a,b\n'], 'alunos.csv', { type: 'text/csv' })
    fireEvent.change(screen.getByLabelText('Planilha'), { target: { files: [arquivo] } })
    expect(await screen.findByText(/Corrija os 2 erros/)).toBeTruthy()
    expect((screen.getByRole('button', { name: 'Confirmar importação' }) as HTMLButtonElement).disabled).toBe(true)
    expect(confirmar).not.toHaveBeenCalled()
  })

  it('bloqueia arquivo maior que 20 MB antes do envio', () => {
    renderTela()
    const grande = new File(['x'], 'grande.csv', { type: 'text/csv' })
    Object.defineProperty(grande, 'size', { value: 20 * 1024 * 1024 + 1 })
    fireEvent.change(screen.getByLabelText('Planilha'), { target: { files: [grande] } })
    expect(screen.getByRole('alert').textContent).toContain('Arquivo excede 20 MB.')
    expect(enviar).not.toHaveBeenCalled()
  })
})
