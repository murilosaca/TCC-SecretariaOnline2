import type { ContatoSecretaria } from '../models/publico';
import { apiGet } from './client';

export const publicoApi = {
  contato: () => apiGet<ContatoSecretaria>('/publico/contato'),
};
