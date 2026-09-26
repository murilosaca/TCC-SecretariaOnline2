import type { Estagio, EstagioPage } from '../models/estagio';
import { apiGet, apiPost, apiPostForm } from './client';
import { baixarViaPresign } from './download';
import type { NativeUploadFile } from './upload';

export const estagiosApi = {
  listarMeus: (situacao?: string, page = 0, size = 20) =>
    apiGet<EstagioPage>('/estagios', {
      aluno: 'me',
      situacao: situacao || undefined,
      page,
      size,
    }),
  obter: (id: string) => apiGet<Estagio>(`/estagios/${id}`),
  enviarDocumento: (id: string, tipo: string, arquivo: NativeUploadFile) => {
    const form = new FormData();
    form.append('tipo', tipo);
    form.append('arquivo', {
      uri: arquivo.uri,
      name: arquivo.name,
      type: arquivo.mimeType,
    } as unknown as Blob);
    return apiPostForm<Estagio>(`/estagios/${id}/documentos`, form);
  },
  parecer: (id: string, documentoId: string, acao: 'APROVAR' | 'REPROVAR', parecer: string) =>
    apiPost<Estagio>(`/estagios/${id}/documentos/${documentoId}/parecer`, { acao, parecer }),
  baixar: (href: string) => baixarViaPresign(href),
};
