import { Link } from 'expo-router';
import { useQuery } from '@tanstack/react-query';
import { ScrollView, Text, TouchableOpacity, View } from 'react-native';
import { eventosApi } from '@/api/eventos';
import { AppHeader } from '@/components/AppHeader';
import { Banner } from '@/components/Banner';
import { EmptyState, LoadingState } from '@/components/ScreenState';
import { useActions } from '@/hooks/useActions';
import type { Evento } from '@/models/evento';

export default function EventosScreen() {
  const lista = useQuery({
    queryKey: ['eventos', 'me'],
    queryFn: () => eventosApi.listarMeus(),
  });

  return (
    <View className="flex-1 bg-so2-background">
      <AppHeader title="Eventos" />
      <ScrollView contentContainerStyle={{ padding: 24 }}>
        <Text className="mb-4 text-sm text-so2-textMuted">
          Confirme presença apenas quando a janela estiver aberta.
        </Text>
        {lista.isError ? (
          <Banner
            message="Não foi possível carregar os eventos."
            actionLabel="Tentar de novo"
            onAction={() => void lista.refetch()}
          />
        ) : null}
        {lista.isLoading ? <LoadingState label="Carregando eventos…" /> : null}
        {lista.data && lista.data.content.length === 0 ? (
          <EmptyState label="Nenhum evento disponível no momento." />
        ) : null}
        {lista.data?.content.map((item) => (
          <CartaoEvento key={item.id} item={item} />
        ))}
      </ScrollView>
    </View>
  );
}

function CartaoEvento({ item }: { item: Evento }) {
  const actions = useActions(item._links);
  return (
    <View className="mb-3 rounded-xl border border-so2-border bg-so2-card p-4">
      <Text className="font-bold text-so2-text">{item.titulo}</Text>
      <Text className="text-sm text-so2-textMuted">
        {new Date(item.inicioEm).toLocaleString('pt-BR')} · {item.cargaHoraria}h
      </Text>
      <Text className="text-xs text-so2-textMuted">
        {item.estado} · presença {item.situacaoPresenca}
      </Text>
      {actions.can('sessao') ? (
        <Link href={`/eventos/${item.id}/presenca`} asChild>
          <TouchableOpacity className="mt-3">
            <Text className="font-semibold text-so2-primary">Presença</Text>
          </TouchableOpacity>
        </Link>
      ) : null}
    </View>
  );
}
