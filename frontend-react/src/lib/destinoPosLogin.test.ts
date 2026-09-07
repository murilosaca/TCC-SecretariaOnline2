import { describe, expect, it } from 'vitest'
import { destinoPosLogin } from './destinoPosLogin'

describe('destinoPosLogin', () => {
  it('primeiro acesso ignora from e vai para /primeiro-acesso', () => {
    expect(destinoPosLogin(true, '/eventos/1/presenca')).toBe('/primeiro-acesso')
  })

  it('volta para rota interna segura após login', () => {
    expect(destinoPosLogin(false, '/eventos/abc/presenca')).toBe('/eventos/abc/presenca')
  })

  it('recusa from inseguro e cai em /inicio', () => {
    expect(destinoPosLogin(false, undefined)).toBe('/inicio')
    expect(destinoPosLogin(false, '//evil.example')).toBe('/inicio')
    expect(destinoPosLogin(false, '/login')).toBe('/inicio')
    expect(destinoPosLogin(false, '/recuperar-senha')).toBe('/inicio')
    expect(destinoPosLogin(false, '/nova-senha')).toBe('/inicio')
    expect(destinoPosLogin(false, 'https://evil.example')).toBe('/inicio')
  })
})
