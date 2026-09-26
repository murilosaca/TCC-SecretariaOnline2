import { Link } from 'expo-router';
import { useQuery } from '@tanstack/react-query';
import { useState } from 'react';
import { ScrollView, Text, TouchableOpacity, View } from 'react-native';
import { ApiError } from '@/api/client';
import { tccsApi } from '@/api/tccs';
import { AppHeader } from '@/components/AppHeader';
import { Banner } from '@/components/Banner';
import { EmptyState, LoadingState } from '@/components/ScreenState';
import { useActions } from '@/hooks/useActions';
import { dataBr, rotuloEstadoTcc } from '@/lib/tcc';
import type { Tcc } from '@/models/tcc';

const FILTROS: { value: string; label: string }[] = [
  { value: '', label: 'Todas' },
  { value: 'EM_ELABORACAO', label: 'Em elaboração' },
  { value: 'SUBMETIDO', label: 'Submetido' },
  { value: 'CORRECOES_SOLICITADAS', label: 'Correções' },
  { value: 'APROVADO', label: 'Aprovado' },
  { value: 'REPROVADO', label: 'Reprovado' },
];

export default function TccsScreen() {
  const [estado, setEstado] = useState('');
  const lista = useQuery({
    queryKey: ['tccs', 'me', estado],
    queryFn: () => tccsApi.listarMeus(estado),
  });
  const forbidden = lista.isError && lista.error instanceof ApiError && lista.error.status === 403;

  return (
    <View className="flex-1 bg-so2-background">
      <AppHeader title="TCCs" />
      <ScrollView contentContainerStyle={{ padding: 24, paddingBottom: 48 }}>
        <Text className="mb-4 text-sm text-so2-textMuted">
          TCCs registrados pela secretaria. Envie a versão final quando a ação estiver disponível.
        </Text>

        <ScrollView horizontal showsHorizontalScrollIndicator={false} className="mb-4">
          <View className="flex-row gap-2">
            {FILTROS.map((filtro) => (
              <TouchableOpacity
                key={filtro.value || 'todas'}
                className={`rounded-md border px-3 py-2 ${
                  estado === filtro.value ? 'border-so2-primary bg-so2-primary' : 'border-so2-border bg-so2-card'
                }`}
                onPress={() => setEstado(filtro.value)}
                accessibilityRole="button"
                accessibilityState={{ selected: estado === filtro.value }}
                accessibilityLabel={`Filtrar situação ${filtro.label}`}
              >
                <Text className={estado === filtro.value ? 'text-white' : 'text-so2-text'}>{filtro.label}</Text>
              </TouchableOpacity>
            ))}
          </View>
        </ScrollView>

        {forbidden ? <EmptyState label="TCCs indisponíveis para esta sessão." /> : null}
        {lista.isError && !forbidden ? (
          <Banner
            message="Não foi possível carregar os TCCs."
            actionLabel="Tentar de novo"
            onAction={() => void lista.refetch()}
          />
        ) : null}
        {lista.isLoading ? <LoadingState label="Carregando TCCs…" /> : null}
        {lista.data && lista.data.content.length === 0 ? (
          <EmptyState
            label={estado ? 'Nenhum TCC com essa situação.' : 'Nenhum TCC registrado. Consulte a secretaria.'}
          />
        ) : null}
        {lista.data?.content.map((item) => (
          <Linha key={item.id} item={item} />
        ))}
      </ScrollView>
    </View>
  );
}

function Linha({ item }: { item: Tcc }) {
  const actions = useActions(item._links);
  const card = (
    <View className="mb-3 rounded-xl border border-so2-border bg-so2-card p-4">
      <Text className="font-bold text-so2-text">{item.titulo}</Text>
      <Text className="mt-1 text-sm text-so2-textMuted">
        {item.orientadorRotulo || '—'} · Defesa {dataBr(item.dataDefesa)}
      </Text>
      <Text className="mt-2 text-sm font-medium text-so2-primary">{rotuloEstadoTcc(item.estado)}</Text>
    </View>
  );
  if (!actions.can('self')) {
    return card;
  }
  return (
    <Link href={`/tccs/${item.id}`} asChild>
      <TouchableOpacity accessibilityRole="button" accessibilityLabel={`Abrir TCC ${item.titulo}`}>
        {card}
      </TouchableOpacity>
    </Link>
  );
}
