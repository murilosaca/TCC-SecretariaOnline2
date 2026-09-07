const KEY = 'so2_device_uuid'

export function obterDeviceUuid(): string {
  const existente = sessionStorage.getItem(KEY)
  if (existente && existente.length >= 8) {
    return existente
  }
  const gerado = crypto.randomUUID()
  sessionStorage.setItem(KEY, gerado)
  return gerado
}
