import type { HateoasLinks } from '../models/hateoas';

/**
 * UI cega a perfil: CTA só existe se o rel estiver em `_links`.
 * Nunca consultar authorities[] / role para menu ou botão.
 */
export function useActions(links?: HateoasLinks | null) {
  return {
    can: (rel: string) => Boolean(links?.[rel]),
    href: (rel: string) => links?.[rel],
  };
}
