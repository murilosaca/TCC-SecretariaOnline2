import type { HateoasLinks, PageResponse } from './hateoas';

export type Certificado = {
  id: string;
  tipo: string;
  titulo: string;
  cargaHoraria: number;
  emitidoEm: string;
  hashSha256: string;
  _links?: HateoasLinks;
};

export type CertificadoPage = PageResponse<Certificado>;
