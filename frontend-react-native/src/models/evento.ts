import type { HateoasLinks, PageResponse } from './hateoas';

export function isQrMode(mode?: string | null): boolean {
  return mode === 'QR_SINGLE' || mode === 'QR_DUAL';
}

export type Evento = {
  id: string;
  titulo: string;
  inicioEm: string;
  fimEm: string;
  cargaHoraria: number;
  attendanceMode: string;
  estado: string;
  situacaoPresenca: string;
  janelaAtiva: boolean;
  _links?: HateoasLinks;
};

export type SessaoPresenca = {
  eventoId: string;
  titulo: string;
  attendanceMode: string;
  estado: string;
  situacaoPresenca: string;
  janelaAtiva: boolean;
  janelaExpira: string | null;
  faseDisponivel: string | null;
  _links?: HateoasLinks;
};

export type ConfirmarPresencaRequest = {
  pin?: string;
  token?: string;
  deviceUuid: string;
  fase: string;
};

export type EventoPage = PageResponse<Evento>;
