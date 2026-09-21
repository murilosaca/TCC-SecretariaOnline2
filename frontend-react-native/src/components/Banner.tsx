import { Text, TouchableOpacity, View } from 'react-native';

type Tone = 'danger' | 'warning' | 'success' | 'info';

type Props = {
  message: string;
  tone?: Tone;
  actionLabel?: string;
  onAction?: () => void;
};

const TONE: Record<Tone, string> = {
  danger: 'bg-so2-danger border-red-200',
  warning: 'bg-so2-pending border-yellow-200',
  success: 'bg-so2-success border-green-200',
  info: 'bg-blue-50 border-blue-200',
};

export function Banner({ message, tone = 'danger', actionLabel, onAction }: Props) {
  return (
    <View className={`mb-4 rounded-lg border p-3 ${TONE[tone]}`} accessibilityRole="alert">
      <Text className="text-sm text-so2-text">{message}</Text>
      {actionLabel && onAction ? (
        <TouchableOpacity onPress={onAction} className="mt-2">
          <Text className="font-semibold text-so2-primary">{actionLabel}</Text>
        </TouchableOpacity>
      ) : null}
    </View>
  );
}
