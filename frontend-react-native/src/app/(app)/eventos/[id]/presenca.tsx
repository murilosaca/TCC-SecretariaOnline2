import { useState } from 'react';
import { useLocalSearchParams } from 'expo-router';
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { ScrollView, Text, TextInput, TouchableOpacity, View } from 'react-native';
import { ApiError } from '@/api/client';
import { eventosApi } from '@/api/eventos';
import { obterDeviceUuid } from '@/auth/tokenStore';
import { AppHeader } from '@/components/AppHeader';
import { Banner } from '@/components/Banner';
import { QrScanner } from '@/components/QrScanner';
import { LoadingState } from '@/components/ScreenState';
import { useActions } from '@/hooks/useActions';
import { isQrMode } from '@/models/evento';
import type { SessaoPresenca } from '@/models/evento';

export default function PresencaEventoScreen() {
  const { id = '' } = useLocalSearchParams<{ id: string }>();
  const queryClient = useQueryClient();
  const [erroConfirmacao, setErroConfirmacao] = useState<string | null>(null);

  const sessao = useQuery({
    queryKey: ['eventos', id, 'sessao'],
    queryFn: () => eventosApi.sessao(id),
    enabled: Boolean(id),
  });

  const confirmar = useMutation({
    mutationFn: async ({ segredo, fase }: { segredo: string; fase: string }) => {
      const qr = isQrMode(sessao.data?.attendanceMode);
      const deviceUuid = await obterDeviceUuid();
      return eventosApi.confirmar(id, {
        ...(qr ? { token: segredo } : { pin: segredo }),
        deviceUuid,
        fase,
      });
    },
    onSuccess: (atualizada) => {
      setErroConfirmacao(null);
      queryClient.setQueryData(['eventos', id, 'sessao'], atualizada);
      void queryClient.invalidateQueries({ queryKey: ['eventos', 'me'] });
    },
    onError: (erro) => {
      setErroConfirmacao(erro instanceof ApiError ? erro.message : 'Não foi possível confirmar a presença.');
    },
  });

  return (
    <View className="flex-1 bg-so2-background">
      <AppHeader title="Presença" showMenu={false} />
      <ScrollView contentContainerStyle={{ padding: 24 }}>
        {sessao.isError ? (
          <Banner
            message="Não foi possível carregar a sessão de presença."
            actionLabel="Tentar de novo"
            onAction={() => void sessao.refetch()}
          />
        ) : null}
        {sessao.isLoading ? <LoadingState label="Carregando sessão…" /> : null}
        {sessao.data ? (
          <ConteudoSessao
            sessao={sessao.data}
            erro={erroConfirmacao}
            pending={confirmar.isPending}
            onConfirm={(segredo, fase) => confirmar.mutate({ segredo, fase })}
          />
        ) : null}
      </ScrollView>
    </View>
  );
}

function ConteudoSessao({
  sessao,
  erro,
  pending,
  onConfirm,
}: {
  sessao: SessaoPresenca;
  erro: string | null;
  pending: boolean;
  onConfirm: (segredo: string, fase: string) => void;
}) {
  const actions = useActions(sessao._links);
  const podeConfirmar = actions.can('confirmar-entrada') || actions.can('confirmar-saida');
  const qr = isQrMode(sessao.attendanceMode);
  const fase = actions.can('confirmar-saida') && !actions.can('confirmar-entrada')
    ? 'SAIDA'
    : (sessao.faseDisponivel ?? 'ENTRADA');

  if (sessao.situacaoPresenca === 'COMPLETA') {
    return <Banner tone="success" message="Presença confirmada." />;
  }

  if (podeConfirmar && sessao.janelaAtiva) {
    return (
      <View className="rounded-xl border border-so2-border bg-so2-card p-4">
        <Text className="mb-1 text-xl font-bold text-so2-text">{sessao.titulo}</Text>
        <Text className="mb-4 text-sm text-so2-textMuted">
          {qr
            ? 'Aponte a câmera para o QR ou cole o token. Sem geofence.'
            : 'Informe o PIN divulgado no evento.'}
        </Text>
        {erro ? <Banner message={erro} /> : null}
        {qr ? (
          <QrScanner disabled={pending} onScan={(token) => onConfirm(token, fase)} />
        ) : (
          <PinForm pending={pending} onConfirm={(pin) => onConfirm(pin, fase)} />
        )}
      </View>
    );
  }

  if (sessao.situacaoPresenca === 'PARCIAL') {
    return <Banner tone="success" message="Entrada registrada. Confirme a saída quando solicitado." />;
  }

  return <Banner tone="info" message="A janela de validação encerrou." />;
}

function PinForm({ pending, onConfirm }: { pending: boolean; onConfirm: (pin: string) => void }) {
  const [pin, setPin] = useState('');
  return (
    <View>
      <Text className="mb-1 text-sm text-so2-textMuted">PIN de presença</Text>
      <TextInput
        className="mb-3 rounded-md border border-so2-border px-4 py-3 text-so2-text"
        value={pin}
        onChangeText={setPin}
        keyboardType="number-pad"
        editable={!pending}
        autoComplete="off"
        accessibilityLabel="PIN de presença"
      />
      <TouchableOpacity
        className="items-center rounded-md bg-so2-primary py-3"
        disabled={pending || !pin.trim()}
        onPress={() => onConfirm(pin.trim())}
      >
        <Text className="font-bold text-white">{pending ? 'Confirmando…' : 'Confirmar'}</Text>
      </TouchableOpacity>
    </View>
  );
}
