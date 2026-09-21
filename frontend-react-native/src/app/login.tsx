import { useCallback, useState } from 'react';
import { Redirect, Link } from 'expo-router';
import { ActivityIndicator, Text, TextInput, TouchableOpacity, View, ScrollView } from 'react-native';
import { ApiError } from '@/api/client';
import { useAuth } from '@/auth/AuthContext';
import { Banner } from '@/components/Banner';

export default function LoginScreen() {
  const { status, mustChangePassword, login } = useAuth();
  const [identificador, setIdentificador] = useState('');
  const [senha, setSenha] = useState('');
  const [mostrarSenha, setMostrarSenha] = useState(false);
  const [aviso, setAviso] = useState<string | null>(null);
  const [avisoTone, setAvisoTone] = useState<'danger' | 'warning'>('danger');
  const [enviando, setEnviando] = useState(false);

  const onSubmit = useCallback(async () => {
    setAviso(null);
    if (!identificador.trim() || !senha) {
      setAviso('Informe e-mail ou GRR e a senha.');
      return;
    }
    setEnviando(true);
    try {
      await login(identificador.trim(), senha);
    } catch (error) {
      setSenha('');
      if (error instanceof ApiError && error.status === 429) {
        setAvisoTone('warning');
        setAviso('Muitas tentativas. Aguarde antes de tentar novamente.');
      } else {
        setAvisoTone('danger');
        setAviso('Credenciais inválidas. Verifique seus dados e tente novamente.');
      }
    } finally {
      setEnviando(false);
    }
  }, [identificador, senha, login]);

  if (status === 'loading') {
    return (
      <View className="flex-1 items-center justify-center bg-so2-background">
        <ActivityIndicator color="#3F00FF" />
      </View>
    );
  }
  if (status === 'authenticated') {
    return <Redirect href={mustChangePassword ? '/primeiro-acesso' : '/inicio'} />;
  }

  return (
    <ScrollView className="flex-1 bg-so2-background" contentContainerStyle={{ flexGrow: 1, justifyContent: 'center' }}>
      <View className="px-6 py-12">
        <Text className="mb-1 text-center text-2xl font-bold text-so2-text">Secretaria Online 2</Text>
        <Text className="mb-8 text-center text-so2-textMuted">SEPT/UFPR · Entrar</Text>
        <View className="rounded-2xl border border-so2-border bg-so2-card p-6">
          {aviso ? <Banner message={aviso} tone={avisoTone} /> : null}
          <Text className="mb-1 text-sm font-medium text-so2-textMuted">E-mail ou GRR</Text>
          <TextInput
            className="mb-4 rounded-md border border-so2-border px-4 py-3 text-so2-text"
            value={identificador}
            onChangeText={setIdentificador}
            autoCapitalize="none"
            autoCorrect={false}
            autoComplete="username"
            placeholder="GRR20241234 ou e-mail"
            placeholderTextColor="#9CA3AF"
            accessibilityLabel="E-mail ou GRR"
          />
          <Text className="mb-1 text-sm font-medium text-so2-textMuted">Senha</Text>
          <View className="mb-6 flex-row items-center rounded-md border border-so2-border">
            <TextInput
              className="flex-1 px-4 py-3 text-so2-text"
              value={senha}
              onChangeText={setSenha}
              secureTextEntry={!mostrarSenha}
              autoComplete="password"
              placeholderTextColor="#9CA3AF"
              accessibilityLabel="Senha"
            />
            <TouchableOpacity
              className="px-3 py-3"
              onPress={() => setMostrarSenha((atual) => !atual)}
              accessibilityLabel={mostrarSenha ? 'Ocultar senha' : 'Mostrar senha'}
            >
              <Text className="text-so2-primary">{mostrarSenha ? 'Ocultar' : 'Mostrar'}</Text>
            </TouchableOpacity>
          </View>
          <TouchableOpacity
            className="mb-4 items-center rounded-md bg-so2-primary py-3"
            onPress={() => void onSubmit()}
            disabled={enviando}
          >
            {enviando ? <ActivityIndicator color="#fff" /> : <Text className="text-lg font-bold text-white">Entrar</Text>}
          </TouchableOpacity>
          <Link href="/recuperar-senha" asChild>
            <TouchableOpacity className="items-center py-2">
              <Text className="font-medium text-so2-primary">Esqueci minha senha</Text>
            </TouchableOpacity>
          </Link>
          <Link href="/contato" asChild>
            <TouchableOpacity className="items-center py-2">
              <Text className="font-medium text-so2-primary">Contato</Text>
            </TouchableOpacity>
          </Link>
        </View>
      </View>
    </ScrollView>
  );
}
