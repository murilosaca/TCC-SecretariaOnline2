import { api } from './client'
import type { VerificacaoCertificado, Jwks } from '../models/certificado'

export type ProtocoloPublico = {
  protocolo: string
  tipoNome: string
  estado: string
  hashSha256: string
}

export const publicoApi = {
  protocolo: (protocolo: string) => api.get<ProtocoloPublico>(`/publico/protocolos/${protocolo}`),
  verificarCertificado: (hash: string) =>
    api.get<VerificacaoCertificado>(`/publico/certificados/${hash}/verificacao`),
  jwks: () => api.get<Jwks>('/.well-known/jwks.json'),
}

