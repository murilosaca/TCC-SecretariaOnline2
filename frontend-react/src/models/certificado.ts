import type { HateoasLinks, PageResponse } from './academico'

export type Certificado = {
  id: string
  tipo: string
  titulo: string
  cargaHoraria: number
  emitidoEm: string
  hashSha256: string
  _links?: HateoasLinks
}

export type CertificadoPage = PageResponse<Certificado>

export type VerificacaoCertificado = {
  status: string
  hashSha256: string
  assinaturaEd25519: string
  beneficiarioNome: string
  atividade: string
  cargaHoraria: number
  emitidoEm: string
  jwksUrl: string
}

export type Jwks = {
  keys: Array<{
    kty: string
    crv?: string
    x?: string
    kid?: string
    use?: string
    alg?: string
  }>
}
