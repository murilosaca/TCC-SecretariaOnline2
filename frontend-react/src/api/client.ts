export class ApiError extends Error {
  readonly status: number

  constructor(status: number, message: string) {
    super(message)
    this.status = status
  }
}

async function parse<T>(response: Response): Promise<T> {
  if (!response.ok) {
    throw new ApiError(response.status, `Falha HTTP ${response.status}`)
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

export const api = {
  get<T>(path: string, params: Record<string, string | number | undefined> = {}): Promise<T> {
    return fetch(`${path}${query(params)}`).then((r) => parse<T>(r))
  },
  post<T>(path: string, body: unknown): Promise<T> {
    return fetch(path, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(body),
    }).then((r) => parse<T>(r))
  },
  put<T>(path: string, body: unknown): Promise<T> {
    return fetch(path, {
      method: 'PUT',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(body),
    }).then((r) => parse<T>(r))
  },
  delete(path: string): Promise<void> {
    return fetch(path, { method: 'DELETE' }).then((r) => parse<void>(r))
  },
}
