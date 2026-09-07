import type {
  ConfirmarPresencaRequest,
  CriarEventoRequest,
  Evento,
  EventoPage,
  HostSessao,
  SessaoPresenca,
} from '../models/evento'
import { api } from './client'

export const eventosApi = {
  listarMeus: (page = 0, size = 20) =>
    api.get<EventoPage>('/events', { audience: 'me', page, size }),
  listarDoAnfitriao: (page = 0, size = 20) =>
    api.get<EventoPage>('/events', { mine: 'true', page, size }),
  criar: (body: CriarEventoRequest) => api.post<Evento>('/events', body),
  obter: (id: string) => api.get<Evento>(`/events/${id}`),
  sessao: (id: string) => api.get<SessaoPresenca>(`/events/${id}/attendance/session`),
  sessaoHost: (id: string) => api.get<HostSessao>(`/events/${id}/attendance/host-session`),
  abrirJanelaEntrada: (id: string) => api.post<HostSessao>(`/events/${id}/attendance/windows/entry`),
  encerrar: (id: string) => api.post<HostSessao>(`/events/${id}/encerrar`),
  confirmar: (id: string, body: ConfirmarPresencaRequest) =>
    api.post<SessaoPresenca>(`/events/${id}/attendance/confirm`, body),
}
