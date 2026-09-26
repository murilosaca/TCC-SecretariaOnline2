import { useQuery } from '@tanstack/react-query';
import { useState } from 'react';
import { ScrollView, Text, TouchableOpacity, View } from 'react-native';
import { ApiError } from '@/api/client';
import { egressosApi } from '@/api/egressos';
import { AppHeader } from '@/components/AppHeader';
import { Banner } from '@/components/Banner';
import { EmptyState, LoadingState } from '@/components/ScreenState';
import { useActions } from '@/hooks/useActions';
import { truncarHash } from '@/lib/hash';
import type { EgressoCertificado, EgressoColacao, EgressoDiploma, EgressoPainel } from '@/models/egresso';

export default function EgressoInicioScreen() {
  const painel = useQuery({
    queryKey: ['egressos', 'me'],
    queryFn: () => egressosApi.painel(),
  });
  const forbidden = painel.isError && painel.error instanceof ApiError && painel.error.status === 403;

  return (
    <View className="flex-1 bg-so2-background">
      <AppHeader title="Egresso" />
      <ScrollView contentContainerStyle={{ padding: 24, paddingBottom: 48 }}>
        {painel.isLoading ? <LoadingState label="Carregando painel…" /> : null}

        {painel.data ? (
          <View className="mb-4">
            <Text className="text-3xl font-extrabold text-so2-text">Olá, {painel.data.nome}</Text>
            <Text className="mt-1 text-sm text-so2-textMuted">
              {painel.data.curso ?? 'Curso indisponível'}
              {' · '}
              {painel.data.concluidoEm
                ? new Date(painel.data.concluidoEm).toLocaleDateString('pt-BR')
                : 'Data de conclusão indisponível'}
            </Text>
            <Text className="mt-2 font-medium text-so2-primary">Concluído</Text>
            <Text className="mt-1 text-sm text-so2-textMuted">Painel somente leitura.</Text>
          </View>
        ) : !painel.isLoading ? (
          <View className="mb-4">
            <Text className="text-3xl font-extrabold text-so2-text">Início</Text>
            <Text className="mt-1 text-sm text-so2-textMuted">Portal do egresso · SEPT/UFPR</Text>
          </View>
        ) : null}

        {forbidden ? (
          <EmptyState label="Você não tem permissão para acessar este recurso." />
        ) : null}
        {painel.isError && !forbidden ? (
          <Banner
            message="Não foi possível carregar o painel."
            actionLabel="Tentar de novo"
            onAction={() => void painel.refetch()}
          />
        ) : null}

        {painel.data ? (
          <>
            <KpiRow painel={painel.data} />
            <DiplomaBloco diploma={painel.data.diploma} />
            <CertificadosBloco itens={painel.data.certificados} />
            <ColacaoBloco colacao={painel.data.colacao} />
          </>
        ) : null}
      </ScrollView>
    </View>
  );
}

function KpiRow({ painel }: { painel: EgressoPainel }) {
  const horas = painel.kpis.horasFormativasValidadas;
  const certificados = painel.kpis.certificadosEmitidos;
  const diploma = painel.kpis.situacaoDiploma;
  return (
    <View className="mb-4 gap-y-3">
      <View className="rounded-xl border border-so2-border bg-so2-card p-4">
        <Text className="mb-1 text-so2-textMuted">Horas formativas</Text>
        {horas === null || horas === undefined ? (
          <Text className="text-so2-textMuted">Indisponível neste momento.</Text>
        ) : (
          <Text className="text-2xl font-bold text-so2-text">{horas} h</Text>
        )}
      </View>
      <View className="flex-row gap-3">
        <View className="flex-1 rounded-xl border border-so2-border bg-so2-card p-4">
          <Text className="mb-1 text-so2-textMuted">Certificados</Text>
          {certificados === null || certificados === undefined ? (
            <Text className="text-so2-textMuted">Indisponível neste momento.</Text>
          ) : (
            <Text className="text-2xl font-bold text-so2-text">{certificados}</Text>
          )}
        </View>
        <View className="flex-1 rounded-xl border border-so2-border bg-so2-card p-4">
          <Text className="mb-1 text-so2-textMuted">Diploma</Text>
          {diploma === 'EMITIDO' ? (
            <Text className="text-lg font-bold text-so2-primary">Emitido</Text>
          ) : (
            <Text className="text-so2-textMuted">Indisponível</Text>
          )}
        </View>
      </View>
    </View>
  );
}

