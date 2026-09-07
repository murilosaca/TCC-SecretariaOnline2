import { api } from './client'

export type ProtocoloPublico = {
  protocolo: string
  tipoNome: string
  estado: string
  hashSha256: string
}

export const publicoApi = {
  protocolo: (protocolo: string) => api.get<ProtocoloPublico>(`/publico/protocolos/${protocolo}`),
}
