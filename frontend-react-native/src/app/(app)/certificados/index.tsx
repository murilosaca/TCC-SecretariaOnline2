import { useQuery } from '@tanstack/react-query';
import { useState } from 'react';
import { ScrollView, Text, TouchableOpacity, View } from 'react-native';
import { certificadosApi } from '@/api/certificados';
import { AppHeader } from '@/components/AppHeader';
import { Banner } from '@/components/Banner';
import { EmptyState, LoadingState } from '@/components/ScreenState';
import { useActions } from '@/hooks/useActions';
import { truncarHash } from '@/lib/hash';
import type { Certificado } from '@/models/certificado';

export default function CertificadosScreen() {
  const lista = useQuery({
    queryKey: ['certificates', 'me'],
    queryFn: () => certificadosApi.listarMeus(),
  });

  return (
    <View className="flex-1 bg-so2-background">
      <AppHeader title="Certificados" />
      <ScrollView contentContainerStyle={{ padding: 24, paddingBottom: 48 }}>
        <Text className="mb-4 text-sm text-so2-textMuted">
          Documentos emitidos pelo sistema após aprovação da CAAF.
        </Text>
        {lista.isError ? (
          <Banner
            message="Não foi possível carregar os certificados."
            actionLabel="Tentar de novo"
            onAction={() => void lista.refetch()}
          />
        ) : null}
        {lista.isLoading ? <LoadingState label="Carregando certificados…" /> : null}
        {lista.data && lista.data.content.length === 0 ? (
          <EmptyState label="Você ainda não possui certificados emitidos." />
        ) : null}
        {lista.data?.content.map((item) => (
          <Linha key={item.id} item={item} />
        ))}
      </ScrollView>
    </View>
  );
}

function Linha({ item }: { item: Certificado }) {
  const actions = useActions(item._links);
  const data = new Date(item.emitidoEm).toLocaleDateString('pt-BR');
  const [baixando, setBaixando] = useState(false);
  const [erro, setErro] = useState<string | null>(null);

  async function baixar() {
    const href = actions.href('download') ?? actions.href('reemitir');
    if (!href) {
      return;
    }
    setErro(null);
    setBaixando(true);
    try {
      await certificadosApi.baixar(href);
    } catch {
      setErro('Não foi possível baixar o PDF.');
    } finally {
      setBaixando(false);
    }
  }

  const podeBaixar = actions.can('download') || actions.can('reemitir');

  return (
    <View className="mb-3 rounded-xl border border-so2-border bg-so2-card p-4">
      <Text className="font-bold text-so2-text">{item.titulo}</Text>
      <Text className="mt-1 text-sm text-so2-textMuted">
        {item.tipo === 'FORMATIVA' ? 'Formativa' : item.tipo} · {data}
      </Text>
      <Text className="mt-1 font-mono text-xs text-so2-textMuted">{truncarHash(item.hashSha256)}</Text>
      {podeBaixar ? (
        <TouchableOpacity
          className="mt-3 items-center rounded-md bg-so2-primary py-3"
          onPress={() => void baixar()}
          disabled={baixando}
          accessibilityRole="button"
          accessibilityLabel={`Baixar certificado de ${item.titulo}`}
        >
          <Text className="font-bold text-white">
            {baixando ? 'Baixando…' : actions.can('reemitir') && !actions.can('download') ? 'Reemitir PDF' : 'Download'}
          </Text>
        </TouchableOpacity>
      ) : null}
      {erro ? (
        <Text className="mt-2 text-sm text-so2-dangerText" accessibilityRole="alert">
          {erro}
        </Text>
      ) : null}
    </View>
  );
}
