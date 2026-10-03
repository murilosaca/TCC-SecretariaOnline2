export const VARIAVEIS_TEMPLATE = ['nome', 'protocolo', 'curso', 'link', 'data'] as const

const EXEMPLOS: Record<(typeof VARIAVEIS_TEMPLATE)[number], string> = {
  nome: 'João da Silva',
  protocolo: 'SOL-2026-001',
  curso: 'TADS',
  link: 'https://so2.ufpr.br',
  data: '03/10/2026',
}

export type PartePreview =
  | { tipo: 'texto'; texto: string }
  | { tipo: 'variavel'; texto: string; bruto: string; desconhecida: boolean }

const TOKEN = /\{\{\s*([a-zA-Z0-9_]+)\s*\}\}/g

export function partesDoPreview(corpo: string): PartePreview[] {
  const partes: PartePreview[] = []
  let cursor = 0
  for (const match of corpo.matchAll(TOKEN)) {
    const inicio = match.index ?? 0
    if (inicio > cursor) {
      partes.push({ tipo: 'texto', texto: corpo.slice(cursor, inicio) })
    }
    const chave = match[1]
    const conhecida = (VARIAVEIS_TEMPLATE as readonly string[]).includes(chave)
    partes.push({
      tipo: 'variavel',
      texto: conhecida ? EXEMPLOS[chave as keyof typeof EXEMPLOS] : match[0],
      bruto: match[0],
      desconhecida: !conhecida,
    })
    cursor = inicio + match[0].length
  }
  if (cursor < corpo.length) {
    partes.push({ tipo: 'texto', texto: corpo.slice(cursor) })
  }
  return partes
}

export function textoPreview(corpo: string): string {
  return partesDoPreview(corpo)
    .map((parte) => (parte.tipo === 'texto' ? parte.texto : parte.texto))
    .join('')
}
