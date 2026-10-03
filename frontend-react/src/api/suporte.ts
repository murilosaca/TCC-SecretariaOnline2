import { api } from './client'

export type FaqItem = {
  id: string
  pergunta: string
  resposta: string
}

export type TicketAberto = {
  protocolo: string
  tipoCodigo: string
}

export const suporteApi = {
  faq: () => api.get<FaqItem[]>('/suporte/faq'),
  abrir: (assunto: string, mensagem: string) =>
    api.post<TicketAberto>('/requests', {
      tipoCodigo: 'SUPORTE_TECNICO',
      payload: { assunto, mensagem },
    }),
}
