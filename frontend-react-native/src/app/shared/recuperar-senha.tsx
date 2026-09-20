import { View, Text, TextInput, TouchableOpacity, ScrollView, Alert } from 'react-native';
import { useState } from 'react';
import { useRouter } from 'expo-router';
import { apiClient } from '../../api/client';

export default function RecoverPasswordScreen() {
  const router = useRouter();
  const [identifier, setIdentifier] = useState('');

  const handleRecover = async () => {
    if (!identifier) {
      Alert.alert('erro', 'por favor, informe o email ou grr.');
      return;
    }

    try {
      // payload structure for password recovery
      const payload = {
        login: identifier,
      };

      console.log('sending recovery request:', payload);
      // const response = await apiClient.post('/auth/recover-password', payload);
      
      Alert.alert('sucesso', 'link de recuperação solicitado (esqueleto)');
    } catch (error) {
      console.error('recovery request failed:', error);
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

        {/* recovery card */}
        <View className="bg-so2-card p-8 rounded-2xl shadow-lg border border-so2-border">
          
          <View className="items-center mb-4">
            <View className="w-12 h-12 bg-so2-primary rounded-xl justify-center items-center mb-4">
              <Text className="text-white text-2xl font-extrabold">S2</Text>
            </View>
            <Text className="text-xl font-bold text-so2-text mb-1">
              SecretariaOnline
            </Text>
            <Text className="text-sm text-so2-textMuted mb-6">
              Recuperar Senha
            </Text>
          </View>

          <Text className="text-so2-text text-sm mb-6 text-center">
            Informe o email cadastrado. Enviaremos um link válido por 24 horas.
          </Text>

          <View className="mb-6">
            <TextInput
              className="border border-so2-border rounded-md px-4 py-3 text-so2-text"
              placeholder="GRR / email"
              value={identifier}
              onChangeText={setIdentifier}
              autoCapitalize="none"
              placeholderTextColor="#9CA3AF"
            />
          </View>

          <TouchableOpacity
            className="bg-so2-primary py-3 rounded-md items-center mb-6 shadow"
            onPress={handleRecover}
          >
            <Text className="text-white text-lg font-bold">Enviar</Text>
          </TouchableOpacity>

          <View className="items-center">
            <TouchableOpacity onPress={() => router.back()}>
              <Text className="text-so2-primary font-medium">Voltar</Text>
            </TouchableOpacity>
          </View>

        </View>
      </View>
    </ScrollView>
  );
}