import type { HateoasLinks } from '../models/academico'

export type PainelInicio = 'aluno' | 'professor' | 'secretaria'

const PAINEIS: Record<string, PainelInicio> = {
  '/bff/dashboard/aluno': 'aluno',
  '/bff/dashboard/professor': 'professor',
  '/bff/dashboard/secretary': 'secretaria',
}

/** O rel `painel` de GET /auth/me aponta o BFF. A UI não lê authorities. */
export function painelDe(links?: HateoasLinks | null): PainelInicio | null {
  const href = links?.painel
  if (!href) {
    return null
  }
  return PAINEIS[href] ?? null
}
