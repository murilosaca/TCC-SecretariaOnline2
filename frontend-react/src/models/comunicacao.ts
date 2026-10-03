export type TipoComunicacao = 'INSTITUCIONAL' | 'TURMA' | 'INBOX'

export type PrioridadeComunicacao = 'CRITICAL' | 'HIGH' | 'MEDIUM' | 'LOW'

export type ComunicacaoItem = {
  id: string
  tipo: TipoComunicacao
  titulo: string
  corpo: string
  prioridade: PrioridadeComunicacao
  data: string
  lida: boolean
  _links?: Record<string, string>
}

export type BadgesComunicacao = {
  todos: number
  institucional: number
  turma: number
  inbox: number
}

export type HubComunicacao = {
  content: ComunicacaoItem[]
  badges: BadgesComunicacao
  page: { number: number; size: number; totalElements: number; totalPages: number }
  _links?: Record<string, string>
}

export type AudienciaComunicacao = {
  tipo: 'TURMA' | 'CURSO'
  id: string
  rotulo: string
}

export type PublicarComunicacao = {
  titulo: string
  corpo: string
  audiencia: { tipo: 'TURMA' | 'CURSO'; id: string }
  prioridade: PrioridadeComunicacao
  expiraEm: string | null
}
