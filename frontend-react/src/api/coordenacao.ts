import type { ConfigCurso, ConfigCursoPatch } from '../models/coordenacao'
import { api } from './client'

export const coordenacaoApi = {
  obterConfig: (id: string) => api.get<ConfigCurso>(`/coordenacao/cursos/${id}/config`),
  atualizarConfig: (href: string, body: ConfigCursoPatch) => api.patch<ConfigCurso>(href, body),
}
