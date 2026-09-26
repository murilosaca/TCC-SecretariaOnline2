import { Link } from 'expo-router';
import { useQuery } from '@tanstack/react-query';
import { ScrollView, Text, TouchableOpacity, View } from 'react-native';
import { formativasApi } from '@/api/formativas';
import { AppHeader } from '@/components/AppHeader';
import { Banner } from '@/components/Banner';
import { EmptyState, LoadingState } from '@/components/ScreenState';
import { useActions } from '@/hooks/useActions';
import { rotuloEstadoFormativa, rotuloOrigemFormativa } from '@/lib/formativa';
import type { Formativa } from '@/models/formativa';

export default function FormativasScreen() {
  const lista = useQuery({
    queryKey: ['formativas', 'me'],
    queryFn: () => formativasApi.listarMinhas(),
  });

  return (
    <View className="flex-1 bg-so2-background">
      <AppHeader title="Formativas" />
      <ScrollView contentContainerStyle={{ padding: 24, paddingBottom: 48 }}>
        <Text className="mb-4 text-sm text-so2-textMuted">
          Confirme horas geradas a partir de presença validada.
        </Text>
        {lista.isError ? (
          <Banner
            message="Não foi possível carregar as formativas."
            actionLabel="Tentar de novo"
            onAction={() => void lista.refetch()}
          />
        ) : null}
        {lista.isLoading ? <LoadingState label="Carregando formativas…" /> : null}
        {lista.data && lista.data.content.length === 0 ? (
          <EmptyState label="Nenhuma atividade formativa no momento. Confirme presença em um evento para gerar uma pendência." />
        ) : null}
        {lista.data?.content.map((item) => (
          <Linha key={item.id} item={item} />
        ))}
      </ScrollView>
    </View>
  );
}

function Linha({ item }: { item: Formativa }) {
  const actions = useActions(item._links);
  const data = new Date(item.createdAt).toLocaleDateString('pt-BR');
  const card = (
    <View className="mb-3 rounded-xl border border-so2-border bg-so2-card p-4">
      <Text className="font-bold text-so2-text">{item.titulo}</Text>
      <Text className="mt-1 text-sm text-so2-textMuted">
        {item.cargaHoraria}h · {rotuloOrigemFormativa(item.origem)} · {data}
      </Text>
      <Text className="mt-2 text-sm font-medium text-so2-primary">{rotuloEstadoFormativa(item.estado)}</Text>
    </View>
  );
  if (!actions.can('self')) {
    return card;
  }
  return (
    <Link href={`/formativas/${item.id}`} asChild>
      <TouchableOpacity accessibilityRole="button" accessibilityLabel={`Abrir ${item.titulo}`}>
        {card}
      </TouchableOpacity>
    </Link>
  );
}
