import type { CoordinatorReport, SecretaryReport } from '../models/relatorios'
import { api } from './client'

export type ReportFiltrosQuery = {
  periodo?: string
  curso?: string
}

export const reportsApi = {
  coordinator: (filtros: ReportFiltrosQuery = {}) => {
    const params = new URLSearchParams()
    if (filtros.periodo) {
      params.set('periodo', filtros.periodo)
    }
    if (filtros.curso) {
      params.set('curso', filtros.curso)
    }
    const qs = params.toString()
    return api.get<CoordinatorReport>(`/reports/coordinator${qs ? `?${qs}` : ''}`)
  },
  secretary: (filtros: ReportFiltrosQuery = {}) => {
    const params = new URLSearchParams()
    if (filtros.periodo) {
      params.set('periodo', filtros.periodo)
    }
    if (filtros.curso) {
      params.set('curso', filtros.curso)
    }
    const qs = params.toString()
    return api.get<SecretaryReport>(`/reports/secretary${qs ? `?${qs}` : ''}`)
  },
}
