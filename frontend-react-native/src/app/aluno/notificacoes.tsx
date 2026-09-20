import { View, Text, TouchableOpacity, ScrollView } from 'react-native';
import { useRouter } from 'expo-router';
import { Feather } from '@expo/vector-icons';

export default function NotificationsScreen() {
  const router = useRouter();

  return (
    <ScrollView className="flex-1 bg-so2-background" contentContainerStyle={{ padding: 24, paddingTop: 48 }}>
      <View className="flex-row items-center gap-x-4 mb-10">
        <TouchableOpacity onPress={() => router.back()}>
          <Feather name="arrow-left" size={28} color="#111827" />
        </TouchableOpacity>
        <Text className="text-2xl font-bold text-so2-text">notificações</Text>
      </View>

      <View className="flex-1 items-center justify-center mt-20">
        <Feather name="bell-off" size={48} color="#9CA3AF" />
        <Text className="text-lg font-medium text-so2-text mt-4">nenhuma notificação</Text>
        <Text className="text-so2-textMuted text-center mt-2">
          você será avisado quando houver atualizações.
        </Text>
      </View>
    </ScrollView>
  );
}