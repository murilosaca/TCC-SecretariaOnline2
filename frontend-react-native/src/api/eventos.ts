import type { ConfirmarPresencaRequest, EventoPage, SessaoPresenca } from '../models/evento';
import { apiGet, apiPost } from './client';

export const eventosApi = {
  listarMeus: (page = 0, size = 20) => apiGet<EventoPage>('/events', { audience: 'me', page, size }),
  sessao: (id: string) => apiGet<SessaoPresenca>(`/events/${id}/attendance/session`),
  confirmar: (id: string, body: ConfirmarPresencaRequest) =>
    apiPost<SessaoPresenca>(`/events/${id}/attendance/confirm`, body),
};
