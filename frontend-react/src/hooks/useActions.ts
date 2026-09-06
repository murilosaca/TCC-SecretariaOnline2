import type { HateoasLinks } from '../models/academico'

/**
 * UI cega a perfil (RNF-UX-04 / RF-TR-005):
 * botões e CTAs só existem se o link HATEOAS estiver na resposta.
 */
export function useActions(links?: HateoasLinks | null) {
  return {
    can: (rel: string) => Boolean(links?.[rel]),
    href: (rel: string) => links?.[rel],
  }
}
