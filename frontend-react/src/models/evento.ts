import type { HateoasLinks, PageResponse } from './academico'

export interface Evento {
  id: string
  idAnfitriao?: string | null
  titulo: string
  inicioEm: string
  fimEm: string
  cargaHoraria: number
  attendanceMode: string
  estado: string
  situacaoPresenca: string
  janelaAtiva: boolean
  _links?: HateoasLinks
}

export interface CriarEventoRequest {
  titulo: string
  inicioEm: string
  fimEm: string
  cargaHoraria: number
}

export interface HostSessao {
  eventoId: string
  titulo: string
  attendanceMode: string
  estado: string
  janelaAtiva: boolean
  janelaExpira: string | null
  pin?: string | null
  presentes: number
  _links?: HateoasLinks
}

export interface SessaoPresenca {
  eventoId: string
  titulo: string
  attendanceMode: string
  estado: string
  situacaoPresenca: string
  janelaAtiva: boolean
  janelaExpira: string | null
  faseDisponivel: string | null
  _links?: HateoasLinks
}

export interface ConfirmarPresencaRequest {
  pin: string
  deviceUuid: string
  fase: string
}

export type EventoPage = PageResponse<Evento>
