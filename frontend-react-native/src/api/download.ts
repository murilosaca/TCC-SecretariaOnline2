import { Linking } from 'react-native';
import { apiGet } from './client';

export type DownloadUrlPayload = {
  downloadUrl: string;
  expiresInSeconds: number;
};

/** Obtém URL pré-assinada (TTL 15 min) e abre no sistema (PDF). */
export async function baixarViaPresign(href: string): Promise<void> {
  const payload = await apiGet<DownloadUrlPayload>(href);
  const ok = await Linking.canOpenURL(payload.downloadUrl);
  if (!ok) {
    throw new Error('Não foi possível abrir o PDF.');
  }
  await Linking.openURL(payload.downloadUrl);
}
