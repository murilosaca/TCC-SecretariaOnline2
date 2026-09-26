import { useLocalSearchParams } from 'expo-router';
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { useState } from 'react';
import { ScrollView, Text, TextInput, TouchableOpacity, View } from 'react-native';
import { ApiError } from '@/api/client';
import { estagiosApi } from '@/api/estagios';
import { escolherPdf } from '@/api/upload';
import { AppHeader } from '@/components/AppHeader';
import { Banner } from '@/components/Banner';
import { EmptyState, LoadingState } from '@/components/ScreenState';
import { useActions } from '@/hooks/useActions';
import {
  dataBr,
  rotuloAcaoParecer,
  rotuloEstadoDocumento,
  rotuloSituacaoEstagio,
  rotuloTipoDocumento,
  vigencia,
} from '@/lib/estagio';
import type { DocumentoEstagio } from '@/models/estagio';

const PARECER_REPROVAR_MIN = 20;

export default function EstagioDetalheScreen() {
  const { id = '' } = useLocalSearchParams<{ id: string }>();
  const queryClient = useQueryClient();
  const [aba, setAba] = useState<'documentos' | 'pareceres'>('documentos');

  const detalhe = useQuery({
    queryKey: ['estagio', id],
    queryFn: () => estagiosApi.obter(id),
    enabled: Boolean(id),
  });

  const enviar = useMutation({
    mutationFn: ({ tipo, arquivo }: { tipo: string; arquivo: Awaited<ReturnType<typeof escolherPdf>> }) => {
      if (!arquivo) {
        return Promise.reject(new Error('Arquivo obrigatório'));
      }
      return estagiosApi.enviarDocumento(id, tipo, arquivo);
    },
    onSuccess: (atual) => {
      queryClient.setQueryData(['estagio', id], atual);
      void queryClient.invalidateQueries({ queryKey: ['estagios'] });
    },
  });

  const parecer = useMutation({
    mutationFn: ({
      documentoId,
      acao,
      texto,
    }: {
      documentoId: string;
      acao: 'APROVAR' | 'REPROVAR';
      texto: string;
    }) => estagiosApi.parecer(id, documentoId, acao, texto),
    onSuccess: (atual) => {
      queryClient.setQueryData(['estagio', id], atual);
      void queryClient.invalidateQueries({ queryKey: ['estagios'] });
    },
  });

  const item = detalhe.data;
  const erroAcao = enviar.error ?? parecer.error;
  const pending = enviar.isPending || parecer.isPending;

  return (
    <View className="flex-1 bg-so2-background">
      <AppHeader title="Estágio" showMenu={false} />
      <ScrollView contentContainerStyle={{ padding: 24, paddingBottom: 48 }}>
        {detalhe.isLoading ? <LoadingState label="Carregando estágio…" /> : null}
        {detalhe.isError ? (
          <Banner
            message="Estágio não encontrado ou indisponível."
            actionLabel="Tentar de novo"
            onAction={() => void detalhe.refetch()}
          />
        ) : null}

        {item ? (
          <>
            <Text className="text-2xl font-bold text-so2-text">{item.empresa}</Text>
            <Text className="mt-1 text-sm text-so2-textMuted">
              {item.alunoNome ? `${item.alunoNome} · ` : ''}
              {vigencia(item.inicio, item.fim)}
            </Text>
            <Text className="mt-2 font-medium text-so2-primary">{rotuloSituacaoEstagio(item.situacao)}</Text>

            {erroAcao ? (
              <Banner
                message={erroAcao instanceof ApiError ? erroAcao.message : 'Não foi possível concluir a ação.'}
              />
            ) : null}

            <View className="mt-4 flex-row gap-2">
              <TouchableOpacity
                className={`flex-1 items-center rounded-md border py-2 ${
                  aba === 'documentos' ? 'border-so2-primary bg-so2-primary' : 'border-so2-border bg-so2-card'
                }`}
                onPress={() => setAba('documentos')}
                accessibilityRole="tab"
                accessibilityState={{ selected: aba === 'documentos' }}
              >
                <Text className={aba === 'documentos' ? 'font-medium text-white' : 'text-so2-text'}>Documentos</Text>
              </TouchableOpacity>
              <TouchableOpacity
                className={`flex-1 items-center rounded-md border py-2 ${
                  aba === 'pareceres' ? 'border-so2-primary bg-so2-primary' : 'border-so2-border bg-so2-card'
                }`}
                onPress={() => setAba('pareceres')}
                accessibilityRole="tab"
                accessibilityState={{ selected: aba === 'pareceres' }}
              >
                <Text className={aba === 'pareceres' ? 'font-medium text-white' : 'text-so2-text'}>Pareceres</Text>
              </TouchableOpacity>
            </View>

            {aba === 'documentos'
              ? item.documentos.map((documento) => (
                  <DocumentoCard
                    key={documento.id}
                    documento={documento}
                    pending={pending}
                    onEnviar={(arquivo) => enviar.mutate({ tipo: documento.tipo, arquivo })}
                    onParecer={(acao, texto) => parecer.mutate({ documentoId: documento.id, acao, texto })}
                  />
                ))
              : null}

            {aba === 'pareceres' ? (
              item.pareceres.length === 0 ? (
                <EmptyState label="Nenhum parecer registrado." />
              ) : (
                item.pareceres.map((registro) => (
                  <View key={registro.id} className="mt-3 rounded-xl border border-so2-border bg-so2-card p-4">
                    <Text className="font-medium text-so2-primary">{rotuloAcaoParecer(registro.acao)}</Text>
                    <Text className="mt-1 text-sm text-so2-textMuted">
                      {registro.autorRotulo || 'Orientador'} · {dataBr(registro.createdAt)} ·{' '}
                      {rotuloTipoDocumento(registro.tipoDocumento)}
                    </Text>
                    <Text className="mt-2 text-so2-text">{registro.texto}</Text>
                  </View>
                ))
              )
            ) : null}

            <View className="mt-4 rounded-xl border border-so2-border bg-so2-card p-4">
              <Text className="mb-2 text-lg font-semibold text-so2-text">Metadados</Text>
              <Meta label="Empresa" valor={item.empresa} />
              <Meta label="Supervisor" valor={item.supervisor} />
              <Meta label="Vigência" valor={vigencia(item.inicio, item.fim)} />
              <Meta label="Orientador" valor={item.orientadorRotulo || '—'} />
              <Meta label="Situação" valor={rotuloSituacaoEstagio(item.situacao)} />
            </View>
          </>
        ) : null}
      </ScrollView>
    </View>
  );
}

