import type { Tcc, TccPage } from '../models/tcc';
import { apiGet, apiPostForm } from './client';
import { baixarViaPresign } from './download';
import type { NativeUploadFile } from './upload';

export const tccsApi = {
  listarMeus: (estado?: string, page = 0, size = 20) =>
    apiGet<TccPage>('/tccs', { aluno: 'me', estado: estado || undefined, page, size }),
  obter: (id: string) => apiGet<Tcc>(`/tccs/${id}`),
  enviarVersaoFinal: (id: string, arquivo: NativeUploadFile) => {
    const form = new FormData();
    form.append('arquivo', {
      uri: arquivo.uri,
      name: arquivo.name,
      type: arquivo.mimeType,
    } as unknown as Blob);
    return apiPostForm<Tcc>(`/tccs/${id}/versao-final`, form);
  },
  baixar: (href: string) => baixarViaPresign(href),
};
