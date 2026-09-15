import { View, Text, TouchableOpacity, ScrollView, TextInput } from 'react-native';
import { useState } from 'react';
import { Feather } from '@expo/vector-icons';
import { useRouter } from 'expo-router';

export default function NewRequestScreen() {
  const router = useRouter();
  
  // form states
  const [currentStep, setCurrentStep] = useState(1);
  const [errorMessage, setErrorMessage] = useState<string | null>(null);
  const [grr, setGrr] = useState('GRR20240001');
  const [justification, setJustification] = useState('');
  
  // final step states
  const [receiveEmail, setReceiveEmail] = useState(false);
  const [confirmTruth, setConfirmTruth] = useState(false);

  const handleNextStep = () => {
    console.log('advancing to next step', currentStep + 1);
    setCurrentStep(prev => Math.min(prev + 1, 3));
  };

  const handlePrevStep = () => {
    console.log('returning to previous step', currentStep - 1);
    setCurrentStep(prev => Math.max(prev - 1, 1));
  };

  const handleSubmit = () => {
    if (!confirmTruth) {
      setErrorMessage('you must confirm the information is true.');
      return;
    }
    
    console.log('submitting dynamic form payload');
    // api call simulation
    setErrorMessage('Erro ao enviar solicitação\nTente novamente ou entre em contato com a secretaria.');
  };

  const renderStepper = () => (
    <View className="flex-row justify-between items-center mb-8 px-4">
      {/* step 1 */}
      <View className="items-center">
        <View className={`w-8 h-8 rounded-full items-center justify-center ${currentStep >= 1 ? 'bg-so2-primary' : 'bg-gray-200'}`}>
          <Text className={currentStep >= 1 ? 'text-white' : 'text-gray-500'}>1</Text>
        </View>
        <Text className="text-xs text-so2-textMuted mt-1">Tipo</Text>
      </View>
      
      <View className="flex-1 h-px bg-gray-200 mx-2" />
      
      {/* step 2 */}
      <View className="items-center">
        <View className={`w-8 h-8 rounded-full items-center justify-center border-2 ${currentStep >= 2 ? 'border-so2-primary bg-so2-primary' : 'border-gray-300 bg-white'}`}>
          <Text className={currentStep >= 2 ? 'text-white' : 'text-so2-primary'}>2</Text>
        </View>
        <Text className="text-xs text-so2-textMuted mt-1">Detalhes</Text>
      </View>

      <View className="flex-1 h-px bg-gray-200 mx-2" />
      
      {/* step 3 */}
      <View className="items-center">
        <View className={`w-8 h-8 rounded-full items-center justify-center ${currentStep >= 3 ? 'bg-so2-primary' : 'bg-gray-200'}`}>
          <Text className={currentStep >= 3 ? 'text-white' : 'text-gray-500'}>3</Text>
        </View>
        <Text className="text-xs text-so2-textMuted mt-1">Revisão</Text>
      </View>
    </View>
  );

  const renderStepOne = () => (
    <View>
      <View className="mb-4">
        <Text className="text-so2-text text-sm mb-2">GRR *</Text>
        <TextInput
          className="border border-gray-200 rounded-md px-4 py-3 bg-white text-so2-text"
          value={grr}
          onChangeText={setGrr}
        />
        <Text className="text-xs text-so2-textMuted mt-1">Número de matrícula UFPR</Text>
      </View>

      <View className="mb-4">
        <Text className="text-so2-text text-sm mb-2">Tipo de solicitação *</Text>
        <View className="border border-gray-200 rounded-md px-4 py-3 bg-white flex-row justify-between items-center">
          <Text className="text-so2-textMuted">Selecione...</Text>
          <Feather name="chevron-down" size={16} color="#9CA3AF" />
        </View>
      </View>

      <View className="mb-4">
        <Text className="text-so2-text text-sm mb-2">Justificativa *</Text>
        <TextInput
          className="border border-gray-200 rounded-md px-4 py-3 bg-white text-so2-text"
          placeholder="Descreva o motivo da solicitação em no mínimo 50 caracteres..."
          multiline
          numberOfLines={4}
          value={justification}
          onChangeText={setJustification}
          textAlignVertical="top"
        />
        <Text className="text-xs text-so2-textMuted mt-1">0/500 caracteres</Text>
      </View>

      {/* file upload mock */}
      <View className="border-2 border-dashed border-gray-300 rounded-lg p-6 items-center justify-center bg-white mb-6">
        <Text className="text-so2-text font-medium mb-1">Arraste arquivos ou clique para enviar</Text>
        <Text className="text-xs text-so2-textMuted">PDF, JPG, PNG — máx. 10 MB por arquivo</Text>
      </View>
      
      {/* mock upload progress */}
      <View className="bg-white p-3 rounded-md border border-gray-200 flex-row items-center gap-x-3 mb-6">
        <Text className="text-sm text-so2-text flex-1">comprovante.pdf</Text>
        <View className="w-24 h-1.5 bg-gray-200 rounded-full">
          <View className="w-16 h-1.5 bg-so2-primary rounded-full" />
        </View>
      </View>

      <View className="flex-row justify-between items-center">
        <TouchableOpacity onPress={handlePrevStep}>
          <Text className="text-so2-text font-medium">voltar</Text>
        </TouchableOpacity>
        <TouchableOpacity 
          className="bg-so2-primary px-6 py-2 rounded-md"
          onPress={handleNextStep}
        >
          <Text className="text-white font-medium">avançar</Text>
        </TouchableOpacity>
      </View>
    </View>
  );

  const renderStepThree = () => (
    <View>
      <View className="bg-white p-5 rounded-xl border border-gray-200 mb-6">
        <Text className="font-bold text-so2-text mb-2">Título do card</Text>
        <Text className="text-so2-textMuted text-sm">Conteúdo do card com padding 24px e borda sutil.</Text>
      </View>

      <View className="gap-y-4 mb-8">
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

        <TouchableOpacity 
          className="flex-row items-center gap-x-3" 
          onPress={() => setConfirmTruth(!confirmTruth)}
        >
          <Feather 
            name={confirmTruth ? "check-square" : "square"} 
            size={20} 
            color={confirmTruth ? "#3F00FF" : "#9CA3AF"} 
          />
          <Text className="text-so2-text text-sm flex-1">Confirmo que as informações são verdadeiras</Text>
        </TouchableOpacity>
      </View>
      
      <Text className="text-xs text-so2-textMuted mb-6">Após confirmar, será gerado número anual</Text>

      <View className="flex-row justify-between items-center">
        <TouchableOpacity onPress={handlePrevStep}>
          <Text className="text-so2-text font-medium">Ghost</Text>
        </TouchableOpacity>
        <TouchableOpacity 
          className={`${confirmTruth ? 'bg-so2-primary' : 'bg-blue-300'} px-6 py-2 rounded-md`}
          onPress={handleSubmit}
        >
          <Text className="text-white font-medium">Enviar</Text>
        </TouchableOpacity>
      </View>
    </View>
  );

  return (
    <ScrollView className="flex-1 bg-so2-background" contentContainerStyle={{ padding: 24, paddingTop: 48, flexGrow: 1 }}>
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

      <View className="mb-6">
        <Text className="text-3xl font-bold text-so2-text mb-6">
          Nova solicitação
        </Text>
        
        {renderStepper()}
      </View>

      {errorMessage && (
        <View className="bg-red-50 border border-red-100 rounded-xl p-4 mb-6 flex-row items-start justify-between">
          <View className="flex-row items-start gap-x-3 flex-1">
            <Feather name="alert-circle" size={20} color="#991b1b" />
            <Text className="text-so2-dangerText text-sm flex-1">
              {errorMessage}
            </Text>
          </View>
          <TouchableOpacity onPress={() => setErrorMessage(null)}>
            <Feather name="x" size={20} color="#991B1B" />
          </TouchableOpacity>
        </View>
      )}

      {currentStep === 1 && renderStepOne()}
      {/* bypassing step 2 for now as we don't have the explicit fields for it */}
      {currentStep === 2 && (
         <View className="items-center mt-10">
           <Text className="text-so2-textMuted">step 2 details mock</Text>
           <TouchableOpacity className="bg-so2-primary px-6 py-2 rounded-md mt-6" onPress={handleNextStep}>
             <Text className="text-white font-medium">avançar</Text>
           </TouchableOpacity>
         </View>
      )}
      {currentStep === 3 && renderStepThree()}

    </ScrollView>
  );
}