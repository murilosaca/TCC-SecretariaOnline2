const KEY = 'so2_device_uuid'

let memoria: string | null = null

export function obterDeviceUuid(): string {
  try {
    const existente = localStorage.getItem(KEY)
    if (existente && existente.length >= 8) {
      memoria = existente
      return existente
    }
  } catch {
    // storage bloqueado (modo privado / política) — cai para memória
  }

  if (memoria && memoria.length >= 8) {
    return memoria
  }

  const gerado = crypto.randomUUID()
  memoria = gerado
  try {
    localStorage.setItem(KEY, gerado)
  } catch {
    // permanece só em memória desta aba; não volta a sessionStorage
  }
  return gerado
}
