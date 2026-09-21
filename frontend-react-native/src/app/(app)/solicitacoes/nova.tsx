import { useMemo, useState } from 'react';
import { useRouter } from 'expo-router';
import { useMutation, useQuery } from '@tanstack/react-query';
import { ScrollView, Text, TouchableOpacity, View } from 'react-native';
import { ApiError } from '@/api/client';
import { solicitacoesApi } from '@/api/solicitacoes';
import { AppHeader } from '@/components/AppHeader';
import { Banner } from '@/components/Banner';
import { DynamicForm } from '@/components/DynamicForm';
import { EmptyState, LoadingState } from '@/components/ScreenState';
import { fieldsFromSchema, validateAgainstSchema } from '@/lib/formSchema';
import type { RequestType } from '@/models/solicitacao';

export default function NovaSolicitacaoScreen() {
  const router = useRouter();
  const [passo, setPasso] = useState(1);
  const [tipoCodigo, setTipoCodigo] = useState<string | null>(null);
  const [values, setValues] = useState<Record<string, unknown>>({});
  const [errors, setErrors] = useState<Record<string, string>>({});

  const tipos = useQuery({
    queryKey: ['request-types'],
    queryFn: () => solicitacoesApi.listarTipos(),
  });

  const tipoSelecionado = useMemo(
    () => tipos.data?.content.find((tipo) => tipo.codigo === tipoCodigo) ?? null,
    [tipos.data, tipoCodigo],
  );
  const fields = fieldsFromSchema(tipoSelecionado?.formSchema);

  const criar = useMutation({
    mutationFn: () => solicitacoesApi.criar(tipoCodigo as string, values),
    onSuccess: (criada) => router.replace(`/solicitacoes/${criada.id}`),
  });

  function escolherTipo(tipo: RequestType) {
    setTipoCodigo(tipo.codigo);
    setValues({});
    setErrors({});
    setPasso(2);
  }

  function continuarFormulario() {
    if (!tipoSelecionado) {
      return;
    }
    const atuais = validateAgainstSchema(tipoSelecionado.formSchema, values);
    setErrors(atuais);
    if (Object.keys(atuais).length === 0) {
      setPasso(3);
    }
  }

  return (
    <View className="flex-1 bg-so2-background">
      <AppHeader title="Nova solicitação" showMenu={false} />
      <ScrollView contentContainerStyle={{ padding: 24 }}>
        <Text className="mb-4 text-sm text-so2-textMuted">
          Wizard genérico dirigido pelo form_schema do RequestType.
        </Text>
        <Text className="mb-4 text-xs text-so2-textMuted">Passo {passo} de 3</Text>
        {tipos.isError ? (
          <Banner
            message="Não foi possível carregar os tipos."
            actionLabel="Tentar de novo"
            onAction={() => void tipos.refetch()}
          />
        ) : null}
        {criar.isError ? (
          <Banner message={criar.error instanceof ApiError ? criar.error.message : 'Falha ao abrir a solicitação.'} />
        ) : null}

        {passo === 1 ? (
          <View>
            {tipos.isLoading ? <LoadingState label="Carregando tipos…" /> : null}
            {tipos.data && tipos.data.content.length === 0 ? (
              <EmptyState label="Nenhum tipo de solicitação disponível para você." />
            ) : null}
            {tipos.data?.content.map((tipo) => (
              <TouchableOpacity
                key={tipo.id}
                className="mb-3 rounded-xl border border-so2-border bg-so2-card p-4"
                onPress={() => escolherTipo(tipo)}
              >
                <Text className="font-bold text-so2-text">{tipo.nome}</Text>
                <Text className="text-sm text-so2-textMuted">{tipo.descricao ?? 'Sem descrição.'}</Text>
                <Text className="mt-1 text-xs text-so2-textMuted">Prazo: {tipo.prazoDias} dias</Text>
              </TouchableOpacity>
            ))}
          </View>
        ) : null}

        {passo === 2 && tipoSelecionado ? (
          <View className="rounded-xl border border-so2-border bg-so2-card p-4">
            <Text className="mb-4 text-lg font-semibold text-so2-text">{tipoSelecionado.nome}</Text>
            <DynamicForm
              fields={fields}
              values={values}
              errors={errors}
              onChange={(name, value) => setValues((atual) => ({ ...atual, [name]: value }))}
            />
            <View className="mt-6 flex-row justify-between">
              <TouchableOpacity onPress={() => setPasso(1)}>
                <Text className="font-medium text-so2-text">Voltar</Text>
              </TouchableOpacity>
              <TouchableOpacity className="rounded-md bg-so2-primary px-5 py-2" onPress={continuarFormulario}>
                <Text className="font-bold text-white">Continuar</Text>
              </TouchableOpacity>
            </View>
          </View>
        ) : null}

        {passo === 3 && tipoSelecionado ? (
          <View className="rounded-xl border border-so2-border bg-so2-card p-4">
            <Text className="mb-2 text-lg font-semibold text-so2-text">Revisar</Text>
            <Text className="mb-3 text-so2-text">
              <Text className="font-bold">Tipo: </Text>
              {tipoSelecionado.nome}
            </Text>
            {fields.map((field) => (
              <View key={field.name} className="mb-2">
                <Text className="text-xs text-so2-textMuted">{field.title}</Text>
                <Text className="text-so2-text">{formatar(values[field.name])}</Text>
              </View>
            ))}
            <View className="mt-6 flex-row justify-between">
              <TouchableOpacity onPress={() => setPasso(2)}>
                <Text className="font-medium text-so2-text">Voltar</Text>
              </TouchableOpacity>
              <TouchableOpacity
                className="rounded-md bg-so2-primary px-5 py-2"
                disabled={criar.isPending}
                onPress={() => criar.mutate()}
              >
                <Text className="font-bold text-white">{criar.isPending ? 'Confirmando…' : 'Confirmar'}</Text>
              </TouchableOpacity>
            </View>
          </View>
        ) : null}
      </ScrollView>
    </View>
  );
}

function formatar(valor: unknown): string {
  if (valor === undefined || valor === null || valor === '') {
    return '—';
  }
  if (typeof valor === 'boolean') {
    return valor ? 'Sim' : 'Não';
  }
  return String(valor);
}
