import { fireEvent, render, screen } from '@testing-library/react'
import { QueryClient, QueryClientProvider } from '@tanstack/react-query'
import { describe, expect, it, vi } from 'vitest'
import { Suporte } from './Suporte'
import { suporteApi } from '../../api/suporte'

vi.mock('../../api/suporte', () => ({
  suporteApi: {
    faq: vi.fn().mockResolvedValue([
      { id: '1', pergunta: 'Como redefinir minha senha?', resposta: 'Use recuperar senha.' },
      { id: '2', pergunta: 'Prazo de deliberação?', resposta: 'O prazo está no tipo.' },
    ]),
    abrir: vi.fn(),
  },
}))

function renderPage() {
  const client = new QueryClient({ defaultOptions: { queries: { retry: false } } })
  return render(
    <QueryClientProvider client={client}>
      <Suporte />
    </QueryClientProvider>,
  )
}

describe('Suporte', () => {
  it('não tem botão de excluir e não envia assunto vazio', async () => {
    renderPage()
    const senha = await screen.findByRole('button', { name: 'Como redefinir minha senha?' })
    expect(senha.getAttribute('aria-expanded')).toBe('true')
    expect(screen.queryByRole('button', { name: /excluir/i })).toBeNull()
    fireEvent.click(screen.getByRole('button', { name: 'Enviar ticket' }))
    expect(screen.getByText('Assunto é obrigatório')).toBeTruthy()
    expect(suporteApi.abrir).not.toHaveBeenCalled()
  })

  it('mostra o protocolo e limpa o formulário', async () => {
    vi.mocked(suporteApi.abrir).mockResolvedValue({ protocolo: 'PROT-2026-00042', tipoCodigo: 'SUPORTE_TECNICO' })
    renderPage()
    await screen.findByRole('button', { name: 'Como redefinir minha senha?' })
    fireEvent.change(screen.getByLabelText('Assunto'), { target: { value: 'Dúvida sobre prazo' } })
    fireEvent.change(screen.getByLabelText('Mensagem'), { target: { value: 'Qual é o prazo?' } })
    fireEvent.click(screen.getByRole('button', { name: 'Enviar ticket' }))
    expect(await screen.findByText(/PROT-2026-00042/)).toBeTruthy()
    expect((screen.getByLabelText('Assunto') as HTMLInputElement).value).toBe('')
    expect((screen.getByLabelText('Mensagem') as HTMLTextAreaElement).value).toBe('')
  })
})
