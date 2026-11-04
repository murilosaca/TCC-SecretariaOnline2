import { rotuloCodigo } from './rotulos'

export function rotuloEstadoFormativa(estado: string): string {
  return rotuloCodigo(estado)
}

export function rotuloOrigemFormativa(origem: string): string {
  if (origem === 'PRESENCA_VALIDADA') {
    return 'Presença validada'
  }
  if (origem === 'COMPROVANTE') {
    return 'Comprovante'
  }
  return rotuloCodigo(origem)
}