function Meta({ label, valor }: { label: string; valor: string }) {
  return (
    <View className="mb-2">
      <Text className="text-sm text-so2-textMuted">{label}</Text>
      <Text className="text-so2-text">{valor}</Text>
    </View>
  );
}

function DocumentoCard({
  documento,
  pending,
  onEnviar,
  onParecer,
}: {
  documento: DocumentoEstagio;
  pending: boolean;
  onEnviar: (arquivo: NonNullable<Awaited<ReturnType<typeof escolherPdf>>>) => void;
  onParecer: (acao: 'APROVAR' | 'REPROVAR', texto: string) => void;
}) {
  const actions = useActions(documento._links);
  const [arquivoNome, setArquivoNome] = useState<string | null>(null);
  const [arquivo, setArquivo] = useState<NonNullable<Awaited<ReturnType<typeof escolherPdf>>> | null>(null);
  const [baixando, setBaixando] = useState(false);
  const [erroDownload, setErroDownload] = useState<string | null>(null);

  async function escolher() {
    const escolhido = await escolherPdf();
    if (escolhido) {
      setArquivo(escolhido);
      setArquivoNome(escolhido.name);
    }
  }

  async function baixar() {
    const href = actions.href('download');
    if (!href) {
      return;
    }
    setErroDownload(null);
    setBaixando(true);
    try {
      await estagiosApi.baixar(href);
    } catch {
      setErroDownload('Não foi possível baixar o PDF.');
    } finally {
      setBaixando(false);
    }
  }

  return (
    <View className="mt-3 rounded-xl border border-so2-border bg-so2-card p-4">
      <Text className="text-lg font-semibold text-so2-text">{rotuloTipoDocumento(documento.tipo)}</Text>
      <Text className="mt-1 text-sm font-medium text-so2-primary">{rotuloEstadoDocumento(documento.estado)}</Text>
      {documento.nomeArquivo ? <Text className="mt-1 text-sm text-so2-textMuted">{documento.nomeArquivo}</Text> : null}

      {actions.can('download') ? (
        <TouchableOpacity className="mt-3 items-center rounded-md border border-so2-border py-2" onPress={() => void baixar()} disabled={baixando}>
          <Text className="text-so2-primary">{baixando ? 'Baixando…' : 'Baixar PDF'}</Text>
        </TouchableOpacity>
      ) : null}
      {erroDownload ? (
        <Text className="mt-1 text-sm text-so2-dangerText" accessibilityRole="alert">
          {erroDownload}
        </Text>
      ) : null}

      {actions.can('upload') ? (
        <View className="mt-3 gap-y-2">
          <TouchableOpacity className="items-center rounded-md border border-so2-border py-2" onPress={() => void escolher()} disabled={pending}>
            <Text className="text-so2-text">{arquivoNome ?? 'Selecionar PDF'}</Text>
          </TouchableOpacity>
          <TouchableOpacity
            className="items-center rounded-md bg-so2-primary py-3"
            disabled={!arquivo || pending}
            onPress={() => arquivo && onEnviar(arquivo)}
          >
            <Text className="font-bold text-white">{pending ? 'Enviando…' : 'Enviar PDF'}</Text>
          </TouchableOpacity>
        </View>
      ) : null}

      <PainelParecerDocumento documento={documento} pending={pending} onParecer={onParecer} />
    </View>
  );
}

