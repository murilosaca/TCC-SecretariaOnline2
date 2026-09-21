import type { AlunoDashboard } from '../models/dashboard';
import { apiGet } from './client';

export const bffApi = {
  dashboardAluno: () => apiGet<AlunoDashboard>('/bff/dashboard/aluno'),
};
