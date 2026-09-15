import { View, Text, TouchableOpacity, ScrollView } from 'react-native';
import { useRouter } from 'expo-router';
import { Feather } from '@expo/vector-icons';

export default function MenuScreen() {
  const router = useRouter();

  return (
    <ScrollView className="flex-1 bg-so2-background" contentContainerStyle={{ padding: 24, paddingTop: 48 }}>
      <View className="flex-row justify-between items-center mb-10">
        <Text className="text-2xl font-bold text-so2-text">menu</Text>
        <TouchableOpacity onPress={() => router.back()}>
          <Feather name="x" size={28} color="#111827" />
        </TouchableOpacity>
      </View>

      <View className="gap-y-6">
        <TouchableOpacity onPress={() => router.push('/inicio-aluno' as any)} className="flex-row items-center gap-x-4">
          <Feather name="home" size={24} color="#3F00FF" />
          <Text className="text-lg text-so2-text font-medium">início</Text>
        </TouchableOpacity>

        <TouchableOpacity onPress={() => router.push('/comunicacao' as any)} className="flex-row items-center gap-x-4">
          <Feather name="message-square" size={24} color="#3F00FF" />
          <Text className="text-lg text-so2-text font-medium">comunicação</Text>
        </TouchableOpacity>

        <TouchableOpacity onPress={() => router.push('/contato' as any)} className="flex-row items-center gap-x-4">
          <Feather name="help-circle" size={24} color="#3F00FF" />
          <Text className="text-lg text-so2-text font-medium">contato</Text>
        </TouchableOpacity>

        {/* add logout logic here later */}
        <TouchableOpacity onPress={() => router.replace('/')} className="flex-row items-center gap-x-4 mt-8">
          <Feather name="log-out" size={24} color="#991B1B" />
          <Text className="text-lg text-so2-dangerText font-medium">sair</Text>
        </TouchableOpacity>
      </View>
    </ScrollView>
  );
}