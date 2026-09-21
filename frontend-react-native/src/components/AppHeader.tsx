import { Feather } from '@expo/vector-icons';
import { useRouter, type Href } from 'expo-router';
import { Text, TouchableOpacity, View } from 'react-native';
import { useSafeAreaInsets } from 'react-native-safe-area-context';

type Props = {
  title: string;
  showMenu?: boolean;
};

export function AppHeader({ title, showMenu = true }: Props) {
  const router = useRouter();
  const insets = useSafeAreaInsets();
  return (
    <View
      className="flex-row items-center justify-between border-b border-so2-border bg-so2-card px-4 pb-3"
      style={{ paddingTop: insets.top + 8 }}
    >
      {showMenu ? (
        <TouchableOpacity
          accessibilityRole="button"
          accessibilityLabel="Abrir menu"
          onPress={() => router.push('/menu' as Href)}
          className="p-2"
        >
          <Feather name="menu" size={22} color="#111827" />
        </TouchableOpacity>
      ) : (
        <TouchableOpacity
          accessibilityRole="button"
          accessibilityLabel="Voltar"
          onPress={() => router.back()}
          className="p-2"
        >
          <Feather name="arrow-left" size={22} color="#111827" />
        </TouchableOpacity>
      )}
      <Text className="flex-1 text-center text-lg font-semibold text-so2-text">{title}</Text>
      <View className="w-10" />
    </View>
  );
}