function PainelParecerDocumento({
  documento,
  pending,
  onParecer,
}: {
  documento: DocumentoEstagio;
  pending: boolean;
  onParecer: (acao: 'APROVAR' | 'REPROVAR', texto: string) => void;
}) {
  const actions = useActions(documento._links);
  const podeAprovar = actions.can('aprovar');
  const podeReprovar = actions.can('reprovar');
  const [texto, setTexto] = useState('');
  const [erro, setErro] = useState<string | null>(null);

  if (!podeAprovar && !podeReprovar) {
    return null;
  }

  function disparar(acao: 'APROVAR' | 'REPROVAR') {
    const limpo = texto.trim();
    if (acao === 'REPROVAR' && limpo.length < PARECER_REPROVAR_MIN) {
      setErro(`Informe o parecer para reprovação (mín. ${PARECER_REPROVAR_MIN} caracteres).`);
      return;
    }
    if (!limpo) {
      setErro('Informe o parecer.');
      return;
    }
    setErro(null);
    onParecer(acao, limpo);
  }

  return (
    <View className="mt-4 border-t border-so2-border pt-3">
      <Text className="mb-2 text-base font-semibold text-so2-text">Parecer do orientador</Text>
      <TextInput
        className="min-h-[96px] rounded-md border border-so2-border px-3 py-2 text-so2-text"
        value={texto}
        onChangeText={(value) => {
          setTexto(value);
          if (erro) {
            setErro(null);
          }
        }}
        multiline
        textAlignVertical="top"
        editable={!pending}
        accessibilityLabel="Parecer"
      />
      {erro ? (
        <Text className="mt-1 text-sm text-so2-dangerText" accessibilityRole="alert">
          {erro}
        </Text>
      ) : null}
      <View className="mt-2 flex-row gap-2">
        {podeAprovar ? (
          <TouchableOpacity
            className="flex-1 items-center rounded-md bg-so2-primary py-3"
            onPress={() => disparar('APROVAR')}
            disabled={pending}
          >
            <Text className="font-bold text-white">Aprovar</Text>
          </TouchableOpacity>
        ) : null}
        {podeReprovar ? (
          <TouchableOpacity
            className="flex-1 items-center rounded-md border border-so2-dangerText py-3"
            onPress={() => disparar('REPROVAR')}
            disabled={pending}
          >
            <Text className="font-bold text-so2-dangerText">Reprovar</Text>
          </TouchableOpacity>
        ) : null}
      </View>
    </View>
  );
}
