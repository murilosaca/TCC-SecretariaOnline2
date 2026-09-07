import { authSession } from '../auth/session'

export class ApiError extends Error {
  readonly status: number
  readonly retryAfterSeconds?: number

  constructor(status: number, message: string, retryAfterSeconds?: number) {
    super(message)
    this.status = status
    this.retryAfterSeconds = retryAfterSeconds
  }
}

type ProblemBody = {
  detail?: string
  retryAfterSeconds?: number
}

const PUBLIC_AUTH = ['/auth/login', '/auth/recuperar-senha', '/auth/redefinir-senha', '/auth/refresh']

let refreshInFlight: Promise<boolean> | null = null

async function parse<T>(response: Response): Promise<T> {
  if (!response.ok) {
    let message = `Falha HTTP ${response.status}`
    let retryAfterSeconds: number | undefined
    try {
      const body = (await response.json()) as ProblemBody
      if (typeof body.detail === 'string' && body.detail) {
        message = body.detail
      }
      if (typeof body.retryAfterSeconds === 'number') {
        retryAfterSeconds = body.retryAfterSeconds
      }
    } catch {
      /* corpo vazio ou não JSON */
    }
    throw new ApiError(response.status, message, retryAfterSeconds)
  }
  if (response.status === 204) {
    return undefined as T
  }
  return (await response.json()) as T
}

function query(params: Record<string, string | number | undefined>): string {
  const search = new URLSearchParams()
  Object.entries(params).forEach(([key, value]) => {
    if (value !== undefined && value !== '') {
      search.set(key, String(value))
    }
  })
  const qs = search.toString()
  return qs ? `?${qs}` : ''
}

function headers(body?: unknown): HeadersInit {
  const result: Record<string, string> = { Accept: 'application/json' }
  if (body !== undefined) {
    result['Content-Type'] = 'application/json'
  }
  const token = authSession.getAccessToken()
  if (token) {
    result.Authorization = `Bearer ${token}`
  }
  return result
}

async function request<T>(path: string, init: RequestInit, retried = false): Promise<T> {
  const response = await fetch(path, {
    ...init,
    credentials: 'include',
    headers: { ...headers(init.body), ...(init.headers ?? {}) },
  })
  if (response.status === 401 && !retried && shouldRefresh(path)) {
    const ok = await refreshAccessToken()
    if (ok) {
      return request<T>(path, init, true)
    }
  }
  return parse<T>(response)
}

function shouldRefresh(path: string): boolean {
  const pathname = path.split('?')[0]
  return !PUBLIC_AUTH.includes(pathname)
}

function redirecionarSessaoExpirada() {
  if (typeof window === 'undefined') {
    return
  }
  const path = window.location.pathname
  if (
    path.startsWith('/login') ||
    path.startsWith('/erro/') ||
    path.startsWith('/recuperar-senha') ||
    path.startsWith('/nova-senha') ||
    path.startsWith('/contato') ||
    path.startsWith('/publico/')
  ) {
    return
  }
  window.location.assign('/erro/401')
}

async function refreshAccessToken(): Promise<boolean> {
  if (!refreshInFlight) {
    refreshInFlight = fetch('/auth/refresh', {
      method: 'POST',
      credentials: 'include',
      headers: { Accept: 'application/json' },
    })
      .then(async (response) => {
        if (!response.ok) {
          authSession.setAccessToken(null)
          redirecionarSessaoExpirada()
          return false
        }
        const body = (await response.json()) as { accessToken?: string }
        if (!body.accessToken) {
          authSession.setAccessToken(null)
          redirecionarSessaoExpirada()
          return false
        }
        authSession.setAccessToken(body.accessToken)
        return true
      })
      .finally(() => {
        refreshInFlight = null
      })
  }
  return refreshInFlight
}

export const api = {
  get<T>(path: string, params: Record<string, string | number | undefined> = {}): Promise<T> {
    return request<T>(`${path}${query(params)}`, { method: 'GET' })
  },
  post<T>(path: string, body?: unknown): Promise<T> {
    return request<T>(path, {
      method: 'POST',
      body: body === undefined ? undefined : JSON.stringify(body),
    })
  },
  put<T>(path: string, body: unknown): Promise<T> {
    return request<T>(path, { method: 'PUT', body: JSON.stringify(body) })
  },
  delete(path: string): Promise<void> {
    return request<void>(path, { method: 'DELETE' })
  },
}
