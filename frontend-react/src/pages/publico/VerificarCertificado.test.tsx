import { QueryClient, QueryClientProvider } from '@tanstack/react-query'
import { render, screen, waitFor } from '@testing-library/react'
import { MemoryRouter, Route, Routes } from 'react-router-dom'
import { beforeEach, describe, expect, it, vi } from 'vitest'
import { ApiError } from '../../api/client'
import { VerificarCertificado } from './VerificarCertificado'

const verificarCertificado = vi.fn()
const jwks = vi.fn()
const verificarAssinatura = vi.fn()

vi.mock('../../api/publico', () => ({
  publicoApi: {
    verificarCertificado: (...args: unknown[]) => verificarCertificado(...args),
    jwks: (...args: unknown[]) => jwks(...args),
  },
}))

vi.mock('../../lib/certificadoPublico', async () => {
  const real = await vi.importActual<typeof import('../../lib/certificadoPublico')>(
    '../../lib/certificadoPublico',
  )
  return {
    ...real,
    verificarAssinaturaEd25519: (...args: unknown[]) => verificarAssinatura(...args),
  }
})

function renderHash(path: string) {
  const client = new QueryClient({
    defaultOptions: { queries: { retry: false } },
  })
  return render(
    <QueryClientProvider client={client}>
      <MemoryRouter initialEntries={[path]}>
        <Routes>
          <Route path="/publico/verificar-certificado/:hash" element={<VerificarCertificado />} />
        </Routes>
      </MemoryRouter>
    </QueryClientProvider>,
  )
}

const HASH = 'a'.repeat(64)

describe('VerificarCertificado', () => {
  beforeEach(() => {
    verificarCertificado.mockReset()
    jwks.mockReset()
    verificarAssinatura.mockReset()
  })

  it('mostra inválido sem dados quando o hash não existe (F0.7-c)', async () => {
    verificarCertificado.mockRejectedValue(new ApiError(404, 'não encontrado'))
    renderHash(`/publico/verificar-certificado/${HASH}`)
    await waitFor(() => {
      expect(screen.getByText('Certificado inválido')).toBeTruthy()
    })
    expect(screen.getByText(/não pôde ser verificado/)).toBeTruthy()
    expect(screen.queryByText(/Beneficiário/)).toBeNull()
    expect(screen.queryByText(/Tela prevista/)).toBeNull()
  })

  it('mostra válido quando a API e a assinatura ED25519 conferem', async () => {
    verificarCertificado.mockResolvedValue({
      status: 'VALIDO',
      hashSha256: HASH,
      assinaturaEd25519: 'assinatura',
      beneficiarioNome: 'Aluno Dev',
      atividade: 'Oficina',
      cargaHoraria: 4,
      emitidoEm: '2026-09-20T18:00:00Z',
      jwksUrl: '/.well-known/jwks.json',
    })
    jwks.mockResolvedValue({ keys: [{ kty: 'OKP', crv: 'Ed25519', x: 'abc' }] })
    verificarAssinatura.mockResolvedValue(true)
    renderHash(`/publico/verificar-certificado/${HASH}`)
    await waitFor(() => {
      expect(screen.getByText('Certificado válido')).toBeTruthy()
    })
    expect(screen.getByText('Aluno Dev')).toBeTruthy()
    expect(screen.getByText('Oficina')).toBeTruthy()
    expect(screen.getByText('Ver chave pública')).toBeTruthy()
  })

  it('esconde beneficiário se a assinatura falhar no browser (F0.7-d)', async () => {
    verificarCertificado.mockResolvedValue({
      status: 'VALIDO',
      hashSha256: HASH,
      assinaturaEd25519: 'assinatura',
      beneficiarioNome: 'Aluno Dev',
      atividade: 'Oficina',
      cargaHoraria: 4,
      emitidoEm: '2026-09-20T18:00:00Z',
      jwksUrl: '/.well-known/jwks.json',
    })
    jwks.mockResolvedValue({ keys: [{ kty: 'OKP', crv: 'Ed25519', x: 'abc' }] })
    verificarAssinatura.mockResolvedValue(false)
    renderHash(`/publico/verificar-certificado/${HASH}`)
    await waitFor(() => {
      expect(screen.getByText('Certificado inválido')).toBeTruthy()
    })
    expect(screen.queryByText('Aluno Dev')).toBeNull()
  })
})
