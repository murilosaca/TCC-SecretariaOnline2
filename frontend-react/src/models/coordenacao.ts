import type { HateoasLinks } from './academico'

export interface ConfigCurso {
  id: string
  nome: string
  sigla: string
  horasFormativasMinimas: number
  duracaoCalendario: number
  bancaMembrosExternos: number
  bancaModalidade: 'PRESENCIAL' | 'REMOTO' | 'HIBRIDO'
  regimento: string
  _links?: HateoasLinks
}

export interface ConfigCursoPatch {
  horasFormativasMinimas?: number
  duracaoCalendario?: number
  bancaMembrosExternos?: number
  bancaModalidade?: ConfigCurso['bancaModalidade']
  regimento?: string
}
