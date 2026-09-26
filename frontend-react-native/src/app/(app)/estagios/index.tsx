import { Link } from 'expo-router';
import { useQuery } from '@tanstack/react-query';
import { useState } from 'react';
import { ScrollView, Text, TouchableOpacity, View } from 'react-native';
import { ApiError } from '@/api/client';
import { estagiosApi } from '@/api/estagios';
import { AppHeader } from '@/components/AppHeader';
import { Banner } from '@/components/Banner';
import { EmptyState, LoadingState } from '@/components/ScreenState';
import { useActions } from '@/hooks/useActions';
import { rotuloSituacaoEstagio, vigencia } from '@/lib/estagio';
import type { Estagio } from '@/models/estagio';

const FILTROS: { value: string; label: string }[] = [
  { value: '', label: 'Todas' },
  { value: 'ATIVO', label: 'Ativo' },
  { value: 'CONCLUIDO', label: 'Concluído' },
  { value: 'PENDENTE', label: 'Pendente' },
];

export default function EstagiosScreen() {
  const [situacao, setSituacao] = useState('');
  const lista = useQuery({
    queryKey: ['estagios', 'me', situacao],
    queryFn: () => estagiosApi.listarMeus(situacao),
  });
  const forbidden = lista.isError && lista.error instanceof ApiError && lista.error.status === 403;

  return (
    <View className="flex-1 bg-so2-background">
      <AppHeader title="Estágios" />
      <ScrollView contentContainerStyle={{ padding: 24, paddingBottom: 48 }}>
        <Text className="mb-4 text-sm text-so2-textMuted">
          Estágios registrados pela secretaria. Envie os documentos quando a ação estiver disponível.
        </Text>

        <ScrollView horizontal showsHorizontalScrollIndicator={false} className="mb-4">
          <View className="flex-row gap-2">
            {FILTROS.map((filtro) => (
              <TouchableOpacity
                key={filtro.value || 'todas'}
                className={`rounded-md border px-3 py-2 ${
                  situacao === filtro.value ? 'border-so2-primary bg-so2-primary' : 'border-so2-border bg-so2-card'
                }`}
                onPress={() => setSituacao(filtro.value)}
                accessibilityRole="button"
                accessibilityState={{ selected: situacao === filtro.value }}
                accessibilityLabel={`Filtrar situação ${filtro.label}`}
              >
                <Text className={situacao === filtro.value ? 'text-white' : 'text-so2-text'}>{filtro.label}</Text>
              </TouchableOpacity>
            ))}
          </View>
        </ScrollView>

        {forbidden ? <EmptyState label="Estágios indisponíveis para esta sessão." /> : null}
        {lista.isError && !forbidden ? (
          <Banner
            message="Não foi possível carregar os estágios."
            actionLabel="Tentar de novo"
            onAction={() => void lista.refetch()}
          />
        ) : null}
        {lista.isLoading ? <LoadingState label="Carregando estágios…" /> : null}
        {lista.data && lista.data.content.length === 0 ? (
          <EmptyState
            label={situacao ? 'Nenhum estágio com essa situação.' : 'Você não possui estágios registrados.'}
          />
        ) : null}
        {lista.data?.content.map((item) => (
          <Linha key={item.id} item={item} />
        ))}
      </ScrollView>
    </View>
  );
}

function Linha({ item }: { item: Estagio }) {
  const actions = useActions(item._links);
  const card = (
    <View className="mb-3 rounded-xl border border-so2-border bg-so2-card p-4">
      <Text className="font-bold text-so2-text">{item.empresa}</Text>
      <Text className="mt-1 text-sm text-so2-textMuted">
        {item.supervisor} · {vigencia(item.inicio, item.fim)}
      </Text>
      <Text className="mt-2 text-sm font-medium text-so2-primary">{rotuloSituacaoEstagio(item.situacao)}</Text>
    </View>
  );
  if (!actions.can('self')) {
    return card;
  }
  return (
    <Link href={`/estagios/${item.id}`} asChild>
      <TouchableOpacity accessibilityRole="button" accessibilityLabel={`Abrir estágio ${item.empresa}`}>
        {card}
      </TouchableOpacity>
    </Link>
  );
}
