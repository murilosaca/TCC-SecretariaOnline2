import { api } from './client'

export type BuscaHit = {
  id: string
  titulo: string
  subtitulo: string
  href: string
}

export type BuscaResultado = {
  q: string
  alunos: BuscaHit[]
  solicitacoes: BuscaHit[]
  eventos: BuscaHit[]
  usuarios: BuscaHit[]
}

export function buscarGlobal(q: string, signal: AbortSignal): Promise<BuscaResultado> {
  return api.get<BuscaResultado>('/search', { q }, signal)
}
