export type BlocoEstado = 'ok' | 'empty' | 'error'

export function blocoLista<T>(itens: T[] | null | undefined): BlocoEstado {
  if (itens == null) {
    return 'error'
  }
  return itens.length === 0 ? 'empty' : 'ok'
}

export function blocoKpi(valor: number | null | undefined): BlocoEstado {
  return valor == null ? 'error' : 'ok'
}
