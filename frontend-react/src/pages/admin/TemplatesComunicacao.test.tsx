import { fireEvent, render, screen, waitFor } from '@testing-library/react'
import { QueryClient, QueryClientProvider } from '@tanstack/react-query'
import { describe, expect, it, vi } from 'vitest'
import { TemplatesComunicacao } from './TemplatesComunicacao'

vi.mock('../../api/templates', () => ({
  templatesApi: {
    listar: () =>
      Promise.resolve({
        content: [{ id: 't1', nome: 'aproveitamento.deferido', assunto: 'Deferido' }],
        page: { number: 0, size: 20, totalElements: 1, totalPages: 1 },
      }),
    buscar: () =>
      Promise.resolve({
        id: 't1',
        nome: 'aproveitamento.deferido',
        assunto: 'Deferido',
        corpo: 'texto',
        variaveis: ['nome', 'protocolo', 'curso', 'link', 'data'],
        revisoes: [
          {
            versao: 2,
            assunto: 'Deferido',
            corpo: 'texto',
            autorId: 'a',
            criadoEm: '2026-10-03T00:00:00Z',
            status: 'CURRENT',
          },
          {
            versao: 1,
            assunto: 'Antigo',
            corpo: 'antigo',
            autorId: 'a',
            criadoEm: '2026-01-01T00:00:00Z',
            status: 'ARCHIVED',
          },
        ],
        _links: { salvar: '/admin/templates-comunicacao/t1/revisoes' },
      }),
    criar: vi.fn(),
    salvar: vi.fn(),
  },
}))

function renderPage() {
  const client = new QueryClient({ defaultOptions: { queries: { retry: false } } })
  return render(
    <QueryClientProvider client={client}>
      <TemplatesComunicacao />
    </QueryClientProvider>,
  )
}

describe('TemplatesComunicacao', () => {
  it('substitui nome e protocolo no preview e marca variável desconhecida', async () => {
    renderPage()
    await screen.findByRole('button', { name: /Versão 2/ })
    const editor = screen.getByLabelText('Markdown')
    fireEvent.change(editor, {
      target: { value: 'Olá {{nome}}, sua solicitação {{protocolo}} foi deferida. {{aluno_email}}' },
    })
    expect(screen.getByLabelText('Pré-visualização').textContent).toContain(
      'Olá João da Silva, sua solicitação SOL-2026-001 foi deferida.',
    )
    const desconhecida = screen.getByText('{{aluno_email}}')
    expect(desconhecida.className).toContain('danger')
    expect(desconhecida.getAttribute('title')).toContain('Variável não reconhecida')
  })

  it('desabilita Salvar na revisão arquivada', async () => {
    renderPage()
    await screen.findByRole('button', { name: 'Salvar' })
    fireEvent.click(await screen.findByRole('button', { name: /Versão 1/ }))
    expect(await screen.findByText('Versão 1 — somente leitura')).toBeTruthy()
    await waitFor(() => expect((screen.getByRole('button', { name: 'Salvar' }) as HTMLButtonElement).disabled).toBe(true))
  })
})
