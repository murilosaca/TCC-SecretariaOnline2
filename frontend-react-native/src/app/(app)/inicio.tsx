import { Link, type Href } from 'expo-router';
import { useQuery } from '@tanstack/react-query';
import { ScrollView, Text, TouchableOpacity, View } from 'react-native';
import { ApiError } from '@/api/client';
import { bffApi } from '@/api/bff';
import { AppHeader } from '@/components/AppHeader';
import { Banner } from '@/components/Banner';
import { EmptyState, LoadingState } from '@/components/ScreenState';
import { useActions } from '@/hooks/useActions';
import { blocoKpi, blocoLista } from '@/lib/dashboardBlocks';
import type {
  KpisDashboard,
  PendenciaDashboard,
  ProximoEventoDashboard,
  UltimaSolicitacaoDashboard,
} from '@/models/dashboard';

export default function InicioScreen() {
  const dashboard = useQuery({
    queryKey: ['bff', 'dashboard', 'aluno'],
    queryFn: () => bffApi.dashboardAluno(),
  });
  const actions = useActions(dashboard.data?._links);
  const forbidden = dashboard.isError && dashboard.error instanceof ApiError && dashboard.error.status === 403;

  return (
    <View className="flex-1 bg-so2-background">
      <AppHeader title="Início" />
      <ScrollView contentContainerStyle={{ padding: 24, paddingBottom: 48 }}>
        {dashboard.isLoading ? <LoadingState label="Carregando dashboard…" /> : null}

        {dashboard.data ? (
          <View className="mb-4">
            <Text className="text-3xl font-extrabold text-so2-text">Olá, {dashboard.data.saudacao.nome}</Text>
            <Text className="mt-1 text-sm text-so2-textMuted">
              {[dashboard.data.saudacao.curso, dashboard.data.periodoVigente?.rotulo].filter(Boolean).join(' · ') ||
                'Portal do aluno · SEPT/UFPR'}
            </Text>
          </View>
        ) : !dashboard.isLoading ? (
          <Text className="mb-4 text-3xl font-extrabold text-so2-text">Início</Text>
        ) : null}

        {actions.can('novaSolicitacao') ? (
          <Link href="/solicitacoes/nova" asChild>
            <TouchableOpacity className="mb-4 items-center rounded-md bg-so2-primary py-3">
              <Text className="font-bold text-white">Nova solicitação</Text>
            </TouchableOpacity>
          </Link>
        ) : null}

        {forbidden ? <EmptyState label="Dashboard do aluno indisponível para esta sessão." /> : null}
        {dashboard.isError && !forbidden ? (
          <Banner
            message="Não foi possível carregar o dashboard."
            actionLabel="Tentar de novo"
            onAction={() => void dashboard.refetch()}
          />
        ) : null}

        {dashboard.data?.alertaPeriodoAusente ? (
          <Banner
            tone="warning"
            message="Não há período letivo vigente. A secretaria ainda não cadastrou o calendário atual."
          />
        ) : null}
        {dashboard.data && dashboard.data.alertaPeriodoAusente === null && !dashboard.data.periodoVigente ? (
          <Banner tone="warning" message="Não foi possível carregar o período letivo no momento." />
        ) : null}

        {dashboard.data ? <KpiRow kpis={dashboard.data.kpis} /> : null}

        {dashboard.data ? (
          <>
            <ListaPendencias titulo="Pendências" itens={dashboard.data.pendencias} onRetry={() => void dashboard.refetch()} />
            <ListaPendencias
              titulo="Formativas a confirmar"
              itens={dashboard.data.pendenciasFormativas}
              hideIfEmpty
              onRetry={() => void dashboard.refetch()}
            />
            <ListaSolicitacoes itens={dashboard.data.ultimasSolicitacoes} onRetry={() => void dashboard.refetch()} />
            <ListaEventos itens={dashboard.data.proximosEventos} onRetry={() => void dashboard.refetch()} />
          </>
        ) : null}
      </ScrollView>
    </View>
  );
}

function KpiRow({ kpis }: { kpis: KpisDashboard }) {
  const horas = kpis.horasFormativas;
  return (
    <View className="mb-4 gap-y-3">
      <View className="rounded-xl border border-so2-border bg-so2-card p-4">
        <Text className="mb-1 text-so2-textMuted">Horas formativas</Text>
        {horas ? (
          <Text className="text-2xl font-bold text-so2-text">
            {horas.validadas} / {horas.requeridas} h
          </Text>
        ) : (
          <Text className="text-so2-textMuted">Indisponível neste momento.</Text>
        )}
      </View>
      <View className="flex-row gap-3">
        <KpiNumerico label="Solicitações" valor={kpis.solicitacoesAbertas} />
        <KpiNumerico label="Eventos hoje" valor={kpis.eventosHoje} />
      </View>
      <KpiNumerico label="Certificados" valor={kpis.certificados} />
    </View>
  );
}

