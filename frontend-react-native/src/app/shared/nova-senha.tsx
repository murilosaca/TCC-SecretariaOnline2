import { View, Text, TextInput, TouchableOpacity, ScrollView, Alert } from 'react-native';
import { useState } from 'react';
import { useRouter } from 'expo-router';
import { apiClient } from '../../api/client';

export default function NewPasswordScreen() {
  const router = useRouter();
  const [newPassword, setNewPassword] = useState('');
  const [confirmPassword, setConfirmPassword] = useState('');

  const handleResetPassword = async () => {
    if (newPassword !== confirmPassword) {
      Alert.alert('erro', 'as senhas não coincidem.');
      return;
    }

    try {
      // payload structure for setting new password
      const payload = {
        senha: newPassword,
      };

      console.log('sending new password request:', payload);
      // const response = await apiClient.post('/auth/reset-password', payload);
      
      Alert.alert('sucesso', 'senha alterada com sucesso');
      router.replace('/');
    } catch (error) {
      console.error('password reset request failed:', error);
      Alert.alert('erro', 'falha na requisição.');
    }
  };

  return (
    <ScrollView className="flex-1 bg-so2-background" contentContainerStyle={{flexGrow: 1, justifyContent: 'center'}}>
      <View className="px-6 py-12">
        
        {/* header logos stacked for mobile */}
        <View className="items-center mb-10">
          <View className="flex-row items-center gap-x-4 mb-6">
            <View className="w-20 h-10 bg-gray-200 rounded justify-center items-center">
              <Text className="text-xs text-gray-500">logo ufpr</Text>
            </View>
            <View className="w-20 h-10 bg-gray-200 rounded justify-center items-center">
              <Text className="text-xs text-gray-500">logo sept</Text>
            </View>
          </View>
        </View>

        {/* new password card */}
        <View className="bg-so2-card p-8 rounded-2xl shadow-lg border border-so2-border">
          
          <View className="items-center mb-4">
            <View className="w-12 h-12 bg-so2-primary rounded-xl justify-center items-center mb-4">
              <Text className="text-white text-2xl font-extrabold">S2</Text>
            </View>
            <Text className="text-xl font-bold text-so2-text mb-1">
              SecretariaOnline
            </Text>
            <Text className="text-sm text-so2-textMuted mb-6">
              Nova Senha
            </Text>
          </View>

          <View className="mb-4">
            <Text className="text-so2-textMuted mb-2 font-medium">Nova senha</Text>
            <TextInput
              className="border border-so2-border rounded-md px-4 py-3 text-so2-text"
              placeholder="••••••••"
              value={newPassword}
              onChangeText={setNewPassword}
              secureTextEntry
              placeholderTextColor="#9CA3AF"
            />
          </View>

          <View className="mb-8">
            <Text className="text-so2-textMuted mb-2 font-medium">Confirmar senha</Text>
            <TextInput
              className="border border-so2-border rounded-md px-4 py-3 text-so2-text"
              placeholder="••••••••"
              value={confirmPassword}
              onChangeText={setConfirmPassword}
              secureTextEntry
              placeholderTextColor="#9CA3AF"
            />
          </View>

          <TouchableOpacity
            className="bg-so2-primary py-3 rounded-md items-center mb-6 shadow"
            onPress={handleResetPassword}
          >
            <Text className="text-white text-lg font-bold">Definir Nova Senha</Text>
          </TouchableOpacity>

          <View className="items-center">
            <TouchableOpacity onPress={() => router.replace('/')}>
              <Text className="text-so2-primary font-medium">Voltar</Text>
            </TouchableOpacity>
          </View>

        </View>
      </View>
    </ScrollView>
  );
}