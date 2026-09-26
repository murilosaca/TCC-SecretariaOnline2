import type { Certificado, CertificadoPage } from '../models/certificado';
import { apiGet } from './client';
import { baixarViaPresign } from './download';

export const certificadosApi = {
  listarMeus: (page = 0, size = 20) =>
    apiGet<CertificadoPage>('/certificates', { beneficiario: 'me', page, size }),
  baixar: (href: string) => baixarViaPresign(href),
};
