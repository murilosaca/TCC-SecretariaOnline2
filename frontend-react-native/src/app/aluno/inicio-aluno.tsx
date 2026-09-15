import { View, Text, TouchableOpacity, ScrollView, ActivityIndicator } from 'react-native';
import { useState, useEffect } from 'react';
import { Feather } from '@expo/vector-icons';
import { apiClient } from '../../api/client';
import { router } from 'expo-router';

// interfaces matching the bff payload structure
interface DashboardData {
  studentName: string;
  courseAndPeriod: string;
  formativeHours: {
    current: number;
    required: number;
  };
  activeRequestsCount: number;
  todayEventsCount: number;
  activeWindowCount: number;
}

export default function StudentDashboardScreen() {
  const [isLoading, setIsLoading] = useState(false);
  const [isShowingRequests, setIsShowingRequests] = useState(false);
  
  // hardcoded mock data
  const [dashboardData, setDashboardData] = useState<DashboardData>({
    studentName: 'Maria Silva',
    courseAndPeriod: 'Engenharia de Software - 2026/1',
    formativeHours: { current: 72, required: 120 },
    activeRequestsCount: 3,
    todayEventsCount: 2,
    activeWindowCount: 1,
  });

  useEffect(() => {
    // fetch dashboard data from bff
    const fetchDashboard = async () => {
      // setIsLoading(true);
      try {
        // const response = await apiClient.get('/bff/dashboard/aluno');
        // setDashboardData(response.data);
        console.log('fetching dashboard data from api');
      } catch (error) {
        console.error('failed to fetch dashboard data', error);
      } finally {
        // setIsLoading(false);
      }
    };

    fetchDashboard();
  }, []);

  const renderDashboard = () => (
    <View className="flex-1">
      {/* formative hours card */}
      <View className="bg-so2-card p-5 rounded-xl border border-so2-border mb-4">
        <Text className="text-so2-textMuted mb-2">Horas formativas</Text>
        <Text className="text-3xl font-bold text-so2-text mb-2">{dashboardData.formativeHours.current}h</Text>
        <View className="w-full h-2 bg-gray-200 rounded-full mb-1">
          <View 
            className="h-2 bg-so2-primary rounded-full" 
            style={{ width: `${(dashboardData.formativeHours.current / dashboardData.formativeHours.required) * 100}%` }}
          />
        </View>
        <Text className="text-right text-xs text-so2-textMuted">
          {dashboardData.formativeHours.current}/{dashboardData.formativeHours.required}h
        </Text>
      </View>

      {/* grid cards */}
      <View className="flex-row gap-4 mb-4">
        <TouchableOpacity 
          className="flex-1 bg-so2-card p-5 rounded-xl border border-so2-border"
          onPress={() => setIsShowingRequests(true)}
        >
          <Text className="text-so2-textMuted mb-2">Solicitações</Text>
          <Text className="text-3xl font-bold text-so2-text">{dashboardData.activeRequestsCount}</Text>
        </TouchableOpacity>

        <View className="flex-1 bg-so2-card p-5 rounded-xl border border-so2-border">
          <Text className="text-so2-textMuted mb-2">Eventos hoje</Text>
          <View className="flex-row items-center gap-2">
            <Text className="text-3xl font-bold text-so2-text">{dashboardData.todayEventsCount}</Text>
            {dashboardData.activeWindowCount > 0 && (
              <View className="bg-blue-100 px-2 py-1 rounded-md">
                <Text className="text-blue-600 text-xs">{dashboardData.activeWindowCount} janela</Text>
              </View>
            )}
          </View>
        </View>
      </View>

      {/* generic card */}
      <View className="bg-so2-card p-6 rounded-xl border border-so2-border mb-4">
        <Text className="font-bold text-so2-text mb-1">Título do card</Text>
        <Text className="text-so2-textMuted">Conteúdo do card com padding 24px e borda sutil.</Text>
      </View>

      {/* event card */}
      <View className="bg-so2-card p-5 rounded-xl border border-so2-border mb-8">
        <View className="flex-row justify-between items-center mb-3">
          <View className="bg-so2-success px-2 py-1 rounded-md border border-green-200">
            <Text className="text-so2-successText text-xs">Aprovada</Text>
          </View>
          <Text className="text-so2-textMuted text-xs">08/06</Text>
        </View>
        <Text className="font-bold text-so2-text mb-2">Oficina de Metodologia Científica</Text>
        <Text className="text-so2-textMuted text-sm mb-4">
          Aprovada com 8h. Documentação validada pela coordenação do curso.
        </Text>
        <TouchableOpacity>
          <Text className="text-so2-primary font-bold">Ver detalhes →</Text>
        </TouchableOpacity>
      </View>
    </View>
  );

  const renderRequestsList = () => (
    <View className="flex-1">
      <TouchableOpacity onPress={() => setIsShowingRequests(false)} className="mb-4">
        <Text className="text-so2-primary">← voltar</Text>
      </TouchableOpacity>
      <Text className="text-xl font-bold text-so2-text mb-4">Solicitações</Text>
      
      {/* mock request cards matching colors from image */}
      <TouchableOpacity className="bg-so2-danger p-4 rounded-xl border border-red-200 mb-3 flex-row justify-between items-center">
        <View className="flex-row items-center gap-3">
          <View className="w-10 h-10 bg-white/50 rounded-full items-center justify-center">
            <Text className="text-so2-dangerText">...</Text>
          </View>
          <View>
            <Text className="font-bold text-so2-dangerText">Formativa rejeitada — Workshop IA</Text>
            <Text className="text-so2-dangerText/70 text-xs">ver parecer</Text>
          </View>
        </View>
        <Text className="text-so2-dangerText text-xs font-bold">Indeferida</Text>
      </TouchableOpacity>

      <TouchableOpacity className="bg-so2-pending p-4 rounded-xl border border-yellow-200 mb-3 flex-row justify-between items-center">
        <View className="flex-row items-center gap-3">
          <View className="w-10 h-10 bg-white/50 rounded-full items-center justify-center">
            <Text className="text-so2-pendingText">...</Text>
          </View>
          <View>
            <Text className="font-bold text-so2-pendingText">Falta Anexo — Workshop Python</Text>
            <Text className="text-so2-pendingText/70 text-xs">ver parecer</Text>
          </View>
        </View>
        <Text className="text-so2-pendingText text-xs font-bold">Em ajuste</Text>
      </TouchableOpacity>
      
      <TouchableOpacity className="bg-so2-draft p-4 rounded-xl border border-gray-200 mb-3 flex-row justify-between items-center">
        <View className="flex-row items-center gap-3">
          <View className="w-10 h-10 bg-white rounded-full items-center justify-center shadow-sm">
            <Text className="text-so2-draftText">...</Text>
          </View>
          <View>
            <Text className="font-bold text-so2-draftText">Rascunho — Workshop BD</Text>
            <Text className="text-so2-draftText/70 text-xs">ver parecer</Text>
          </View>
        </View>
        <Text className="text-so2-draftText text-xs font-bold">Rascunho</Text>
      </TouchableOpacity>

      <TouchableOpacity className="bg-so2-success p-4 rounded-xl border border-green-200 mb-3 flex-row justify-between items-center">
        <View className="flex-row items-center gap-3">
          <View className="w-10 h-10 bg-white/50 rounded-full items-center justify-center">
            <Text className="text-so2-successText">...</Text>
          </View>
          <View>
            <Text className="font-bold text-so2-successText">Rascunho — Workshop BD</Text>
            <Text className="text-so2-successText/70 text-xs">ver parecer</Text>
          </View>
        </View>
        <Text className="text-so2-successText text-xs font-bold">Deferido</Text>
      </TouchableOpacity>
    </View>
  );

  return (
    <ScrollView className="flex-1 bg-so2-background" contentContainerStyle={{ padding: 24, paddingTop: 48 }}>
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

      {/* header */}
      <View className="mb-6">
        <Text className="text-3xl font-extrabold text-so2-text mb-1">
          Olá, {dashboardData.studentName}
        </Text>
        <Text className="text-sm text-so2-textMuted">
          {dashboardData.courseAndPeriod}
        </Text>
      </View>

      {isLoading ? (
        <ActivityIndicator size="large" color="#3F00FF" />
      ) : (
        isShowingRequests ? renderRequestsList() : renderDashboard()
      )}
    </ScrollView>
  );
}