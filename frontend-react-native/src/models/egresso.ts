import type { HateoasLinks } from './hateoas';

export type EgressoCertificado = {
  id: string;
  titulo: string;
  tipo: string;
  emitidoEm: string;
  hashSha256: string;
  _links?: HateoasLinks;
};

export type EgressoDiploma = {
  numero: string;
  emitidoEm: string;
  _links?: HateoasLinks;
};

export type EgressoColacao = {
  data: string | null;
  turma: string | null;
};

export type EgressoPainel = {
  nome: string;
  curso: string | null;
  concluidoEm: string | null;
  kpis: {
    horasFormativasValidadas: number | null;
    certificadosEmitidos: number | null;
    situacaoDiploma: string | null;
  };
  diploma: EgressoDiploma | null;
  colacao: EgressoColacao | null;
  certificados: EgressoCertificado[];
  _links?: HateoasLinks;
};
