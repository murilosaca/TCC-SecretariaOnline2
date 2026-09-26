import type { AlunoDashboard, ProfessorDashboard } from '../models/dashboard'
import { api } from './client'

export const bffApi = {
  dashboardAluno: () => api.get<AlunoDashboard>('/bff/dashboard/aluno'),
  dashboardProfessor: () => api.get<ProfessorDashboard>('/bff/dashboard/professor'),
}
