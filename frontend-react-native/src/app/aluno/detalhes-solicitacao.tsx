import { View, Text, TouchableOpacity, ScrollView } from 'react-native';
import { useState, useEffect } from 'react';
import { Feather } from '@expo/vector-icons';
import { useRouter } from 'expo-router';
// import { apiClient } from '../../api/client';

// interfaces matching the expected backend structure
interface TimelineEvent {
  id: string;
  title: string;
  description: string;
  date: string;
}

interface Attachment {
  id: string;
  filename: string;
  size: string;
}

interface RequestDetails {
  id: string;
  title: string;
  protocol: string;
  events: TimelineEvent[];
  attachments: Attachment[];
}

export default function RequestDetailsScreen() {
  const router = useRouter();
  
  const [isLoading, setIsLoading] = useState(true);
  const [details, setDetails] = useState<RequestDetails | null>(null);

  useEffect(() => {
    // simulating api fetch for request details
    const fetchDetails = async () => {
      setIsLoading(true);
      
      try {
        console.log('fetching specific request details');
        
        // mock data simulating a successful response (screen 2)
        setTimeout(() => {
          setDetails({
            id: '1',
            title: 'Trancamento de matrícula',
            protocol: '2026-0042',
            events: [
              {
                id: 'e1',
                title: 'Solicitação submetida',
                description: 'Protocolo 2026-0042 criado',
                date: '08/06/2026 - 14:32'
              },
              {
                id: 'e2',
                title: 'Solicitação submetida',
                description: 'Protocolo 2026-0042 criado',
                date: '08/06/2026 - 14:32'
              },
              {
                id: 'e3',
                title: 'Solicitação submetida',
                description: 'Protocolo 2026-0042 criado',
                date: '08/06/2026 - 14:32'
              }
            ],
            attachments: [
              { id: 'a1', filename: 'comprovante.pdf', size: '248 KB' },
              { id: 'a2', filename: 'comprovante.pdf', size: '248 KB' }
            ]
          });
          setIsLoading(false);
          
          // uncomment below to test empty state (screen 1)
          // setDetails({ ...details, events: [], attachments: [] } as RequestDetails);
          
        }, 1200);
      } catch (error) {
        console.error('failed to fetch request details');
        setIsLoading(false);
      }
    };

    fetchDetails();
  }, []);

  // screen 3: skeleton loading state
  const renderSkeletons = () => (
    <View className="gap-y-4 mt-6">
      {[1, 2, 3, 4, 5].map((key) => (
        <View key={key} className="h-24 bg-gray-100 rounded-xl" />
      ))}
    </View>
  );

  // screen 1: empty state
  const renderEmptyState = () => (
    <View className="flex-1 items-center justify-center mt-20">
      <Text className="text-lg font-bold text-so2-text mb-2">Nenhuma pendência</Text>
      <Text className="text-so2-textMuted text-center px-10">
        Você está em dia com suas obrigações acadêmicas.
      </Text>
    </View>
  );

  // screen 2: populated state with timeline and files
  const renderPopulatedState = () => {
    if (!details) return null;
    
    return (
      <View className="flex-1 mt-6">
        {/* generic info card */}
        <View className="bg-white p-6 rounded-xl border border-gray-200 mb-8">
          <Text className="font-bold text-so2-text mb-2">Título do card</Text>
          <Text className="text-so2-textMuted text-sm">
            Conteúdo do card com padding 24px e borda sutil.
          </Text>
        </View>

        {/* timeline */}
        <View className="mb-8 pl-2">
          {details.events.map((event, index) => {
            const isLast = index === details.events.length - 1;
            return (
              <View key={event.id} className="flex-row mb-6">
                {/* timeline line and dot indicator */}
                <View className="items-center mr-4 relative">
                  {!isLast && (
                    <View className="absolute top-3 w-px h-full bg-gray-300" />
                  )}
                  <View className="w-3 h-3 rounded-full bg-so2-primary mt-1.5 z-10" />
                </View>
                
                {/* event content */}
                <View className="flex-1">
                  <Text className="font-bold text-so2-text text-base">{event.title}</Text>
                  <Text className="text-so2-textMuted text-sm mt-0.5">{event.description}</Text>
                  <Text className="text-gray-400 text-xs mt-0.5">{event.date}</Text>
                </View>
              </View>
            );
          })}
        </View>

        {/* attachments */}
        <View className="gap-y-3">
          {details.attachments.map((file) => (
            <TouchableOpacity key={file.id} className="flex-row items-center bg-white border border-gray-200 rounded-lg p-4 gap-x-4">
              <View className="w-10 h-10 bg-blue-50 rounded-md items-center justify-center">
                <Feather name="file" size={20} color="#3F00FF" />
              </View>
              <View className="flex-1">
                <Text className="font-bold text-so2-text text-sm">{file.filename}</Text>
                <Text className="text-so2-textMuted text-xs mt-0.5">{file.size}</Text>
              </View>
            </TouchableOpacity>
          ))}
        </View>
      </View>
    );
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

      {/* breadcrumb */}
      <TouchableOpacity 
        className="flex-row items-center gap-x-2 mb-4"
        onPress={() => router.back()}
      >
        <Feather name="chevron-right" size={16} color="#6B7280" />
        <Text className="text-so2-textMuted text-sm">Solicitações</Text>
      </TouchableOpacity>

      <Text className="text-3xl font-extrabold text-so2-text mb-4">
        {details?.title || 'Carregando...'}
      </Text>
      
      {/* protocol badge */}
      <View className="flex-row items-center gap-x-2 border border-gray-200 bg-white rounded-md px-3 py-1.5 self-start">
        <Feather name="tag" size={14} color="#6B7280" />
        <Text className="text-so2-textMuted text-sm font-medium">
          {details?.protocol || '---'}
        </Text>
      </View>

      {isLoading 
        ? renderSkeletons() 
        : (details?.events.length === 0 ? renderEmptyState() : renderPopulatedState())
      }
    </ScrollView>
  );
}