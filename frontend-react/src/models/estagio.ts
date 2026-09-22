import type { HateoasLinks, PageResponse } from './academico'

export type EstagioSituacao = 'ATIVO' | 'PENDENTE' | 'CONCLUIDO'

export type EstadoDocumentoEstagio = 'PENDENTE' | 'AGUARDANDO_PARECER' | 'APROVADO' | 'REPROVADO'

export type DocumentoEstagio = {
  id: string
  tipo: string
  obrigatorio: boolean
  estado: EstadoDocumentoEstagio
  nomeArquivo?: string | null
  tamanho?: number | null
  enviadoEm?: string | null
  _links?: HateoasLinks
}

export type ParecerEstagio = {
  id: string
  idDocumento: string
  tipoDocumento: string
  idAutor: string
  autorRotulo?: string | null
  acao: string
  texto: string
  createdAt: string
}

export type Estagio = {
  id: string
  idAluno: string
  alunoNome?: string | null
  idCurso: string
  idOrientador?: string | null
  orientadorRotulo?: string | null
  empresa: string
  supervisor: string
  inicio: string
  fim: string
  situacao: EstagioSituacao
  documentoPendente?: string | null
  documentos: DocumentoEstagio[]
  pareceres: ParecerEstagio[]
  _links?: HateoasLinks
}

export type EstagioPage = PageResponse<Estagio>
