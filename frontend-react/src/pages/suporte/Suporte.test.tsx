import { fireEvent, render, screen } from '@testing-library/react'
import { QueryClient, QueryClientProvider } from '@tanstack/react-query'
import { describe, expect, it, vi } from 'vitest'
import { Suporte } from './Suporte'
import { suporteApi } from '../../api/suporte'

vi.mock('../../api/suporte', () => ({
  suporteApi: {
    faq: vi.fn().mockResolvedValue([
      { id: '1', pergunta: 'Como redefinir minha senha?', resposta: 'Use recuperar senha.' },
      { id: '2', pergunta: 'Qual é o prazo de deliberação?', resposta: 'O prazo está no tipo.' },
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

async function irParaTicket() {
  fireEvent.click(await screen.findByRole('tab', { name: 'Abrir ticket' }))
}

describe('Suporte', () => {
  it('não tem botão de excluir e não envia assunto vazio', async () => {
    renderPage()
    const senha = await screen.findByRole('button', { name: 'Como redefinir minha senha?' })
    expect(senha.getAttribute('aria-expanded')).toBe('false')
    expect(screen.queryByRole('button', { name: /excluir/i })).toBeNull()
    await irParaTicket()
    fireEvent.click(screen.getByRole('button', { name: 'Enviar ticket' }))
    expect(screen.getByText('Assunto é obrigatório')).toBeTruthy()
    expect(suporteApi.abrir).not.toHaveBeenCalled()
  })

  it('permite expandir vários itens do FAQ ao mesmo tempo', async () => {
    renderPage()
    const senha = await screen.findByRole('button', { name: 'Como redefinir minha senha?' })
    const prazo = screen.getByRole('button', { name: 'Qual é o prazo de deliberação?' })
    fireEvent.click(senha)
    fireEvent.click(prazo)
    expect(senha.getAttribute('aria-expanded')).toBe('true')
    expect(prazo.getAttribute('aria-expanded')).toBe('true')
    expect(screen.getByText('Use recuperar senha.')).toBeTruthy()
    expect(screen.getByText('O prazo está no tipo.')).toBeTruthy()
  })

  it('mostra o protocolo e limpa o formulário', async () => {
    vi.mocked(suporteApi.abrir).mockResolvedValue({ protocolo: 'PROT-2026-00042', tipoCodigo: 'SUPORTE_TECNICO' })
    renderPage()
    await irParaTicket()
    fireEvent.change(screen.getByLabelText('Assunto'), { target: { value: 'Dúvida sobre prazo' } })
    fireEvent.change(screen.getByLabelText('Mensagem'), { target: { value: 'Qual é o prazo?' } })
    fireEvent.click(screen.getByRole('button', { name: 'Enviar ticket' }))
    expect(await screen.findByText(/PROT-2026-00042/)).toBeTruthy()
    expect((screen.getByLabelText('Assunto') as HTMLInputElement).value).toBe('')
    expect((screen.getByLabelText('Mensagem') as HTMLTextAreaElement).value).toBe('')
  })

  it('troca entre as abas FAQ e Abrir ticket', async () => {
    renderPage()
    expect((await screen.findByRole('tab', { name: 'FAQ' })).getAttribute('aria-selected')).toBe('true')
    expect(await screen.findByRole('button', { name: 'Como redefinir minha senha?' })).toBeTruthy()
    await irParaTicket()
    expect(screen.getByRole('tab', { name: 'Abrir ticket' }).getAttribute('aria-selected')).toBe('true')
    expect(screen.getByLabelText('Assunto')).toBeTruthy()
    expect(screen.queryByRole('button', { name: 'Como redefinir minha senha?' })).toBeNull()
  })
})
