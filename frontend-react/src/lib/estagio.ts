export function rotuloSituacaoEstagio(situacao: string): string {
  switch (situacao) {
    case 'ATIVO':
      return 'Ativo'
    case 'PENDENTE':
      return 'Pendente'
    case 'CONCLUIDO':
      return 'Concluído'
    default:
      return situacao
  }
}

export function rotuloTipoDocumento(tipo: string): string {
  switch (tipo) {
    case 'TCE':
      return 'TCE'
    case 'RELATORIO_FINAL':
      return 'Relatório final'
    default:
      return tipo
  }
}

export function rotuloEstadoDocumento(estado: string): string {
  switch (estado) {
    case 'PENDENTE':
      return 'Pendente'
    case 'AGUARDANDO_PARECER':
      return 'Aguardando parecer'
    case 'APROVADO':
      return 'Aprovado'
    case 'REPROVADO':
      return 'Reprovado'
    default:
      return estado
  }
}

export function rotuloAcaoParecer(acao: string): string {
  switch (acao) {
    case 'APROVADO':
      return 'Aprovado'
    case 'REPROVADO':
      return 'Reprovado'
    default:
      return acao
  }
}

export function vigencia(inicio: string, fim: string): string {
  return `${dataBr(inicio)} – ${dataBr(fim)}`
}

export function dataBr(iso: string): string {
  const [ano, mes, dia] = iso.slice(0, 10).split('-')
  if (!ano || !mes || !dia) {
    return iso
  }
  return `${dia}/${mes}/${ano}`
}
