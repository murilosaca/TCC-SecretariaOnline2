import { View, Text, TextInput, TouchableOpacity, ScrollView, Alert } from 'react-native';
import { useState } from 'react';
import { Feather } from '@expo/vector-icons';
import { useRouter } from 'expo-router';

export default function FirstAccessScreen() {
  const router = useRouter();
  const [password, setPassword] = useState('');
  const [confirmPassword, setConfirmPassword] = useState('');
  const [showPassword, setShowPassword] = useState(false);
  const [showConfirmPassword, setShowConfirmPassword] = useState(false);
  
  const [acceptTerms, setAcceptTerms] = useState(false);
  const [receiveEmail, setReceiveEmail] = useState(false);

  const handleDefine = () => {
    if (!password || password !== confirmPassword) {
      Alert.alert('erro', 'as senhas não coincidem ou estão vazias.');
      return;
    }
    if (!acceptTerms) {
      Alert.alert('erro', 'você precisa aceitar a política de privacidade para continuar.');
      return;
    }

    console.log('payload primeiro acesso:', { password, receiveEmail });
    Alert.alert('sucesso', 'senha definida com sucesso (esqueleto)');
    router.replace('/inicio-aluno' as any);
  };

  return (
    <ScrollView className="flex-1 bg-white" contentContainerStyle={{ padding: 24, paddingTop: 48, flexGrow: 1 }}>
      {/* top navigation bar */}
      <View className="flex-row justify-between items-center mb-8">
        <TouchableOpacity onPress={() => router.push('/menu' as any)}>
          <Feather name="menu" size={24} color="#111827" />
        </TouchableOpacity>
        
        <TouchableOpacity 
          className="relative" 
          onPress={() => router.push('/notificacoes' as any)}
        >
          <Feather name="bell" size={24} color="#111827" />
          {/* notification dot */}
          <View className="absolute top-0 right-0 w-2.5 h-2.5 bg-red-500 rounded-full border-2 border-so2-background" />
        </TouchableOpacity>
      </View>

      {/* header texts */}
      <View className="mb-8">
        <Text className="text-3xl font-bold text-so2-text mb-4">
          Primeiro acesso
        </Text>
        <Text className="text-so2-text text-sm leading-relaxed">
          Defina sua nova senha e aceite a política de privacidade para continuar.
        </Text>
      </View>

      {/* form */}
      <View className="mb-4">
        <Text className="text-so2-text text-sm mb-2">Senha *</Text>
        <View className="flex-row items-center border border-gray-200 rounded-lg px-4 bg-white mb-2">
          <TextInput
            className="flex-1 py-3 text-so2-text"
            placeholder="••••••••"
            value={password}
            onChangeText={setPassword}
            secureTextEntry={!showPassword}
          />
          <TouchableOpacity onPress={() => setShowPassword(!showPassword)}>
            <Feather name={showPassword ? "eye-off" : "eye"} size={20} color="#9CA3AF" />
          </TouchableOpacity>
        </View>
      </View>

      <View className="mb-6">
        <Text className="text-so2-text text-sm mb-2">Confirmar senha *</Text>
        <View className="flex-row items-center border border-gray-200 rounded-lg px-4 bg-white mb-2">
          <TextInput
            className="flex-1 py-3 text-so2-text"
            placeholder="••••••••"
            value={confirmPassword}
            onChangeText={setConfirmPassword}
            secureTextEntry={!showConfirmPassword}
          />
          <TouchableOpacity onPress={() => setShowConfirmPassword(!showConfirmPassword)}>
            <Feather name={showConfirmPassword ? "eye-off" : "eye"} size={20} color="#9CA3AF" />
          </TouchableOpacity>
        </View>
      </View>

      {/* checkboxes */}
      <View className="mb-8 gap-y-4">
        <TouchableOpacity 
          className="flex-row items-center gap-x-3" 
          onPress={() => setAcceptTerms(!acceptTerms)}
        >
          <Feather 
            name={acceptTerms ? "check-square" : "square"} 
            size={20} 
            color={acceptTerms ? "#3F00FF" : "#9CA3AF"} 
          />
          <Text className="text-so2-text text-sm flex-1">Li e aceito a política de privacidade</Text>
        </TouchableOpacity>

        <TouchableOpacity 
          className="flex-row items-center gap-x-3" 
          onPress={() => setReceiveEmail(!receiveEmail)}
        >
          <Feather 
            name={receiveEmail ? "check-square" : "square"} 
            size={20} 
            color={receiveEmail ? "#3F00FF" : "#9CA3AF"} 
          />
          <Text className="text-so2-text text-sm flex-1">Receber notificações por email</Text>
        </TouchableOpacity>
      </View>

      <TouchableOpacity
        className="bg-so2-primary py-3 rounded-lg items-center"
        onPress={handleDefine}
      >
        <Text className="text-white font-medium text-lg">Definir</Text>
      </TouchableOpacity>
    </ScrollView>
  );
}