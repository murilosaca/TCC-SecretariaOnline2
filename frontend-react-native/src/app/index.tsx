import { Redirect } from 'expo-router';
import { ActivityIndicator, View } from 'react-native';
import { useAuth } from '@/auth/AuthContext';

export default function Index() {
  const { status, mustChangePassword } = useAuth();
  if (status === 'loading') {
    return (
      <View className="flex-1 items-center justify-center bg-so2-background">
        <ActivityIndicator color="#3F00FF" />
      </View>
    );
  }
  if (status === 'anonymous') {
    return <Redirect href="/login" />;
  }
  return <Redirect href={mustChangePassword ? '/primeiro-acesso' : '/inicio'} />;
}
