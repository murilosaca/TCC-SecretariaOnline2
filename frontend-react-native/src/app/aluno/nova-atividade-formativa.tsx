import { View, Text, TouchableOpacity, ScrollView, TextInput } from 'react-native';
import { useState } from 'react';
import { Feather } from '@expo/vector-icons';
import { useRouter } from 'expo-router';

export default function NewFormativeActivityScreen() {
  const router = useRouter();
  
  const [nameOne, setNameOne] = useState('');
  const [nameTwo, setNameTwo] = useState('');

  const handleSubmit = () => {
    console.log('submitting new formative activity');
  };

  return (
    <ScrollView className="flex-1 bg-so2-background" contentContainerStyle={{ padding: 24, paddingTop: 48, flexGrow: 1 }}>
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
          <View className="absolute top-0 right-0 w-2.5 h-2.5 bg-red-500 rounded-full border-2 border-so2-background" />
        </TouchableOpacity>
      </View>

      <Text className="text-3xl font-bold text-so2-text mb-8">
        Nova atividade formativa
      </Text>

      {/* form fields */}
      <View className="mb-4">
        <Text className="text-so2-text text-sm mb-2">Tipo de solicitação *</Text>
        <View className="border border-gray-200 rounded-md px-4 py-3 bg-white flex-row justify-between items-center">
          <Text className="text-so2-textMuted">Selecione...</Text>
          <Feather name="chevron-down" size={16} color="#9CA3AF" />
        </View>
      </View>

      <View className="mb-4">
        <Text className="text-so2-text text-sm mb-2">Nome completo</Text>
        <TextInput
          className="border border-gray-200 rounded-md px-4 py-3 bg-white text-so2-text"
          placeholder="Ex: Ana Silva"
          value={nameOne}
          onChangeText={setNameOne}
        />
      </View>

      <View className="mb-6">
        <Text className="text-so2-text text-sm mb-2">Nome completo</Text>
        <TextInput
          className="border border-gray-200 rounded-md px-4 py-3 bg-white text-so2-text"
          placeholder="Ex: Ana Silva"
          value={nameTwo}
          onChangeText={setNameTwo}
        />
      </View>

      {/* file upload mock */}
      <View className="border-2 border-dashed border-gray-300 rounded-lg p-8 items-center justify-center bg-white mb-6">
        <Text className="text-so2-text font-medium mb-1">Arraste arquivos ou clique para enviar</Text>
        <Text className="text-xs text-so2-textMuted">PDF, JPG, PNG — máx. 10 MB por arquivo</Text>
      </View>
      
      {/* mock upload progress */}
      <View className="bg-white p-4 rounded-md border border-gray-200 flex-row items-center gap-x-4 mb-4">
        <Text className="text-sm text-so2-text flex-1">comprovante.pdf</Text>
        <View className="w-24 h-2 bg-gray-200 rounded-full">
          <View className="w-16 h-2 bg-so2-primary rounded-full" />
        </View>
      </View>

      <Text className="text-xs text-so2-textMuted mb-8">
        SHA-256 calculado no cliente antes do upload
      </Text>

    </ScrollView>
  );
}