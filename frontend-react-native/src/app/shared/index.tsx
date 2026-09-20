import { View, Text, TextInput, TouchableOpacity, Alert, Image, ScrollView } from 'react-native';
import { useState } from 'react';
import { apiClient } from '../../api/client';
// TODO: Importe as imagens reais quando as tiver
// import logoUfpr from '../assets/images/logo-ufpr.png';
// import logoSept from '../assets/images/logo-sept.png';
// import logoS2 from '../assets/images/logo-s2.png';

export default function LoginScreen() {
  const [identifier, setIdentifier] = useState('');
  const [password, setPassword] = useState('');
  
  const [errorMessage, setErrorMessage] = useState(''); 

  const handleLogin = async () => {
    setErrorMessage('');

    if (!identifier || !password) {
      setErrorMessage('CPF/GRR/e-mail ou senha inválidos.');
      return;
    }

    try {
      const payload = {
        login: identifier, 
        senha: password
      };

      console.log('enviando login:', payload);
      // const response = await apiClient.post('/auth/login', payload);
      Alert.alert('sucesso', 'esqueleto de login acionado');
    } catch (error) {
      console.error('erro login:', error);
      setErrorMessage('Erro na requisição. Tente novamente.');
    }
  };

  return (
    <ScrollView className="flex-1 bg-so2-background" contentContainerStyle={{flexGrow: 1, justifyContent: 'center'}}>
      <View className="px-6 py-12">
        
        {/* CABEÇALHO DE LOGOS (Empilhados no topo - UI/UX mobile) */}
        <View className="items-center mb-10">
          <View className="flex-row items-center gap-x-4 mb-6">
            {/* Placeholders para os logos reais */}
            <View className="w-20 h-10 bg-gray-200 rounded justify-center items-center">
              <Text className="text-xs text-gray-500">Logo UFPR</Text>
            </View>
            <View className="w-20 h-10 bg-gray-200 rounded justify-center items-center">
              <Text className="text-xs text-gray-500">Logo SEPT</Text>
            </View>
          </View>
          <Text className="text-2xl font-bold text-center text-so2-text mb-1">
            secretaria online 2
          </Text>
          <Text className="text-center text-so2-textMuted">
            Acesso ao portal
          </Text>
        </View>

        <View className="bg-so2-card p-8 rounded-2xl shadow-lg border border-so2-border">
          
          <View className="items-center mb-8">
            <View className="w-16 h-16 bg-so2-primary rounded-xl justify-center items-center">
              <Text className="text-white text-3xl font-extrabold">S2</Text>
            </View>
          </View>

          {errorMessage ? (
            <View className="bg-so2-danger border border-so2-dangerText p-4 rounded-md mb-6 flex-row items-center">
              <Text className="text-so2-dangerText text-center flex-1">
                {errorMessage}
              </Text>
            </View>
          ) : null}

          <View className="mb-5">
            <Text className="text-so2-textMuted mb-2 font-medium">CPF / email</Text>
            <TextInput
              className="border border-so2-border rounded-md px-4 py-3 text-so2-text"
              placeholder="ex: aluno.dev@ufpr.br ou GRR20240001"
              value={identifier}
              onChangeText={setIdentifier}
              autoCapitalize="none"
              placeholderTextColor="#9CA3AF"
            />
          </View>

          <View className="mb-8">
            <Text className="text-so2-textMuted mb-2 font-medium">Senha</Text>
            <TextInput
              className="border border-so2-border rounded-md px-4 py-3 text-so2-text"
              placeholder="••••••••"
              value={password}
              onChangeText={setPassword}
              secureTextEntry
              placeholderTextColor="#9CA3AF"
            />
          </View>

          <TouchableOpacity
            className="bg-so2-primary py-3 rounded-md items-center mb-8 shadow"
            onPress={handleLogin}
          >
            <Text className="text-white text-lg font-bold">entrar</Text>
          </TouchableOpacity>

          <View className="space-y-4 items-center">
            <TouchableOpacity onPress={() => Alert.alert('Navegação', 'Ir para Recuperar Senha')}>
              <Text className="text-so2-primary font-medium">Esqueci a senha</Text>
            </TouchableOpacity>
            
            <TouchableOpacity onPress={() => Alert.alert('Navegação', 'Ir para Contato')}>
              <Text className="text-so2-primary font-medium">Contato</Text>
            </TouchableOpacity>
            
            <TouchableOpacity onPress={() => Alert.alert('Navegação', 'Ir para Primeiro Acesso')}>
              <Text className="text-so2-primary font-medium">Primeiro acesso</Text>
            </TouchableOpacity>
          </View>

        </View>
      </View>
    </ScrollView>
  );
}