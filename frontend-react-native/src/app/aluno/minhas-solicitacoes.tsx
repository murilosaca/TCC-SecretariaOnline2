import { View, Text, TouchableOpacity, ScrollView, TextInput } from 'react-native';
import { useState, useEffect } from 'react';
import { Feather } from '@expo/vector-icons';
import { useRouter } from 'expo-router';
// import { apiClient } from '../../api/client';

// interfaces matching the expected backend structure and hateoas links
interface HateoasLinks {
  ver?: string;
  cancelar?: string;
  reenviar?: string;
}

interface RequestItem {
  id: string;
  protocol: string;
  type: string;
  status: string;
  deadline: string;
  _links?: HateoasLinks;
}

export default function MyRequestsScreen() {
  const router = useRouter();
  
  // states to handle the empty and populated screens
  const [isLoading, setIsLoading] = useState(true);
  const [requests, setRequests] = useState<RequestItem[]>([]);
  const [searchQuery, setSearchQuery] = useState('');

  useEffect(() => {
    // simulating api fetch
    const fetchRequests = async () => {
      setIsLoading(true);
      
      try {
        console.log('fetching requests data');
        
        // mock data simulating a successful response
        setTimeout(() => {
          setRequests([
            {
              id: '1',
              protocol: 'SOL-001',
              type: 'Trancamento',
              status: 'Agendado',
              deadline: '12/04',
              _links: { ver: '/requests/1' }
            },
            {
              id: '2',
              protocol: 'SOL-002',
              type: 'Aproveitamento',
              status: 'Agendado',
              deadline: '15/04',
            },
            {
              id: '3',
              protocol: 'SOL-003',
              type: 'Matricula',
              status: 'Agendado',
              deadline: '18/04',
            }
          ]);
          setIsLoading(false);
          
          // uncomment below to test empty state
          // setRequests([]);
          
        }, 1000);
      } catch (error) {
        console.error('failed to fetch requests');
        setIsLoading(false);
      }
    };

    fetchRequests();
  }, []);

  const handleSearch = () => {
    console.log('searching for:', searchQuery);
  };

  const renderEmptyState = () => (
    <View className="flex-1 items-center justify-center mt-20">
      <Text className="text-lg font-bold text-so2-text mb-2">Nenhuma pendência</Text>
      <Text className="text-so2-textMuted text-center px-10">
        Você está em dia com suas obrigações acadêmicas.
      </Text>
    </View>
  );

  const renderList = () => (
    <View className="flex-1">
      {/* filters mock */}
      <View className="flex-row items-center gap-x-3 mb-4">
        <Text className="text-so2-textMuted text-sm">Filtros:</Text>
        <TouchableOpacity className="bg-white px-3 py-1.5 rounded border border-gray-200 flex-row items-center gap-x-1">
          <Text className="text-so2-primary text-xs">Em análise</Text>
          <Feather name="chevron-right" size={14} color="#3F00FF" />
        </TouchableOpacity>
        <TouchableOpacity className="bg-white px-3 py-1.5 rounded border border-gray-200 flex-row items-center gap-x-1">
          <Text className="text-so2-primary text-xs">2026</Text>
          <Feather name="chevron-right" size={14} color="#3F00FF" />
        </TouchableOpacity>
        <TouchableOpacity className="bg-white px-3 py-1.5 rounded border border-gray-200 flex-row items-center gap-x-1">
          <Text className="text-so2-textMuted text-xs">Tipo</Text>
          <Feather name="chevron-right" size={14} color="#9CA3AF" />
        </TouchableOpacity>
      </View>

      {/* search bar */}
      <View className="flex-row gap-x-2 mb-6">
        <TextInput
          className="flex-1 border border-gray-200 rounded-md px-4 py-2 bg-white text-so2-text"
          placeholder="Buscar..."
          value={searchQuery}
          onChangeText={setSearchQuery}
        />
        <TouchableOpacity 
          className="bg-so2-primary px-4 py-2 rounded-md justify-center"
          onPress={handleSearch}
        >
          <Text className="text-white font-medium">Buscar</Text>
        </TouchableOpacity>
      </View>

      {/* requests list */}
      <View className="gap-y-4">
        {requests.map((item) => (
          <View key={item.id} className="bg-white p-4 rounded-lg border border-gray-200">
            <View className="flex-row justify-between mb-2">
              <Text className="font-bold text-so2-text text-base">{item.protocol}</Text>
              <Text className="text-so2-textMuted text-sm">{item.type}</Text>
            </View>
            
            <View className="flex-row items-center gap-x-4 mt-2">
              <View className="bg-blue-50 px-3 py-1 rounded-full border border-blue-100">
                <Text className="text-blue-700 text-xs">{item.status}</Text>
              </View>
              <Text className="text-so2-text text-sm">Prazo: {item.deadline}</Text>
            </View>
          </View>
        ))}
      </View>

      {/* pagination mock */}
      <TouchableOpacity className="mt-8 items-center flex-row justify-center gap-x-2">
        <Feather name="arrow-left" size={16} color="#6B7280" />
        <Text className="text-so2-textMuted font-medium">Anterior</Text>
      </TouchableOpacity>
    </View>
  );

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

      <View className="mb-8">
        <Text className="text-3xl font-bold text-so2-text mb-6">
          Minhas solicitações
        </Text>
        
        <TouchableOpacity 
          className="bg-so2-primary self-start px-6 py-2 rounded-md"
          onPress={() => router.back()}
        >
          <Text className="text-white font-medium">Voltar</Text>
        </TouchableOpacity>
      </View>

      {!isLoading && (requests.length === 0 ? renderEmptyState() : renderList())}
    </ScrollView>
  );
}