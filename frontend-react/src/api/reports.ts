import type { CoordinatorReport } from '../models/relatorios'
import { api } from './client'

export type CoordinatorReportFiltrosQuery = {
  periodo?: string
  curso?: string
}

export const reportsApi = {
  coordinator: (filtros: CoordinatorReportFiltrosQuery = {}) => {
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
}
