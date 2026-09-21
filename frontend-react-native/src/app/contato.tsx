import { useRouter } from 'expo-router';
import { useQuery } from '@tanstack/react-query';
import { ScrollView, Text, TouchableOpacity, View } from 'react-native';
import { publicoApi } from '@/api/publico';
import { Banner } from '@/components/Banner';
import { LoadingState } from '@/components/ScreenState';

export default function ContatoScreen() {
  const router = useRouter();
  const contato = useQuery({ queryKey: ['contato'], queryFn: () => publicoApi.contato() });

  return (
    <ScrollView className="flex-1 bg-so2-background" contentContainerStyle={{ flexGrow: 1, justifyContent: 'center' }}>
      <View className="px-6 py-12">
        <View className="rounded-2xl border border-so2-border bg-so2-card p-6">
          <Text className="mb-2 text-xl font-bold text-so2-text">Contato da secretaria</Text>
          <Text className="mb-4 text-sm text-so2-textMuted">Informações públicas (RF-F0-004).</Text>
          {contato.isLoading ? <LoadingState /> : null}
          {contato.isError ? (
            <Banner
              message="Não foi possível carregar o contato da secretaria."
              actionLabel="Tentar de novo"
              onAction={() => void contato.refetch()}
            />
          ) : null}
          {contato.data ? (
            <View className="mb-6 rounded-lg border border-so2-border p-4">
              <Text className="mb-2 font-bold text-so2-text">{contato.data.nome}</Text>
              <Text className="text-sm text-so2-text">{contato.data.endereco}</Text>
              <Text className="text-sm text-so2-text">Telefone: {contato.data.telefone}</Text>
              <Text className="text-sm text-so2-text">{contato.data.email}</Text>
              <Text className="text-sm text-so2-text">Horário: {contato.data.horario}</Text>
            </View>
          ) : null}
          <TouchableOpacity className="items-center py-2" onPress={() => router.back()}>
            <Text className="font-medium text-so2-primary">Voltar</Text>
          </TouchableOpacity>
        </View>
      </View>
    </ScrollView>
  );
}