function DiplomaBloco({ diploma }: { diploma: EgressoDiploma | null }) {
  const actions = useActions(diploma?._links);
  const [erro, setErro] = useState<string | null>(null);
  const [baixando, setBaixando] = useState(false);

  async function baixar() {
    const href = actions.href('download');
    if (!href || !diploma) {
      return;
    }
    setErro(null);
    setBaixando(true);
    try {
      await egressosApi.baixar(href);
    } catch {
      setErro('Não foi possível baixar o diploma.');
    } finally {
      setBaixando(false);
    }
  }

  return (
    <View className="mb-4 rounded-xl border border-so2-border bg-so2-card p-4" accessibilityLabel="Diploma">
      <Text className="mb-2 text-lg font-semibold text-so2-text">Diploma</Text>
      {!diploma ? <EmptyState label="O registro do diploma ainda não está disponível." /> : null}
      {diploma ? (
        <>
          <Text className="text-so2-text">Número: {diploma.numero}</Text>
          <Text className="mt-1 text-so2-text">
            Emissão: {new Date(diploma.emitidoEm).toLocaleDateString('pt-BR')}
          </Text>
          <Text className="mt-2 font-medium text-so2-primary">Emitido</Text>
          {actions.can('download') ? (
            <TouchableOpacity
              className="mt-3 items-center rounded-md bg-so2-primary py-3"
              onPress={() => void baixar()}
              disabled={baixando}
              accessibilityRole="button"
              accessibilityLabel="Baixar diploma"
            >
              <Text className="font-bold text-white">{baixando ? 'Baixando…' : 'Download'}</Text>
            </TouchableOpacity>
          ) : null}
          {erro ? (
            <Text className="mt-2 text-sm text-so2-dangerText" accessibilityRole="alert">
              {erro}
            </Text>
          ) : null}
        </>
      ) : null}
    </View>
  );
}

function CertificadosBloco({ itens }: { itens: EgressoCertificado[] }) {
  return (
    <View className="mb-4 rounded-xl border border-so2-border bg-so2-card p-4" accessibilityLabel="Certificados">
      <Text className="mb-2 text-lg font-semibold text-so2-text">Certificados</Text>
      {itens.length === 0 ? (
        <EmptyState label="Nenhum certificado emitido durante o curso." />
      ) : (
        itens.map((item) => <CertificadoLinha key={item.id} item={item} />)
      )}
    </View>
  );
}

function CertificadoLinha({ item }: { item: EgressoCertificado }) {
  const actions = useActions(item._links);
  const [baixando, setBaixando] = useState(false);
  const [erro, setErro] = useState<string | null>(null);

  async function reemitir() {
    const href = actions.href('reemitir') ?? actions.href('download');
    if (!href) {
      return;
    }
    setErro(null);
    setBaixando(true);
    try {
      await egressosApi.baixar(href);
    } catch {
      setErro('Não foi possível reemitir o PDF.');
    } finally {
      setBaixando(false);
    }
  }

  return (
    <View className="mb-3 border-b border-so2-border pb-3">
      <Text className="font-medium text-so2-text">{item.titulo}</Text>
      <Text className="mt-1 text-sm text-so2-textMuted">
        {new Date(item.emitidoEm).toLocaleDateString('pt-BR')} · {truncarHash(item.hashSha256)}
      </Text>
      {actions.can('reemitir') || actions.can('download') ? (
        <TouchableOpacity
          className="mt-2 items-center rounded-md border border-so2-border py-2"
          onPress={() => void reemitir()}
          disabled={baixando}
          accessibilityRole="button"
          accessibilityLabel={`Baixar certificado de ${item.titulo}`}
        >
          <Text className="text-so2-primary">{baixando ? 'Baixando…' : 'Reemitir PDF'}</Text>
        </TouchableOpacity>
      ) : null}
      {erro ? (
        <Text className="mt-1 text-sm text-so2-dangerText" accessibilityRole="alert">
          {erro}
        </Text>
      ) : null}
    </View>
  );
}

function ColacaoBloco({ colacao }: { colacao: EgressoColacao | null }) {
  return (
    <View className="mb-4 rounded-xl border border-so2-border bg-so2-card p-4" accessibilityLabel="Colação">
      <Text className="mb-2 text-lg font-semibold text-so2-text">Colação</Text>
      {!colacao ? (
        <EmptyState label="Dados de colação indisponíveis até o registro do diploma." />
      ) : (
        <Text className="text-so2-text">
          {colacao.data ? new Date(colacao.data).toLocaleDateString('pt-BR') : 'Data indisponível'}
          {colacao.turma ? ` · Turma ${colacao.turma}` : ''}
        </Text>
      )}
    </View>
  );
}
