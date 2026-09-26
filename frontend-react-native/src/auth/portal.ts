import type { HateoasLinks } from '../models/hateoas';

const PREFIXOS_ALUNO = ['/formativas', '/estagios', '/tccs', '/eventos'];

/**
 * Portal do egresso: só `egresso-inicio`, sem `inicio` de aluno.
 */
export function portalEgresso(links?: HateoasLinks | null): boolean {
  return Boolean(links?.['egresso-inicio']) && !links?.inicio;
}

export function inicioDaSessao(links?: HateoasLinks | null): string {
  if (portalEgresso(links)) {
    return links?.['egresso-inicio'] ?? '/egresso/inicio';
  }
  return '/inicio';
}

export function rotaExclusivaAluno(pathname: string): boolean {
  const path = pathname.split('?')[0];
  if (path === '/inicio' || path === '/solicitacoes/nova') {
    return true;
  }
  return PREFIXOS_ALUNO.some((prefix) => path === prefix || path.startsWith(`${prefix}/`));
}

export function rotaBloqueada(pathname: string, links?: HateoasLinks | null): boolean {
  const path = pathname.split('?')[0];
  if (portalEgresso(links) && rotaExclusivaAluno(path)) {
    return true;
  }
  if (!portalEgresso(links) && (path === '/egresso' || path.startsWith('/egresso/'))) {
    return true;
  }
  return false;
}
