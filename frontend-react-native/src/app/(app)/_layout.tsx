import { Redirect, Stack, usePathname } from 'expo-router';
import { ActivityIndicator, View } from 'react-native';
import { useAuth } from '@/auth/AuthContext';

export default function AppGroupLayout() {
  const { status, mustChangePassword } = useAuth();
  const pathname = usePathname();

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
  if (mustChangePassword && pathname !== '/primeiro-acesso') {
    return <Redirect href="/primeiro-acesso" />;
  }
  if (!mustChangePassword && pathname === '/primeiro-acesso') {
    return <Redirect href="/inicio" />;
  }
  return <Stack screenOptions={{ headerShown: false }} />;
}
