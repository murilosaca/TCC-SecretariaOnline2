import { beforeEach, describe, expect, it, vi } from 'vitest'

const KEY = 'so2_device_uuid'

describe('obterDeviceUuid', () => {
  beforeEach(() => {
    localStorage.clear()
    vi.resetModules()
  })

  it('reusa o valor já persistido', async () => {
    localStorage.setItem(KEY, 'uuid-persistido-no-storage')
    const { obterDeviceUuid } = await import('./deviceUuid')
    expect(obterDeviceUuid()).toBe('uuid-persistido-no-storage')
    expect(localStorage.getItem(KEY)).toBe('uuid-persistido-no-storage')
  })

  it('gera e persiste quando ausente', async () => {
    const { obterDeviceUuid } = await import('./deviceUuid')
    const gerado = obterDeviceUuid()
    expect(gerado.length).toBeGreaterThanOrEqual(8)
    expect(localStorage.getItem(KEY)).toBe(gerado)
    expect(obterDeviceUuid()).toBe(gerado)
  })
})
