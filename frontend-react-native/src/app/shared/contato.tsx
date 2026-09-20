import { View, Text, TouchableOpacity, ScrollView } from 'react-native';
import { useRouter } from 'expo-router';

export default function ContactScreen() {
  const router = useRouter();

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

        {/* contact card */}
        <View className="bg-so2-card p-8 rounded-2xl shadow-lg border border-so2-border">
          
          <View className="items-center mb-6">
            <View className="w-12 h-12 bg-so2-primary rounded-xl justify-center items-center mb-4">
              <Text className="text-white text-2xl font-extrabold">S2</Text>
            </View>
            <Text className="text-xl font-bold text-so2-text mb-1">
              SecretariaOnline
            </Text>
            <Text className="text-sm text-so2-textMuted">
              Contato
            </Text>
          </View>

          {/* hardcoded contact info box */}
          <View className="border border-so2-border rounded-lg p-5 mb-8">
            <Text className="font-bold text-so2-text mb-3">
              Secretaria Acadêmica — SEPT/UFPR
            </Text>
            <Text className="text-so2-text text-sm mb-1">
              Rua dos Funcionários, 1800 — Jardim Botânico
            </Text>
            <Text className="text-so2-text text-sm mb-1">
              (41) 3360-0000
            </Text>
            <Text className="text-so2-text text-sm mb-1">
              Seg-Sex, 9h-17h
            </Text>
            <Text className="text-so2-text text-sm">
              secretaria@ufpr.br
            </Text>
          </View>

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