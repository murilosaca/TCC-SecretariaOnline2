import axios, { type AxiosError, type InternalAxiosRequestConfig } from 'axios';
import { authSession } from '../auth/session';
import { refreshStore } from '../auth/tokenStore';
import { API_BASE_URL } from '../config';
import { NATIVE_CLIENT_HEADER, NATIVE_CLIENT_VALUE } from './nativeClient';

export class ApiError extends Error {
  readonly status: number;
  readonly retryAfterSeconds?: number;

  constructor(status: number, message: string, retryAfterSeconds?: number) {
    super(message);
    this.status = status;
    this.retryAfterSeconds = retryAfterSeconds;
  }
}

type ProblemBody = {
  detail?: string;
  retryAfterSeconds?: number;
};

const PUBLIC_AUTH = ['/auth/login', '/auth/recuperar-senha', '/auth/redefinir-senha', '/auth/refresh'];

let refreshInFlight: Promise<boolean> | null = null;
let onUnauthorized: (() => void) | null = null;

export function setOnUnauthorized(handler: (() => void) | null): void {
  onUnauthorized = handler;
}

export const apiClient = axios.create({
  baseURL: API_BASE_URL,
  timeout: 20_000,
  headers: {
    Accept: 'application/json',
    'Content-Type': 'application/json',
    [NATIVE_CLIENT_HEADER]: NATIVE_CLIENT_VALUE,
  },
  withCredentials: false,
});

apiClient.interceptors.request.use((config) => {
  const token = authSession.getAccessToken();
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  config.headers[NATIVE_CLIENT_HEADER] = NATIVE_CLIENT_VALUE;
  return config;
});

apiClient.interceptors.response.use(
  (response) => response,
  async (error: AxiosError) => {
    const status = error.response?.status;
    const original = error.config as (InternalAxiosRequestConfig & { _retried?: boolean }) | undefined;
    const path = pathOf(original);
    if (status === 401 && original && !original._retried && shouldRefresh(path)) {
      original._retried = true;
      const ok = await refreshAccessToken();
      if (ok) {
        const token = authSession.getAccessToken();
        if (token) {
          original.headers.Authorization = `Bearer ${token}`;
        }
        return apiClient.request(original);
      }
    }
    throw toApiError(error);
  },
);

function pathOf(config?: InternalAxiosRequestConfig): string {
  const url = config?.url ?? '';
  const withoutBase = url.replace(API_BASE_URL, '');
  return withoutBase.split('?')[0] || '';
}

function shouldRefresh(path: string): boolean {
  return !PUBLIC_AUTH.includes(path);
}

async function refreshAccessToken(): Promise<boolean> {
  if (!refreshInFlight) {
    refreshInFlight = (async () => {
      const stored = await refreshStore.get();
      if (!stored) {
        authSession.setAccessToken(null);
        onUnauthorized?.();
        return false;
      }
      try {
        const response = await axios.post(
          `${API_BASE_URL}/auth/refresh`,
          { refreshToken: stored },
          {
            headers: {
              Accept: 'application/json',
              'Content-Type': 'application/json',
              [NATIVE_CLIENT_HEADER]: NATIVE_CLIENT_VALUE,
            },
            timeout: 20_000,
          },
        );
        const access = response.data?.accessToken as string | undefined;
        const nextRefresh = response.data?.refreshToken as string | undefined;
        if (!access) {
          authSession.setAccessToken(null);
          await refreshStore.clear();
          onUnauthorized?.();
          return false;
        }
        authSession.setAccessToken(access);
        if (nextRefresh) {
          await refreshStore.set(nextRefresh);
        }
        return true;
      } catch {
        authSession.setAccessToken(null);
        await refreshStore.clear();
        onUnauthorized?.();
        return false;
      }
    })().finally(() => {
      refreshInFlight = null;
    });
  }
  return refreshInFlight;
}

function toApiError(error: AxiosError): ApiError {
  const status = error.response?.status ?? 0;
  const body = error.response?.data as ProblemBody | undefined;
  const message =
    typeof body?.detail === 'string' && body.detail
      ? body.detail
      : status
        ? `Falha HTTP ${status}`
        : 'Não foi possível conectar à API.';
  return new ApiError(status, message, body?.retryAfterSeconds);
}

export async function apiGet<T>(path: string, params?: Record<string, string | number | undefined>): Promise<T> {
  const response = await apiClient.get<T>(path, { params: compact(params) });
  return response.data;
}

export async function apiPost<T>(path: string, body?: unknown): Promise<T> {
  const response = await apiClient.post<T>(path, body);
  return response.data;
}

function compact(params?: Record<string, string | number | undefined>) {
  if (!params) {
    return undefined;
  }
  const result: Record<string, string | number> = {};
  for (const [key, value] of Object.entries(params)) {
    if (value !== undefined && value !== '') {
      result[key] = value;
    }
  }
  return result;
}
