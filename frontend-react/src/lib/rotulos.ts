/** Rótulos amigáveis para códigos de domínio (estado, situação, etc.). */
const ROTULOS: Record<string, string> = {
  // Solicitações
  ABERTA: 'Aberta',
  EM_ANALISE: 'Em análise',
  EM_AJUSTE: 'Em ajuste',
  DELIBERADA: 'Deliberada',
  DEFERIDA: 'Deferida',
  INDEFERIDA: 'Indeferida',
  CONCLUIDA: 'Concluída',
  // Eventos / presença
  AGENDADO: 'Agendado',
  EM_ANDAMENTO: 'Em andamento',
  CONCLUIDO: 'Concluído',
  PENDENTE: 'Pendente',
  PARCIAL: 'Parcial',
  COMPLETA: 'Completa',
  // Formativas
  PENDENTE_CONFIRMACAO: 'Aguardando confirmação',
  AGUARDANDO_CAAF: 'Aguardando CAAF',
  APROVADA: 'Aprovada',
  CANCELADA: 'Cancelada',
  // SLA / misc
  ATRASADO: 'Atrasado',
  NO_PRAZO: 'No prazo',
  TODOS: 'Todos',
}

/**
 * Converte código de máquina (`EM_AJUSTE`) em texto para pessoa (`Em ajuste`).
 * Códigos desconhecidos: underscores viram espaço.
 */
export function rotuloCodigo(codigo: string | null | undefined): string {
  if (!codigo) {
    return '—'
  }
  const conhecido = ROTULOS[codigo]
  if (conhecido) {
    return conhecido
  }
  return codigo.replaceAll('_', ' ')
}
