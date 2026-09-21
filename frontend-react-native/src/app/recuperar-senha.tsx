import { useState } from 'react';
import { Link } from 'expo-router';
import { ActivityIndicator, ScrollView, Text, TextInput, TouchableOpacity, View } from 'react-native';
import { ApiError } from '@/api/client';
import { authApi } from '@/api/auth';
import { isValidEmail } from '@/auth/password';
import { Banner } from '@/components/Banner';

export default function RecuperarSenhaScreen() {
  const [email, setEmail] = useState('');
  const [aviso, setAviso] = useState<string | null>(null);
  const [tone, setTone] = useState<'danger' | 'warning' | 'info'>('info');
  const [enviando, setEnviando] = useState(false);
  const [sucesso, setSucesso] = useState(false);

  async function onSubmit() {
    setAviso(null);
    if (!isValidEmail(email)) {
      setTone('danger');
      setAviso('Informe um e-mail válido');
      return;
    }
    setEnviando(true);
    try {
      await authApi.recuperarSenha(email.trim());
      setSucesso(true);
      setTone('info');
      setAviso('Se este e-mail estiver cadastrado, você receberá um link válido por 24 horas. O link abre a web /nova-senha.');
    } catch (error) {
      if (error instanceof ApiError && error.status === 429) {
        setTone('warning');
        setAviso('Muitas tentativas. Aguarde antes de tentar novamente.');
      } else {
        setTone('danger');
        setAviso('Erro ao processar a solicitação. Verifique sua conexão e tente novamente.');
      }
    } finally {
      setEnviando(false);
    }
  }

  return (
    <ScrollView className="flex-1 bg-so2-background" contentContainerStyle={{ flexGrow: 1, justifyContent: 'center' }}>
      <View className="px-6 py-12">
        <View className="rounded-2xl border border-so2-border bg-so2-card p-6">
          <Text className="mb-2 text-xl font-bold text-so2-text">Recuperar senha</Text>
          <Text className="mb-4 text-sm text-so2-textMuted">
            Informe o e-mail cadastrado. A resposta é a mesma exista ou não a conta.
          </Text>
          {aviso ? <Banner message={aviso} tone={tone} /> : null}
          {!sucesso ? (
            <>
              <Text className="mb-1 text-sm font-medium text-so2-textMuted">E-mail</Text>
              <TextInput
                className="mb-4 rounded-md border border-so2-border px-4 py-3 text-so2-text"
                value={email}
                onChangeText={setEmail}
                autoCapitalize="none"
                keyboardType="email-address"
                autoComplete="email"
                editable={!enviando}
                accessibilityLabel="E-mail"
              />
              <TouchableOpacity
                className="mb-4 items-center rounded-md bg-so2-primary py-3"
                onPress={() => void onSubmit()}
                disabled={enviando}
              >
                {enviando ? (
                  <ActivityIndicator color="#fff" />
                ) : (
                  <Text className="font-bold text-white">Enviar link</Text>
                )}
              </TouchableOpacity>
            </>
          ) : null}
          <Link href="/login" asChild>
            <TouchableOpacity className="items-center py-2">
              <Text className="font-medium text-so2-primary">Voltar ao login</Text>
            </TouchableOpacity>
          </Link>
        </View>
      </View>
    </ScrollView>
  );
}
