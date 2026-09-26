import { useLocalSearchParams } from 'expo-router';
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { ScrollView, Text, TouchableOpacity, View } from 'react-native';
import { ApiError } from '@/api/client';
import { formativasApi } from '@/api/formativas';
import { AppHeader } from '@/components/AppHeader';
import { Banner } from '@/components/Banner';
import { EmptyState, LoadingState } from '@/components/ScreenState';
import { useActions } from '@/hooks/useActions';
import { rotuloEstadoFormativa, rotuloOrigemFormativa } from '@/lib/formativa';
import type { HateoasLinks } from '@/models/hateoas';

export default function FormativaDetalheScreen() {
  const { id = '' } = useLocalSearchParams<{ id: string }>();
  const queryClient = useQueryClient();

  const detalhe = useQuery({
    queryKey: ['formativa', id],
    queryFn: () => formativasApi.obter(id),
    enabled: Boolean(id),
  });

  const confirmar = useMutation({
    mutationFn: () => formativasApi.confirmar(id),
    onSuccess: (atual) => {
      queryClient.setQueryData(['formativa', id], atual);
      void queryClient.invalidateQueries({ queryKey: ['formativas', 'me'] });
      void queryClient.invalidateQueries({ queryKey: ['bff', 'dashboard', 'aluno'] });
    },
  });

  const cancelar = useMutation({
    mutationFn: () => formativasApi.cancelar(id),
    onSuccess: (atual) => {
      queryClient.setQueryData(['formativa', id], atual);
      void queryClient.invalidateQueries({ queryKey: ['formativas', 'me'] });
      void queryClient.invalidateQueries({ queryKey: ['bff', 'dashboard', 'aluno'] });
    },
  });

  const item = detalhe.data;
  const pending = confirmar.isPending || cancelar.isPending;
  const erroAcao = confirmar.error ?? cancelar.error;

  return (
    <View className="flex-1 bg-so2-background">
      <AppHeader title="Formativa" showMenu={false} />
      <ScrollView contentContainerStyle={{ padding: 24, paddingBottom: 48 }}>
        {detalhe.isLoading ? <LoadingState label="Carregando formativa…" /> : null}
        {detalhe.isError ? (
          <Banner
            message="Formativa não encontrada ou indisponível."
            actionLabel="Tentar de novo"
            onAction={() => void detalhe.refetch()}
          />
        ) : null}

        {item ? (
          <>
            <Text className="text-2xl font-bold text-so2-text">{item.titulo}</Text>
            <Text className="mt-1 text-sm text-so2-textMuted">
              {rotuloOrigemFormativa(item.origem)} · {item.cargaHoraria}h
            </Text>
            <Text className="mt-2 font-medium text-so2-primary">{rotuloEstadoFormativa(item.estado)}</Text>

            {erroAcao ? (
              <Banner
                message={erroAcao instanceof ApiError ? erroAcao.message : 'Não foi possível concluir a ação.'}
              />
            ) : null}

            <ConfirmacaoWidget
              links={item._links}
              pending={pending}
              onConfirm={() => confirmar.mutate()}
              onCancel={() => cancelar.mutate()}
            />

            <View className="mt-4 rounded-xl border border-so2-border bg-so2-card p-4">
              <Text className="mb-2 text-lg font-semibold text-so2-text">Metadados</Text>
              <Text className="text-sm text-so2-textMuted">Estado</Text>
              <Text className="mb-2 text-so2-text">{rotuloEstadoFormativa(item.estado)}</Text>
              <Text className="text-sm text-so2-textMuted">Registrada em</Text>
              <Text className="mb-2 text-so2-text">{new Date(item.createdAt).toLocaleString('pt-BR')}</Text>
              {item.parecer ? (
                <>
                  <Text className="text-sm text-so2-textMuted">Parecer da CAAF</Text>
                  <Text className="text-so2-text">{item.parecer}</Text>
                </>
              ) : null}
            </View>
          </>
        ) : null}
      </ScrollView>
    </View>
  );
}

function ConfirmacaoWidget({
  links,
  pending,
  onConfirm,
  onCancel,
}: {
  links?: HateoasLinks;
  pending: boolean;
  onConfirm: () => void;
  onCancel: () => void;
}) {
  const actions = useActions(links);
  const podeConfirmar = actions.can('confirmar');
  const podeCancelar = actions.can('cancelar');

  if (!podeConfirmar && !podeCancelar) {
    return <EmptyState label="Nenhuma ação disponível nesta formativa." />;
  }

  return (
    <View className="mt-4 rounded-xl border border-so2-border bg-so2-card p-4">
      <Text className="mb-3 text-so2-text">
        A presença neste evento já foi validada pelo sistema. Confirme para enviar à CAAF ou cancele se não
        quiser registrar as horas.
      </Text>
      <View className="gap-y-2">
        {podeConfirmar ? (
          <TouchableOpacity
            className="items-center rounded-md bg-so2-primary py-3"
            onPress={onConfirm}
            disabled={pending}
            accessibilityRole="button"
            accessibilityLabel="Confirmar formativa"
          >
            <Text className="font-bold text-white">{pending ? 'Confirmando…' : 'Confirmar'}</Text>
          </TouchableOpacity>
        ) : null}
        {podeCancelar ? (
          <TouchableOpacity
            className="items-center rounded-md border border-so2-border py-3"
            onPress={onCancel}
            disabled={pending}
            accessibilityRole="button"
            accessibilityLabel="Cancelar formativa"
          >
            <Text className="font-medium text-so2-text">{pending ? 'Cancelando…' : 'Cancelar'}</Text>
          </TouchableOpacity>
        ) : null}
      </View>
    </View>
  );
}
