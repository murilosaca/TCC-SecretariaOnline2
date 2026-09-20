import type { HateoasLinks, PageResponse } from './academico'

export type AttendanceMode = 'SECRET_SINGLE' | 'SECRET_DUAL' | 'QR_SINGLE' | 'QR_DUAL'

export function isQrMode(mode?: string | null): boolean {
  return mode === 'QR_SINGLE' || mode === 'QR_DUAL'
}

export function isDualMode(mode?: string | null): boolean {
  return mode === 'SECRET_DUAL' || mode === 'QR_DUAL'
}

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
  attendanceMode: AttendanceMode
}

export interface HostSessao {
  eventoId: string
  titulo: string
  attendanceMode: string
  estado: string
  janelaAtiva: boolean
  janelaExpira: string | null
  pin?: string | null
  token?: string | null
  tokenExpira?: string | null
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
  pin?: string
  token?: string
  deviceUuid: string
  fase: string
}

export type EventoPage = PageResponse<Evento>
