import type { HateoasLinks } from './academico'

export type Perfil = {
  id: string
  nome: string
  nomeSocial?: string | null
  telefone?: string | null
  emailPessoal?: string | null
  emailInstitucional: string
  grr?: string | null
  identidadeGenero?: string | null
  fotoUrl?: string | null
  _links: HateoasLinks
}

export type PerfilPatch = {
  nomeSocial?: string | null
  telefone?: string | null
  emailPessoal?: string | null
  identidadeGenero?: string | null
}

export type SessaoDispositivo = {
  id: string
  dispositivo: string
  atual: boolean
  criadaEm: string
  expiraEm: string
  _links?: HateoasLinks
}

export type CanalPreferencia = {
  email: boolean
  inApp: boolean
}

export type PrioridadeNotificacao = 'CRITICAL' | 'HIGH' | 'MEDIUM' | 'LOW'

export type NotificacaoPreferencia = {
  canais: Record<PrioridadeNotificacao, CanalPreferencia>
  dndInicio?: string | null
  dndFim?: string | null
  digest: 'IMEDIATO' | 'RESUMO'
  _links: HateoasLinks
}

export const PRIORIDADES: { id: PrioridadeNotificacao; label: string }[] = [
  { id: 'CRITICAL', label: 'Crítica' },
  { id: 'HIGH', label: 'Alta' },
  { id: 'MEDIUM', label: 'Média' },
  { id: 'LOW', label: 'Baixa' },
]

export const IDENTIDADES_GENERO = [
  { value: '', label: 'Não informar' },
  { value: 'FEMININO', label: 'Feminino' },
  { value: 'MASCULINO', label: 'Masculino' },
  { value: 'NAO_BINARIO', label: 'Não-binário' },
  { value: 'OUTRO', label: 'Outro' },
]
