import type { Certificado, CertificadoPage } from '../models/certificado'
import { api } from './client'

export const certificadosApi = {
  listarMeus: (page = 0, size = 20) =>
    api.get<CertificadoPage>('/certificates', { beneficiario: 'me', page, size }),
  obter: (id: string) => api.get<Certificado>(`/certificates/${id}`),
  baixar: (href: string) => api.getBlob(href),
}
