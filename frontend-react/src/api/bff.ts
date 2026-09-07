import type { AlunoDashboard } from '../models/dashboard'
import { api } from './client'

export const bffApi = {
  dashboardAluno: () => api.get<AlunoDashboard>('/bff/dashboard/aluno'),
}
