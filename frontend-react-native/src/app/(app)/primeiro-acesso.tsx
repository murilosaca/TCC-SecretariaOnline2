import { useState } from 'react';
import { useRouter } from 'expo-router';
import { Linking, ScrollView, Text, TextInput, TouchableOpacity, View } from 'react-native';
import { ApiError } from '@/api/client';
import { useAuth } from '@/auth/AuthContext';
import { isStrongPassword, passwordRules, passwordScore } from '@/auth/password';
import { Banner } from '@/components/Banner';

export default function PrimeiroAcessoScreen() {
  const { completeFirstAccess } = useAuth();
  const router = useRouter();
  const [novaSenha, setNovaSenha] = useState('');
  const [confirmacao, setConfirmacao] = useState('');
  const [lgpd, setLgpd] = useState(false);
  const [aviso, setAviso] = useState<string | null>(null);
  const [enviando, setEnviando] = useState(false);
  const score = passwordScore(novaSenha);
  const podeContinuar = isStrongPassword(novaSenha) && novaSenha === confirmacao && lgpd;

  async function onSubmit() {
    setAviso(null);
    if (novaSenha !== confirmacao) {
      setAviso('As senhas não coincidem');
      return;
    }
    if (!podeContinuar) {
      return;
    }
    setEnviando(true);
    try {
      await completeFirstAccess(novaSenha);
      router.replace('/inicio');
    } catch (error) {
      setAviso(error instanceof ApiError ? error.message : 'Não foi possível concluir o primeiro acesso.');
    } finally {
      setEnviando(false);
    }
  }

  return (
    <ScrollView className="flex-1 bg-so2-background" contentContainerStyle={{ padding: 24, paddingTop: 56 }}>
      <Text className="mb-2 text-3xl font-bold text-so2-text">Primeiro acesso</Text>
      <Text className="mb-6 text-sm text-so2-textMuted">
        Defina uma senha pessoal forte e aceite a política de privacidade (LGPD) para desbloquear o sistema.
      </Text>
      {aviso ? <Banner message={aviso} /> : null}
      <Text className="mb-1 text-sm text-so2-textMuted">Nova senha</Text>
      <TextInput
        className="mb-3 rounded-md border border-so2-border bg-white px-4 py-3 text-so2-text"
        value={novaSenha}
        onChangeText={setNovaSenha}
        secureTextEntry
        accessibilityLabel="Nova senha"
      />
      <Text className="mb-2 text-xs text-so2-textMuted">Força: {score}/4</Text>
      {passwordRules.map((rule) => (
        <Text key={rule.id} className={`text-xs ${rule.test(novaSenha) ? 'text-so2-successText' : 'text-so2-textMuted'}`}>
          {rule.test(novaSenha) ? '✓' : '○'} {rule.label}
        </Text>
      ))}
      <Text className="mb-1 mt-4 text-sm text-so2-textMuted">Confirmar senha</Text>
      <TextInput
        className="mb-4 rounded-md border border-so2-border bg-white px-4 py-3 text-so2-text"
        value={confirmacao}
        onChangeText={setConfirmacao}
        secureTextEntry
        accessibilityLabel="Confirmar senha"
      />
      <TouchableOpacity className="mb-6 flex-row items-center gap-x-3" onPress={() => setLgpd((atual) => !atual)}>
        <View className={`h-5 w-5 rounded border ${lgpd ? 'border-so2-primary bg-so2-primary' : 'border-so2-border'}`} />
        <Text className="flex-1 text-sm text-so2-text">
          Li e aceito a{' '}
          <Text className="text-so2-primary" onPress={() => void Linking.openURL('https://www.ufpr.br/lgpd/')}>
            política de privacidade (LGPD)
          </Text>
        </Text>
      </TouchableOpacity>
      <TouchableOpacity
        className={`items-center rounded-md py-3 ${podeContinuar ? 'bg-so2-primary' : 'bg-gray-300'}`}
        disabled={!podeContinuar || enviando}
        onPress={() => void onSubmit()}
      >
        <Text className="font-bold text-white">{enviando ? 'Salvando...' : 'Continuar'}</Text>
      </TouchableOpacity>
    </ScrollView>
  );
}
