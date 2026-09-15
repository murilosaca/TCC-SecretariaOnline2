import { View, Text, TouchableOpacity, ScrollView } from 'react-native';
import { useState, useEffect } from 'react';
import { Feather } from '@expo/vector-icons';
import { useRouter } from 'expo-router';
import { apiClient } from '../../api/client';

// interfaces matching the expected backend structure for communications
interface CommunicationItem {
  id: string;
  isRead: boolean;
  type: string;
  description: string;
  deadlineDate: string;
}

export default function CommunicationScreen() {
  const router = useRouter();
  
  // states to handle all 4 screens from the design
  const [isLoading, setIsLoading] = useState(true);
  const [errorMessage, setErrorMessage] = useState<string | null>(null);
  const [communications, setCommunications] = useState<CommunicationItem[]>([]);

  useEffect(() => {
    // simulating api fetch to demonstrate states
    const fetchCommunications = async () => {
      setIsLoading(true);
      setErrorMessage(null);
      
      try {
        // const response = await apiClient.get('/bff/comunicacao/aluno');
        // setCommunications(response.data);
        
        // mock data simulating a successful response (screen 1)
        setTimeout(() => {
          setCommunications([
            {
              id: '1',
              isRead: false,
              type: 'AJUSTE',
              description: 'Ajuste na solicitação Trancamento',
              deadlineDate: '2026-06-12T00:00:00.000Z',
            },
            {
              id: '2',
              isRead: false,
              type: 'AJUSTE',
              description: 'Ajuste na solicitação Trancamento',
              deadlineDate: '2026-06-12T00:00:00.000Z',
            },
            {
              id: '3',
              isRead: true,
              type: 'AJUSTE',
              description: 'Ajuste na solicitação Trancamento',
              deadlineDate: '2026-06-12T00:00:00.000Z',
            }
          ]);
          setIsLoading(false);
          
          // uncomment below to test empty state (screen 2)
          // setCommunications([]);
          
          // uncomment below to test error state (screen 3)
          // setErrorMessage('Erro ao enviar solicitação\nTente novamente ou contate o suporte.');
          
        }, 1500);
      } catch (error) {
        setErrorMessage('Erro ao carregar comunicações.');
        setIsLoading(false);
      }
    };

    fetchCommunications();
  }, []);

  const formatDate = (isoString: string) => {
    const date = new Date(isoString);
    return `Prazo ${date.toLocaleDateString('pt-BR')}`;
  };

  // screen 4: skeleton loading state
  const renderSkeletons = () => (
    <View className="gap-y-4">
      {[1, 2, 3, 4, 5].map((key) => (
        <View key={key} className="h-20 bg-gray-100 rounded-xl" />
      ))}
    </View>
  );

  // screen 2: empty state
  const renderEmptyState = () => (
    <View className="flex-1 items-center justify-center mt-20">
      <View className="w-24 h-24 bg-gray-200 rounded-full mb-6" />
      <Text className="text-lg font-bold text-so2-text mb-2">Nenhuma pendência</Text>
      <Text className="text-so2-textMuted text-center px-10">
        Você está em dia com suas obrigações acadêmicas.
      </Text>
    </View>
  );

  // screen 1: populated list
  const renderList = () => (
    <View className="gap-y-6">
      {communications.map((item) => (
        <View key={item.id} className="flex-row items-start gap-x-4">
          <View className={`w-2 h-2 rounded-full mt-4 ${item.isRead ? 'bg-transparent' : 'bg-so2-primary'}`} />
          
          <View className="w-12 h-12 bg-so2-pending rounded-full items-center justify-center">
            <Feather name="search" size={20} color="#92400E" />
          </View>
          
          <View className="flex-1 justify-center py-1">
            <Text className="text-so2-text font-medium leading-relaxed">
              {item.description}
            </Text>
            <Text className="text-so2-textMuted text-xs mt-1">
              {formatDate(item.deadlineDate)}
            </Text>
          </View>
        </View>
      ))}
    </View>
  );

  return (
    <ScrollView className="flex-1 bg-so2-background" contentContainerStyle={{ padding: 24, paddingTop: 48, flexGrow: 1 }}>
      {/* top navigation bar */}
      <View className="flex-row justify-between items-center mb-8">
        <TouchableOpacity onPress={() => router.push('/../shared/menu' as any)}>
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

      {/* screen 3: error banner */}
      {errorMessage && (
        <View className="bg-red-50 border border-red-100 rounded-xl p-4 mb-6 flex-row items-start justify-between">
          <View className="flex-row items-start gap-x-3 flex-1">
            <Feather name="alert-circle" size={20} color="#991B1B" />
            <Text className="text-so2-dangerText text-sm flex-1">
              {errorMessage}
            </Text>
          </View>
          <TouchableOpacity onPress={() => setErrorMessage(null)}>
            <Feather name="x" size={20} color="#991B1B" />
          </TouchableOpacity>
        </View>
      )}

      <View className="mb-6">
        <Text className="text-3xl font-bold text-so2-text mb-6">
          Comunicação
        </Text>
        
        <View className="border-b-2 border-so2-primary self-start px-2 mb-6">
          <Text className="text-so2-text pb-2 font-medium">Pendências</Text>
        </View>
        
        {!isLoading && communications.length > 0 && (
          <View className="flex-row items-center gap-x-3 mb-6">
            <Text className="text-so2-textMuted text-sm">Filtros:</Text>
            <TouchableOpacity className="bg-white px-4 py-1.5 rounded-full border border-gray-200">
              <Text className="text-so2-primary text-xs">Em análise</Text>
            </TouchableOpacity>
            <TouchableOpacity className="bg-white px-4 py-1.5 rounded-full border border-gray-200">
              <Text className="text-so2-primary text-xs">2026</Text>
            </TouchableOpacity>
            <TouchableOpacity className="bg-white px-4 py-1.5 rounded-full border border-gray-200">
              <Text className="text-so2-primary text-xs">Tipo</Text>
            </TouchableOpacity>
          </View>
        )}
      </View>

      {isLoading 
        ? renderSkeletons() 
        : communications.length === 0 
          ? renderEmptyState() 
          : renderList()
      }
    </ScrollView>
  );
}