import { useLocalSearchParams } from 'expo-router';
import { useQuery } from '@tanstack/react-query';
import { ScrollView, Text, View } from 'react-native';
import { solicitacoesApi } from '@/api/solicitacoes';
import { AppHeader } from '@/components/AppHeader';
import { Banner } from '@/components/Banner';
import { LoadingState } from '@/components/ScreenState';
import { fieldsFromSchema } from '@/lib/formSchema';

export default function SolicitacaoDetalheScreen() {
  const { id } = useLocalSearchParams<{ id: string }>();
  const detalhe = useQuery({
    queryKey: ['solicitacao', id],
    queryFn: () => solicitacoesApi.obter(id as string),
    enabled: Boolean(id),
  });
  const item = detalhe.data;
  const campos = fieldsFromSchema(item?.formSchema);

  return (
    <View className="flex-1 bg-so2-background">
      <AppHeader title="Solicitação" showMenu={false} />
      <ScrollView contentContainerStyle={{ padding: 24 }}>
        {detalhe.isLoading ? <LoadingState label="Carregando solicitação…" /> : null}
        {detalhe.isError ? (
          <Banner
            message="Solicitação não encontrada ou indisponível."
            actionLabel="Tentar de novo"
            onAction={() => void detalhe.refetch()}
          />
        ) : null}
        {item ? (
          <View className="rounded-xl border border-so2-border bg-so2-card p-4">
            <Text className="text-2xl font-bold text-so2-text">{item.protocolo}</Text>
            <Text className="mb-3 text-so2-textMuted">
              {item.tipoNome} · {item.estado}
            </Text>
            {(campos.length > 0
              ? campos
              : Object.keys(item.payload).map((name) => ({ name, title: name }))
            ).map((campo) => (
              <View key={campo.name} className="mb-2">
                <Text className="text-xs text-so2-textMuted">{campo.title}</Text>
                <Text className="text-so2-text">
                  {item.payload[campo.name] == null || item.payload[campo.name] === ''
                    ? '—'
                    : String(item.payload[campo.name])}
                </Text>
              </View>
            ))}
          </View>
        ) : null}
      </ScrollView>
    </View>
  );
}
