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
  CRIADA: 'Criada',
  TRANSICAO: 'Transição',
  // Eventos / presença
  AGENDADO: 'Agendado',
  EM_ANDAMENTO: 'Em andamento',
  CONCLUIDO: 'Concluído',
  PENDENTE: 'Pendente',
  PARCIAL: 'Parcial',
  COMPLETA: 'Completa',
  SECRET_SINGLE: 'PIN Único',
  SECRET_DUAL: 'PIN Duplo',
  QR_SINGLE: 'QR Único',
  QR_DUAL: 'QR Duplo',
  // Formativas
  PENDENTE_CONFIRMACAO: 'Aguardando confirmação',
  AGUARDANDO_CAAF: 'Aguardando CAAF',
  APROVADA: 'Aprovada',
  CANCELADA: 'Cancelada',
  // Tipos de solicitação (filtros / fallbacks)
  DECLARACAO_SIMPLES: 'Declaração simples',
  AUTORIZACAO_IMAGEM: 'Autorização de uso de imagem',
  SUPORTE_TECNICO: 'Suporte técnico',
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
