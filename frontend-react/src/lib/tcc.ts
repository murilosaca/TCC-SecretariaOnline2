export function rotuloEstadoTcc(estado: string): string {
  switch (estado) {
    case 'EM_ELABORACAO':
      return 'Em elaboração'
    case 'SUBMETIDO':
      return 'Submetido'
    case 'CORRECOES_SOLICITADAS':
      return 'Correções solicitadas'
    case 'APROVADO':
      return 'Aprovado'
    case 'REPROVADO':
      return 'Reprovado'
    default:
      return estado
  }
}

export function rotuloPapelBanca(papel: string): string {
  switch (papel) {
    case 'ORIENTADOR':
      return 'Orientador'
    case 'COORIENTADOR':
      return 'Co-orientador'
    case 'BANCA':
      return 'Banca'
    default:
      return papel
  }
}

export function rotuloResultadoTcc(resultado: string): string {
  return rotuloEstadoTcc(resultado)
}

export function dataBr(iso: string): string {
  const [ano, mes, dia] = iso.slice(0, 10).split('-')
  if (!ano || !mes || !dia) {
    return iso
  }
  return `${dia}/${mes}/${ano}`
}

export function entregaProxima(iso: string, hoje: Date = new Date()): boolean {
  const [ano, mes, dia] = iso.slice(0, 10).split('-').map(Number)
  if (!ano || !mes || !dia) {
    return false
  }
  const entrega = new Date(ano, mes - 1, dia)
  const base = new Date(hoje.getFullYear(), hoje.getMonth(), hoje.getDate())
  const limite = new Date(base)
  limite.setDate(limite.getDate() + 14)
  return entrega.getTime() <= limite.getTime()
}
