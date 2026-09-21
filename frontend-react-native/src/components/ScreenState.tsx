import { ActivityIndicator, Text, View } from 'react-native';

export function LoadingState({ label = 'Carregando…' }: { label?: string }) {
  return (
    <View className="items-center py-8" accessibilityState={{ busy: true }}>
      <ActivityIndicator color="#3F00FF" />
      <Text className="mt-2 text-so2-textMuted">{label}</Text>
    </View>
  );
}

export function EmptyState({ label }: { label: string }) {
  return (
    <Text className="py-4 text-so2-textMuted" accessibilityRole="text">
      {label}
    </Text>
  );
}
