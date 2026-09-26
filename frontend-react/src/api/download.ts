import { api } from './client'

export type DownloadUrlPayload = {
  downloadUrl: string
  expiresInSeconds: number
}

/** Obtém URL pré-assinada (TTL 15 min) e dispara o download direto no storage. */
export async function baixarViaPresign(href: string, nomeArquivo: string): Promise<void> {
  const payload = await api.get<DownloadUrlPayload>(href)
  const ancora = document.createElement('a')
  ancora.href = payload.downloadUrl
  ancora.rel = 'noopener'
  ancora.download = nomeArquivo
  ancora.target = '_blank'
  document.body.appendChild(ancora)
  ancora.click()
  ancora.remove()
}
