export function rotuloEstadoFormativa(estado: string): string {
  if (estado === 'PENDENTE_CONFIRMACAO') {
    return 'Aguardando confirmação'
  }
  if (estado === 'AGUARDANDO_CAAF') {
    return 'Aguardando CAAF'
  }
  if (estado === 'APROVADA') {
    return 'Aprovada'
  }
  if (estado === 'INDEFERIDA') {
    return 'Indeferida'
  }
  if (estado === 'CANCELADA') {
    return 'Cancelada'
  }
  return estado
}

export function rotuloOrigemFormativa(origem: string): string {
  if (origem === 'PRESENCA_VALIDADA') {
    return 'Presença validada'
  }
  return origem
}
