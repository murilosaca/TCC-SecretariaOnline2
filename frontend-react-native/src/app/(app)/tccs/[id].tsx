import { useLocalSearchParams } from 'expo-router';
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { useState } from 'react';
import { ScrollView, Text, TouchableOpacity, View } from 'react-native';
import { ApiError } from '@/api/client';
import { tccsApi } from '@/api/tccs';
import { escolherPdf, type NativeUploadFile } from '@/api/upload';
import { AppHeader } from '@/components/AppHeader';
import { Banner } from '@/components/Banner';
import { EmptyState, LoadingState } from '@/components/ScreenState';
import { useActions } from '@/hooks/useActions';
import { dataBr, entregaProxima, rotuloEstadoTcc, rotuloPapelBanca, rotuloResultadoTcc } from '@/lib/tcc';

export default function TccDetalheScreen() {
  const { id = '' } = useLocalSearchParams<{ id: string }>();
  const queryClient = useQueryClient();
  const [arquivo, setArquivo] = useState<NativeUploadFile | null>(null);
  const [baixando, setBaixando] = useState(false);
  const [erroDownload, setErroDownload] = useState('');

  const detalhe = useQuery({
    queryKey: ['tcc', id],
    queryFn: () => tccsApi.obter(id),
    enabled: Boolean(id),
  });

  const enviar = useMutation({
    mutationFn: (file: NativeUploadFile) => tccsApi.enviarVersaoFinal(id, file),
    onSuccess: (atual) => {
      queryClient.setQueryData(['tcc', id], atual);
      setArquivo(null);
      void queryClient.invalidateQueries({ queryKey: ['tccs'] });
    },
  });

  const item = detalhe.data;
  const actions = useActions(item?._links);
  const erroAcao = enviar.error;
  const pending = enviar.isPending;
  const prazo = item ? entregaProxima(item.dataEntrega) : false;

  async function escolher() {
    const escolhido = await escolherPdf();
    if (escolhido) {
      setArquivo(escolhido);
    }
  }

  async function baixar() {
    const href = actions.href('download');
    if (!href) {
      return;
    }
    setBaixando(true);
    setErroDownload('');
    try {
      await tccsApi.baixar(href);
    } catch {
      setErroDownload('Não foi possível baixar o PDF.');
    } finally {
      setBaixando(false);
    }
  }

  return (
    <View className="flex-1 bg-so2-background">
      <AppHeader title="TCC" showMenu={false} />
      <ScrollView contentContainerStyle={{ padding: 24, paddingBottom: 48 }}>
        {detalhe.isLoading ? <LoadingState label="Carregando TCC…" /> : null}
        {detalhe.isError ? (
          <Banner
            message="TCC não encontrado ou indisponível."
            actionLabel="Tentar de novo"
            onAction={() => void detalhe.refetch()}
          />
        ) : null}

        {item ? (
          <>
            <Text className="text-2xl font-bold text-so2-text">{item.titulo}</Text>
            <Text className="mt-1 text-sm text-so2-textMuted">{item.alunoNome || item.orientadorRotulo || ''}</Text>
            <Text className="mt-2 font-medium text-so2-primary">{rotuloEstadoTcc(item.estado)}</Text>

            {erroAcao || erroDownload ? (
              <Banner
                message={
                  erroAcao instanceof ApiError
                    ? erroAcao.message
                    : erroDownload || 'Não foi possível concluir a ação.'
                }
              />
            ) : null}

            <View className="mt-4 rounded-xl border border-so2-border bg-so2-card p-4">
              <Text className="mb-2 text-lg font-semibold text-so2-text">Banca</Text>
              {item.membros.map((membro) => (
                <View key={membro.id} className="mb-2 border-b border-so2-border pb-2">
                  <Text className="text-so2-text">{membro.rotulo || '—'}</Text>
                  <Text className="text-sm text-so2-textMuted">{rotuloPapelBanca(membro.papel)}</Text>
                </View>
              ))}
            </View>

            <View className="mt-4 rounded-xl border border-so2-border bg-so2-card p-4">
              <Text className="mb-2 text-lg font-semibold text-so2-text">Datas-chave</Text>
              <Text className="text-so2-text">
                Defesa: <Text className="font-semibold">{dataBr(item.dataDefesa)}</Text>
              </Text>
              <Text className="mt-1 text-so2-text">
                Limite de entrega: <Text className="font-semibold">{dataBr(item.dataEntrega)}</Text>
                {prazo ? ' · Prazo próximo' : ''}
              </Text>
            </View>

            {actions.can('download') ? (
              <TouchableOpacity
                className="mt-4 items-center rounded-md border border-so2-border py-3"
                onPress={() => void baixar()}
                disabled={baixando}
              >
                <Text className="text-so2-primary">{baixando ? 'Baixando…' : 'Baixar TCC'}</Text>
              </TouchableOpacity>
            ) : null}

            {actions.can('upload-final') ? (
              <View className="mt-4 rounded-xl border border-so2-border bg-so2-card p-4">
                <Text className="mb-2 text-lg font-semibold text-so2-text">Versão final</Text>
                <TouchableOpacity
                  className="mb-2 items-center rounded-md border border-so2-border py-2"
                  onPress={() => void escolher()}
                  disabled={pending}
                >
                  <Text className="text-so2-text">{arquivo?.name ?? 'Selecionar PDF'}</Text>
                </TouchableOpacity>
                <TouchableOpacity
                  className="items-center rounded-md bg-so2-primary py-3"
                  disabled={!arquivo || pending}
                  onPress={() => arquivo && enviar.mutate(arquivo)}
                >
                  <Text className="font-bold text-white">
                    {enviar.isPending ? 'Enviando…' : 'Enviar versão final'}
                  </Text>
                </TouchableOpacity>
              </View>
            ) : null}

            <View className="mt-4 rounded-xl border border-so2-border bg-so2-card p-4">
              <Text className="mb-2 text-lg font-semibold text-so2-text">Avaliações</Text>
              {item.avaliacoes.length === 0 ? <EmptyState label="Nenhuma avaliação registrada." /> : null}
              {item.avaliacoes.map((avaliacao) => (
                <View key={avaliacao.id} className="mb-3 border-b border-so2-border pb-3">
                  <Text className="font-medium text-so2-text">
                    {rotuloResultadoTcc(avaliacao.resultado)} · {rotuloPapelBanca(avaliacao.papel)}
                    {avaliacao.autorRotulo ? ` · ${avaliacao.autorRotulo}` : ''} · nota {avaliacao.nota}
                  </Text>
                  <Text className="mt-1 text-so2-text">{avaliacao.parecer}</Text>
                </View>
              ))}
            </View>
          </>
        ) : null}
      </ScrollView>
    </View>
  );
}
