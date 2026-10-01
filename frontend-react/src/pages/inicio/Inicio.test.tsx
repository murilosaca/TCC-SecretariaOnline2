import { QueryClient, QueryClientProvider } from '@tanstack/react-query'
import { fireEvent, render, screen, waitFor } from '@testing-library/react'
import { MemoryRouter } from 'react-router-dom'
import { beforeEach, describe, expect, it, vi } from 'vitest'
import { ApiError } from '../../api/client'
import type { HateoasLinks } from '../../models/academico'
import { Inicio } from './Inicio'

const dashboardAluno = vi.fn()
const dashboardProfessor = vi.fn()
const dashboardSecretaria = vi.fn()
let links: HateoasLinks = { inicio: '/inicio' }

vi.mock('../../api/bff', () => ({
  bffApi: {
    dashboardAluno: (...args: unknown[]) => dashboardAluno(...args),
    dashboardProfessor: (...args: unknown[]) => dashboardProfessor(...args),
    dashboardSecretaria: (...args: unknown[]) => dashboardSecretaria(...args),
  },
}))

vi.mock('../../auth/AuthContext', () => ({
  useAuth: () => ({
    authorities: [],
    status: 'authenticated',
    mustChangePassword: false,
    links,
    login: vi.fn(),
    logout: vi.fn(),
    completeFirstAccess: vi.fn(),
  }),
}))

function renderPage() {
  const client = new QueryClient({ defaultOptions: { queries: { retry: false } } })
  return render(
    <QueryClientProvider client={client}>
      <MemoryRouter>
        <Inicio />
      </MemoryRouter>
    </QueryClientProvider>,
  )
}

const alunoDash = {
  saudacao: { nome: 'Aluno Dev', curso: 'TADS' },
  periodoVigente: null,
  alertaPeriodoAusente: false,
  kpis: {
    horasFormativas: { validadas: 0, requeridas: 120 },
    solicitacoesAbertas: 0,
    eventosHoje: 0,
    certificados: 0,
  },
  pendencias: [],
  ultimasSolicitacoes: [],
  proximosEventos: [],
  pendenciasFormativas: [],
  _links: { self: '/bff/dashboard/aluno', novaSolicitacao: '/solicitacoes/nova' },
}

const professorDash = {
  saudacao: { nome: 'Professor Dev' },
  kpis: {
    pendentesDeliberar: 2,
    formativasRevisao: null,
    eventosHoje: 1,
    slaUrgentes: 1,
  },
  filaSolicitacoes: [
    {
      id: 's1',
      protocolo: 'PROT-2026-00001',
      tipoNome: 'Declaração simples',
      estado: 'EM_ANALISE',
      prazoEm: '2026-09-27T00:00:00Z',
      slaVencido: false,
      urgente: true,
      href: '/solicitacoes/s1',
    },
  ],
  meusEventos: [
    {
      id: 'e1',
      titulo: 'Oficina Proof of Stay',
      inicioEm: '2026-09-26T19:00:00Z',
      fimEm: '2026-09-26T21:00:00Z',
      estado: 'EM_ANDAMENTO',
      _links: {
        self: '/professor/eventos/e1',
        operar: '/professor/eventos/e1/operacao',
      },
    },
  ],
  formativasCaaf: null,
  estagiosPendentes: [],
  tccsPendentes: [],
  _links: {
    self: '/bff/dashboard/professor',
    deliberar: '/solicitacoes?to=me',
    eventos: '/professor/eventos',
    estagios: '/estagios?to=me',
    tccs: '/tccs?to=me',
  },
}

