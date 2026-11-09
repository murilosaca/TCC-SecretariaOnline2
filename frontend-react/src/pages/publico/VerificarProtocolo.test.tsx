import { QueryClient, QueryClientProvider } from '@tanstack/react-query'
import { fireEvent, render, screen, waitFor } from '@testing-library/react'
import { MemoryRouter, Route, Routes } from 'react-router-dom'
import { beforeEach, describe, expect, it, vi } from 'vitest'
import { ApiError } from '../../api/client'
import { VerificarProtocolo } from './VerificarProtocolo'

const protocolo = vi.fn()

vi.mock('../../api/publico', () => ({
  publicoApi: {
    protocolo: (...args: unknown[]) => protocolo(...args),
  },
}))

function renderProtocolo(path: string) {
  const client = new QueryClient({
    defaultOptions: { queries: { retry: false } },
  })
  return render(
    <QueryClientProvider client={client}>
      <MemoryRouter initialEntries={[path]}>
        <Routes>
          <Route path="/publico/verificar-protocolo" element={<VerificarProtocolo />} />
          <Route path="/publico/verificar-protocolo/:id" element={<VerificarProtocolo />} />
        </Routes>
      </MemoryRouter>
    </QueryClientProvider>,
  )
}

describe('VerificarProtocolo', () => {
  beforeEach(() => {
    protocolo.mockReset()
  })

  it('mostra formulário sem TelaPendente quando não há id', () => {
    renderProtocolo('/publico/verificar-protocolo')
    expect(screen.getByRole('heading', { name: 'Verificar protocolo' })).toBeTruthy()
    expect(screen.getByLabelText('Protocolo')).toBeTruthy()
    expect(screen.queryByText(/Tela prevista/)).toBeNull()
  })

  it('mostra not-found quando a API devolve 404', async () => {
    protocolo.mockRejectedValue(new ApiError(404, 'não encontrado'))
    renderProtocolo('/publico/verificar-protocolo/PROT-2026-00099')
    await waitFor(() => {
      expect(screen.getByText(/Nenhum protocolo encontrado para PROT-2026-00099/)).toBeTruthy()
    })
    expect(screen.queryByText(/Tela prevista/)).toBeNull()
  })

  it('mostra metadados quando a consulta é ok', async () => {
    protocolo.mockResolvedValue({
      protocolo: 'PROT-2026-00001',
      tipoNome: 'Declaração simples',
      estado: 'EM_ANALISE',
      hashSha256: 'abcd1234efgh5678ijkl9012',
    })
    renderProtocolo('/publico/verificar-protocolo/PROT-2026-00001')
    await waitFor(() => {
      expect(screen.getByText('PROT-2026-00001')).toBeTruthy()
    })
    expect(screen.getByText('Declaração simples')).toBeTruthy()
    expect(screen.getByText('EM_ANALISE')).toBeTruthy()
    expect(screen.getByText('abcd1234…9012')).toBeTruthy()
    expect(screen.queryByText(/Tela prevista/)).toBeNull()
    expect(screen.getByLabelText('Selecionar arquivo')).toBeTruthy()
  })

  it('compara o PDF no navegador e não consulta a API de novo', async () => {
    const bytes = new TextEncoder().encode('%PDF-local')
    const digest = await crypto.subtle.digest('SHA-256', bytes)
    const hex = [...new Uint8Array(digest)].map((byte) => byte.toString(16).padStart(2, '0')).join('')
    protocolo.mockResolvedValue({
      protocolo: 'PROT-2026-00001',
      tipoNome: 'Declaração simples',
      estado: 'DELIBERADA',
      hashSha256: hex,
    })
    renderProtocolo('/publico/verificar-protocolo/PROT-2026-00001')
    const input = await screen.findByLabelText('Selecionar arquivo')
    fireEvent.change(input, { target: { files: [new File([bytes], 'protocolo.pdf', { type: 'application/pdf' })] } })
    await waitFor(() => {
      expect(screen.getByText(/Hash confere/)).toBeTruthy()
    })
    expect(protocolo).toHaveBeenCalledTimes(1)

    fireEvent.change(input, {
      target: { files: [new File(['outro'], 'outro.pdf', { type: 'application/pdf' })] },
    })
    await waitFor(() => {
      expect(screen.getByText(/NÃO confere/)).toBeTruthy()
    })
    expect(protocolo).toHaveBeenCalledTimes(1)
  })

  it('não oferece arquivo quando o protocolo não existe', async () => {
    protocolo.mockRejectedValue(new ApiError(404, 'não encontrado'))
    renderProtocolo('/publico/verificar-protocolo/PROT-2026-00099')
    await waitFor(() => {
      expect(screen.getByText(/Nenhum protocolo encontrado/)).toBeTruthy()
    })
    expect(screen.queryByLabelText('Selecionar arquivo')).toBeNull()
  })
})
