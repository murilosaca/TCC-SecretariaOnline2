const ROTAS_PUBLICAS_AUTH = new Set(['/login', '/recuperar-senha', '/nova-senha'])

export function destinoPosLogin(mustChangePassword: boolean, from: unknown): string {
  if (mustChangePassword) {
    return '/primeiro-acesso'
  }
  if (typeof from !== 'string') {
    return '/inicio'
  }
  if (!from.startsWith('/') || from.startsWith('//')) {
    return '/inicio'
  }
  const path = from.split('?')[0]
  if (ROTAS_PUBLICAS_AUTH.has(path)) {
    return '/inicio'
  }
  return from
}
