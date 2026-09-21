import { Feather } from '@expo/vector-icons';
import { useRouter, type Href } from 'expo-router';
import { ScrollView, Text, TouchableOpacity, View } from 'react-native';
import { useSafeAreaInsets } from 'react-native-safe-area-context';
import { useAuth } from '@/auth/AuthContext';
import { useActions } from '@/hooks/useActions';

const NAV_ITENS: { rel: string; label: string }[] = [
  { rel: 'inicio', label: 'Início' },
  { rel: 'solicitacoes', label: 'Solicitações' },
  { rel: 'eventos', label: 'Eventos' },
  { rel: 'contato', label: 'Contato' },
];

export default function MenuScreen() {
  const router = useRouter();
  const insets = useSafeAreaInsets();
  const { links, logout } = useAuth();
  const actions = useActions(links);

  return (
    <ScrollView className="flex-1 bg-so2-background" contentContainerStyle={{ padding: 24, paddingTop: insets.top + 16 }}>
      <View className="mb-8 flex-row items-center justify-between">
        <Text className="text-2xl font-bold text-so2-text">Menu</Text>
        <TouchableOpacity onPress={() => router.back()} accessibilityLabel="Fechar menu">
          <Feather name="x" size={28} color="#111827" />
        </TouchableOpacity>
      </View>
      <View className="gap-y-5">
        {NAV_ITENS.filter((item) => actions.can(item.rel)).map((item) => (
          <TouchableOpacity
            key={item.rel}
            className="flex-row items-center gap-x-4"
            onPress={() => {
              const href = actions.href(item.rel);
              if (href) {
                router.push(href as Href);
              }
            }}
          >
            <Text className="text-lg font-medium text-so2-text">{item.label}</Text>
          </TouchableOpacity>
        ))}
        <TouchableOpacity
          className="mt-8 flex-row items-center gap-x-4"
          onPress={() => void logout()}
        >
          <Feather name="log-out" size={22} color="#991B1B" />
          <Text className="text-lg font-medium text-so2-dangerText">Sair</Text>
        </TouchableOpacity>
      </View>
    </ScrollView>
  );
}
