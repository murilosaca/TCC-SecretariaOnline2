let accessToken: string | null = null

export const authSession = {
  getAccessToken(): string | null {
    return accessToken
  },
  setAccessToken(token: string | null): void {
    accessToken = token
  },
}
