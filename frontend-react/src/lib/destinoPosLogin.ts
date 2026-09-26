import type { HateoasLinks } from '../models/academico'
import { inicioDaSessao, rotaExclusivaAluno } from '../auth/portal'

const ROTAS_PUBLICAS_AUTH = new Set(['/login', '/recuperar-senha', '/nova-senha', '/redefinir-senha'])

export function destinoPosLogin(
  mustChangePassword: boolean,
  from: unknown,
  links?: HateoasLinks | null,
): string {
  const home = inicioDaSessao(links)
  if (mustChangePassword) {
    return '/primeiro-acesso'
  }
  if (typeof from !== 'string') {
    return home
  }
  if (!from.startsWith('/') || from.startsWith('//')) {
    return home
  }
  const path = from.split('?')[0]
  if (ROTAS_PUBLICAS_AUTH.has(path)) {
    return home
  }
  if (rotaExclusivaAluno(path) && home.startsWith('/egresso')) {
    return home
  }
  return from
}
