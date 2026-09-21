import { Link } from 'expo-router';
import { useQuery } from '@tanstack/react-query';
import { ScrollView, Text, TouchableOpacity, View } from 'react-native';
import { solicitacoesApi } from '@/api/solicitacoes';
import { AppHeader } from '@/components/AppHeader';
import { Banner } from '@/components/Banner';
import { EmptyState, LoadingState } from '@/components/ScreenState';
import { useActions } from '@/hooks/useActions';

export default function SolicitacoesScreen() {
  const lista = useQuery({
    queryKey: ['solicitacoes', 'me'],
    queryFn: () => solicitacoesApi.listarMinhas(),
  });
  const actions = useActions(lista.data?._links);

  return (
    <View className="flex-1 bg-so2-background">
      <AppHeader title="Solicitações" />
      <ScrollView contentContainerStyle={{ padding: 24 }}>
        {actions.can('novaSolicitacao') ? (
          <Link href="/solicitacoes/nova" asChild>
            <TouchableOpacity className="mb-4 items-center rounded-md bg-so2-primary py-3">
              <Text className="font-bold text-white">Nova solicitação</Text>
            </TouchableOpacity>
          </Link>
        ) : null}
        {lista.isError ? (
          <Banner
            message="Não foi possível carregar as solicitações."
            actionLabel="Tentar de novo"
            onAction={() => void lista.refetch()}
          />
        ) : null}
        {lista.isLoading ? <LoadingState label="Carregando solicitações…" /> : null}
        {lista.data && lista.data.content.length === 0 ? (
          <EmptyState label="Você ainda não abriu solicitações." />
        ) : null}
        {lista.data?.content.map((item) => (
          <Link key={item.id} href={`/solicitacoes/${item.id}`} asChild>
            <TouchableOpacity className="mb-3 rounded-xl border border-so2-border bg-so2-card p-4">
              <Text className="font-bold text-so2-text">{item.protocolo}</Text>
              <Text className="text-sm text-so2-textMuted">
                {item.tipoNome} · {item.estado}
              </Text>
            </TouchableOpacity>
          </Link>
        ))}
      </ScrollView>
    </View>
  );
}
