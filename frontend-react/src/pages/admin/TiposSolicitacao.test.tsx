import { fireEvent, render, screen } from '@testing-library/react'
import { QueryClient, QueryClientProvider } from '@tanstack/react-query'
import { describe, expect, it, vi } from 'vitest'
import { TiposSolicitacao } from './TiposSolicitacao'

vi.mock('../../api/tiposSolicitacao', async () => {
  const atual = await vi.importActual<typeof import('../../api/tiposSolicitacao')>('../../api/tiposSolicitacao')
  return {
    ...atual,
    tiposSolicitacaoApi: {
      listar: () =>
        Promise.resolve({
          content: [
            {
              id: 't1',
              codigo: 'DECLARACAO_SIMPLES',
              nome: 'Declaração simples',
              status: 'PUBLISHED',
              prazoDias: 5,
              versao: 1,
              formSchema: { type: 'object', properties: { motivo: { type: 'string', title: 'Motivo' } } },
              workflowJson: { initial: 'EM_ANALISE', states: { EM_ANALISE: { on: {} } } },
              _links: { publicar: '/admin/tipos-solicitacao/t1/publicar' },
            },
          ],
          page: { number: 0, size: 20, totalElements: 1, totalPages: 1 },
        }),
      criar: vi.fn(),
      publicar: vi.fn(),
      excluir: vi.fn(),
    },
  }
})

describe('TiposSolicitacao', () => {
  it('desabilita Publicar quando o JSON é inválido', async () => {
    const client = new QueryClient({ defaultOptions: { queries: { retry: false } } })
    render(
      <QueryClientProvider client={client}>
        <TiposSolicitacao />
      </QueryClientProvider>,
    )
    fireEvent.click(await screen.findByRole('button', { name: /Declaração simples/ }))
    const publicar = screen.getByRole('button', { name: 'Publicar' }) as HTMLButtonElement
    expect(publicar.disabled).toBe(false)
    fireEvent.change(screen.getByLabelText('form_schema'), { target: { value: '{' } })
    expect((screen.getByRole('button', { name: 'Publicar' }) as HTMLButtonElement).disabled).toBe(true)
    expect(screen.getByText(/JSON inválido na linha/)).toBeTruthy()
  })
})