describe('Inicio', () => {
  beforeEach(() => {
    dashboardAluno.mockReset()
    dashboardProfessor.mockReset()
    dashboardSecretaria.mockReset()
    links = { inicio: '/inicio' }
  })

  it('aluno carrega o BFF do aluno', async () => {
    links = { inicio: '/inicio', painel: '/bff/dashboard/aluno' }
    dashboardAluno.mockResolvedValue(alunoDash)
    renderPage()
    await waitFor(() => {
      expect(screen.getByRole('heading', { level: 1, name: 'Olá, Aluno Dev' })).toBeTruthy()
    })
    expect(dashboardAluno).toHaveBeenCalled()
    expect(dashboardProfessor).not.toHaveBeenCalled()
    expect(dashboardSecretaria).not.toHaveBeenCalled()
    expect(screen.getByRole('link', { name: 'Nova solicitação' })).toBeTruthy()
  })

  it('professor puro carrega o BFF do professor com filas e operar', async () => {
    links = { inicio: '/inicio', painel: '/bff/dashboard/professor' }
    dashboardProfessor.mockResolvedValue(professorDash)
    renderPage()
    await waitFor(() => {
      expect(screen.getByRole('heading', { level: 1, name: 'Olá, Professor Dev' })).toBeTruthy()
    })
    expect(dashboardProfessor).toHaveBeenCalled()
    expect(dashboardAluno).not.toHaveBeenCalled()
    expect(dashboardSecretaria).not.toHaveBeenCalled()
    expect(screen.getByRole('heading', { level: 2, name: 'Fila de solicitações' })).toBeTruthy()
    expect(screen.getByRole('heading', { level: 2, name: 'Meus eventos hoje' })).toBeTruthy()
    expect(screen.getByRole('link', { name: 'Operar evento' })).toBeTruthy()
    expect(screen.queryByRole('heading', { level: 2, name: 'Formativas CAAF' })).toBeNull()
    expect(screen.getByText('1 solicitação urgente')).toBeTruthy()
  })

  it('bloco CAAF só aparece com _links.formativasCaaf', async () => {
    links = { inicio: '/inicio', painel: '/bff/dashboard/professor' }
    dashboardProfessor.mockResolvedValue({
      ...professorDash,
      kpis: { ...professorDash.kpis, formativasRevisao: 1 },
      formativasCaaf: [
        {
          id: 'f1',
          titulo: 'Oficina',
          estado: 'AGUARDANDO_CAAF',
          href: '/formativas/f1',
        },
      ],
      _links: {
        self: '/bff/dashboard/professor',
        formativasCaaf: '/formativas?to=me',
        eventos: '/professor/eventos',
      },
    })
    renderPage()
    await waitFor(() => {
      expect(screen.getByRole('heading', { level: 2, name: 'Formativas CAAF' })).toBeTruthy()
    })
    expect(screen.getByText('Oficina')).toBeTruthy()
  })

  it('sessão sem rel painel mostra empty', () => {
    links = { inicio: '/inicio', cursos: '/secretaria/cursos' }
    renderPage()
    expect(screen.getByText('Dashboard indisponível para esta sessão.')).toBeTruthy()
    expect(dashboardAluno).not.toHaveBeenCalled()
    expect(dashboardProfessor).not.toHaveBeenCalled()
    expect(dashboardSecretaria).not.toHaveBeenCalled()
  })

  it('secretaria carrega o BFF da secretaria e os atalhos só com _links', async () => {
    links = { inicio: '/inicio', painel: '/bff/dashboard/secretary' }
    dashboardSecretaria.mockResolvedValue({
      saudacao: { nome: 'Secretaria Dev' },
      periodoVigente: null,
      alertaPeriodoAusente: true,
      kpis: { abertas: 0, atrasadas: 0, concluidasHoje: 1, eventosDia: 0 },
      alertasSla: 0,
      filaPriorizada: [],
      agendaDia: [],
      _links: {
        self: '/bff/dashboard/secretary',
        cursos: '/secretaria/cursos',
        alunos: '/secretaria/alunos',
      },
    })
    renderPage()
    await waitFor(() => {
      expect(screen.getByRole('heading', { level: 1, name: 'Olá, Secretaria Dev' })).toBeTruthy()
    })
    expect(dashboardSecretaria).toHaveBeenCalled()
    expect(dashboardAluno).not.toHaveBeenCalled()
    expect(dashboardProfessor).not.toHaveBeenCalled()
    expect(screen.getByText('Nenhuma solicitação aberta nos seus cursos.')).toBeTruthy()
    expect(screen.getAllByText('0').length).toBeGreaterThanOrEqual(2)
    expect(screen.getByRole('link', { name: 'Cursos' })).toBeTruthy()
    expect(screen.getByRole('link', { name: 'Alunos' })).toBeTruthy()
    expect(screen.queryByRole('link', { name: 'Importações' })).toBeNull()
    expect(screen.getByText(/Não há período letivo vigente/)).toBeTruthy()
    fireEvent.click(screen.getByRole('button', { name: 'Atualizar' }))
    await waitFor(() => expect(dashboardSecretaria.mock.calls.length).toBeGreaterThan(1))
  })

  it('403 de escopo é estado de erro e não monta os KPIs', async () => {
    links = { inicio: '/inicio', painel: '/bff/dashboard/secretary' }
    dashboardSecretaria.mockRejectedValue(
      new ApiError(403, 'Nenhum curso vinculado à sua secretaria foi encontrado.'),
    )
    renderPage()
    await waitFor(() => {
      expect(
        screen.getByText('Nenhum curso vinculado à sua secretaria foi encontrado.'),
      ).toBeTruthy()
    })
    expect(screen.queryByText('Abertas')).toBeNull()
  })
})