function KpiNumerico({ label, valor }: { label: string; valor: number | null }) {
  const estado = blocoKpi(valor);
  return (
    <View className="flex-1 rounded-xl border border-so2-border bg-so2-card p-4">
      <Text className="mb-1 text-so2-textMuted">{label}</Text>
      {estado === 'error' ? (
        <Text className="text-so2-textMuted">Indisponível neste momento.</Text>
      ) : (
        <Text className="text-2xl font-bold text-so2-text">{valor}</Text>
      )}
    </View>
  );
}

function ListaPendencias({
  titulo,
  itens,
  hideIfEmpty,
  onRetry,
}: {
  titulo: string;
  itens?: PendenciaDashboard[] | null;
  hideIfEmpty?: boolean;
  onRetry: () => void;
}) {
  if (hideIfEmpty && itens != null && itens.length === 0) {
    return null;
  }
  const estado = blocoLista(itens);
  return (
    <View className="mb-4 rounded-xl border border-so2-border bg-so2-card p-4">
      <Text className="mb-2 text-lg font-semibold text-so2-text">{titulo}</Text>
      {estado === 'error' ? (
        <Banner tone="warning" message="Não foi possível carregar." actionLabel="Tentar de novo" onAction={onRetry} />
      ) : null}
      {estado === 'empty' ? <EmptyState label="Nenhuma pendência no momento." /> : null}
      {estado === 'ok' && itens
        ? itens.map((item) => (
            <Link key={item.id} href={item.href as Href} asChild>
              <TouchableOpacity className="border-b border-so2-border py-2">
                <Text className="text-so2-text">{item.titulo}</Text>
                <Text className="text-xs text-so2-textMuted">{item.estado}</Text>
              </TouchableOpacity>
            </Link>
          ))
        : null}
    </View>
  );
}

function ListaSolicitacoes({
  itens,
  onRetry,
}: {
  itens?: UltimaSolicitacaoDashboard[] | null;
  onRetry: () => void;
}) {
  const estado = blocoLista(itens);
  return (
    <View className="mb-4 rounded-xl border border-so2-border bg-so2-card p-4">
      <Text className="mb-2 text-lg font-semibold text-so2-text">Últimas solicitações</Text>
      {estado === 'error' ? (
        <Banner tone="warning" message="Não foi possível carregar as solicitações." actionLabel="Tentar de novo" onAction={onRetry} />
      ) : null}
      {estado === 'empty' ? <EmptyState label="Você ainda não abriu solicitações." /> : null}
      {estado === 'ok' && itens
        ? itens.map((item) => (
            <Link key={item.id} href={`/solicitacoes/${item.id}`} asChild>
              <TouchableOpacity className="border-b border-so2-border py-2">
                <Text className="font-medium text-so2-text">{item.protocolo}</Text>
                <Text className="text-xs text-so2-textMuted">
                  {item.tipoNome} · {item.estado}
                </Text>
              </TouchableOpacity>
            </Link>
          ))
        : null}
    </View>
  );
}

function ListaEventos({ itens, onRetry }: { itens?: ProximoEventoDashboard[] | null; onRetry: () => void }) {
  const estado = blocoLista(itens);
  return (
    <View className="mb-4 rounded-xl border border-so2-border bg-so2-card p-4">
      <Text className="mb-2 text-lg font-semibold text-so2-text">Próximos eventos</Text>
      {estado === 'error' ? (
        <Banner tone="warning" message="Não foi possível carregar os eventos." actionLabel="Tentar de novo" onAction={onRetry} />
      ) : null}
      {estado === 'empty' ? <EmptyState label="Nenhum evento com janela aberta hoje." /> : null}
      {estado === 'ok' && itens
        ? itens.map((item) => (
            <Link key={item.id} href={item.href as Href} asChild>
              <TouchableOpacity className="border-b border-so2-border py-2">
                <Text className="font-medium text-so2-text">{item.titulo}</Text>
                <Text className="text-xs text-so2-textMuted">
                  {new Date(item.inicioEm).toLocaleString('pt-BR')}
                  {item.janelaAtiva ? ' · Janela aberta' : ''}
                </Text>
              </TouchableOpacity>
            </Link>
          ))
        : null}
    </View>
  );
}
