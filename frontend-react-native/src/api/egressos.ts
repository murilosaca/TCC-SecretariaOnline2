import type { EgressoPainel } from '../models/egresso';
import { apiGet } from './client';
import { baixarViaPresign } from './download';

export const egressosApi = {
  painel: () => apiGet<EgressoPainel>('/egressos/me'),
  baixar: (href: string) => baixarViaPresign(href),
};
