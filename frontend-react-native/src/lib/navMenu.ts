import type { HateoasLinks } from '../models/hateoas';

/**
 * Itens de menu com tela nativa nesta fatia (aluno/egresso + P0).
 * Deliberação, CAAF, COE, F5 e o hub de comunicação (fatia 23) ficam de fora do app.
 * Visibilidade só via `_links` de GET /auth/me.
 */
export const NAV_ITENS: { rel: string; label: string }[] = [
  { rel: 'inicio', label: 'Início' },
  { rel: 'egresso-inicio', label: 'Início' },
  { rel: 'solicitacoes', label: 'Solicitações' },
  { rel: 'eventos', label: 'Eventos' },
  { rel: 'formativas', label: 'Formativas' },
  { rel: 'certificados', label: 'Certificados' },
  { rel: 'estagios', label: 'Estágios' },
  { rel: 'tccs', label: 'TCCs' },
  { rel: 'contato', label: 'Contato' },
];

export function navItensVisiveis(links?: HateoasLinks | null): { rel: string; label: string; href: string }[] {
  if (!links) {
    return [];
  }
  return NAV_ITENS.flatMap((item) => {
    const href = links[item.rel];
    return href ? [{ rel: item.rel, label: item.label, href }] : [];
  });
}
