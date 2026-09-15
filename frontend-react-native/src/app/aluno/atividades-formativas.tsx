import { View, Text, TouchableOpacity, ScrollView, TextInput } from 'react-native';
import { useState, useEffect } from 'react';
import { Feather } from '@expo/vector-icons';
import { useRouter } from 'expo-router';

// interfaces matching the expected backend structure
interface ActivityItem {
  id: string;
  protocol: string;
  type: string;
  status: string;
  deadline: string;
}

export default function FormativeActivitiesScreen() {
  const router = useRouter();
  
  const [isLoading, setIsLoading] = useState(true);
  const [activities, setActivities] = useState<ActivityItem[]>([]);
  const [searchQuery, setSearchQuery] = useState('');

  // mock data for the progress bar
  const progressData = {
    current: 72,
    total: 120
  };

  useEffect(() => {
    // simulating api fetch
    const fetchActivities = async () => {
      setIsLoading(true);
      
      try {
        console.log('fetching formative activities data');
        
        setTimeout(() => {
          setActivities([
            {
              id: '1',
              protocol: 'SOL-001',
              type: 'Trancamento',
              status: 'Agendado',
              deadline: '12/04',
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
        }, 1000);
      } catch (error) {
        console.error('failed to fetch activities');
        setIsLoading(false);
      }
    };

    fetchActivities();
  }, []);

  const handleSearch = () => {
    console.log('searching activities for:', searchQuery);
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

      <Text className="text-3xl font-bold text-so2-text mb-6">
        Atividades formativas
      </Text>

      {/* progress card */}
      <View className="bg-white p-5 rounded-xl border border-gray-200 mb-6">
        <Text className="text-so2-textMuted mb-2">Horas formativas</Text>
        <Text className="text-3xl font-bold text-so2-text mb-2">{progressData.current}h</Text>
        <View className="flex-row items-center gap-x-2">
          <View className="flex-1 h-2 bg-gray-200 rounded-full">
            <View 
              className="h-2 bg-so2-primary rounded-full" 
              style={{ width: `${(progressData.current / progressData.total) * 100}%` }}
            />
          </View>
          <Text className="text-xs text-so2-textMuted">
            {progressData.current}/{progressData.total}h
          </Text>
        </View>
      </View>

      {/* filters */}
      <View className="flex-row items-center gap-x-3 mb-4">
        <Text className="text-so2-textMuted text-sm">Filtros:</Text>
        <TouchableOpacity className="bg-white px-3 py-1.5 rounded-full border border-blue-200 flex-row items-center gap-x-1">
          <Text className="text-so2-primary text-xs">Em análise</Text>
          <Feather name="chevron-right" size={14} color="#3F00FF" />
        </TouchableOpacity>
        <TouchableOpacity className="bg-white px-3 py-1.5 rounded-full border border-blue-200 flex-row items-center gap-x-1">
          <Text className="text-so2-primary text-xs">2026</Text>
          <Feather name="chevron-right" size={14} color="#3F00FF" />
        </TouchableOpacity>
        <TouchableOpacity className="bg-white px-3 py-1.5 rounded-full border border-gray-200 flex-row items-center gap-x-1">
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
          className="bg-so2-primary w-12 rounded-md items-center justify-center"
          onPress={handleSearch}
        >
          <Feather name="search" size={20} color="white" />
        </TouchableOpacity>
      </View>

      {/* activities list */}
      <View className="gap-y-4 mb-8">
        {!isLoading && activities.map((item) => (
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

      {/* pagination */}
      <TouchableOpacity className="items-center flex-row justify-center gap-x-2 pb-8">
        <Feather name="arrow-left" size={16} color="#6B7280" />
        <Text className="text-so2-textMuted font-medium">Anterior</Text>
      </TouchableOpacity>
    </ScrollView>
  );
}