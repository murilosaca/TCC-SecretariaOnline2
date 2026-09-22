import { render, screen } from '@testing-library/react'
import { MemoryRouter } from 'react-router-dom'
import { describe, expect, it, vi } from 'vitest'
import type { HateoasLinks } from '../models/academico'
import { AppLayout } from './AppLayout'

const auth = {
  status: 'authenticated' as const,
  mustChangePassword: false,
  authorities: [] as string[],
  links: {} as HateoasLinks,
  login: vi.fn(),
  logout: vi.fn(),
  completeFirstAccess: vi.fn(),
}

vi.mock('../auth/AuthContext', () => ({
  useAuth: () => auth,
}))

function renderNav(links: HateoasLinks, mustChangePassword = false) {
  auth.links = links
  auth.mustChangePassword = mustChangePassword
  return render(
    <MemoryRouter>
      <AppLayout />
    </MemoryRouter>,
  )
}

describe('AppLayout', () => {
  it('nav do aluno não mostra Cursos, Eventos prof. nem Revisão CAAF', () => {
    renderNav({
      inicio: '/inicio',
      solicitacoes: '/solicitacoes',
      eventos: '/eventos',
      formativas: '/formativas',
      certificados: '/certificados',
      estagios: '/estagios',
      tccs: '/tccs',
      contato: '/contato',
    })
    expect(screen.getByText('Início')).toBeTruthy()
    expect(screen.getByText('Formativas')).toBeTruthy()
    expect(screen.getByText('Estágios')).toBeTruthy()
    expect(screen.getByText('TCCs')).toBeTruthy()
    expect(screen.queryByText('Revisão de TCCs')).toBeNull()
    expect(screen.queryByText('Cursos')).toBeNull()
    expect(screen.queryByText('Eventos (prof.)')).toBeNull()
    expect(screen.queryByText('Revisão CAAF')).toBeNull()
    expect(screen.queryByText('Revisão de estágios')).toBeNull()
  })

  it('nav da secretaria mostra Cursos/Alunos e não mostra Solicitações do aluno', () => {
    renderNav({
      inicio: '/inicio',
      deliberar: '/solicitacoes?to=me',
      cursos: '/secretaria/cursos',
      disciplinas: '/secretaria/disciplinas',
      alunos: '/secretaria/alunos',
      calendarios: '/secretaria/calendarios',
      contato: '/contato',
    })
    expect(screen.getByText('Cursos')).toBeTruthy()
    expect(screen.getByText('Alunos')).toBeTruthy()
    expect(screen.getByText('Deliberar')).toBeTruthy()
    expect(screen.queryByText('Solicitações')).toBeNull()
    expect(screen.queryByText('Revisão CAAF')).toBeNull()
    expect(screen.queryByText('Eventos (prof.)')).toBeNull()
    expect(screen.queryByText('Estágios')).toBeNull()
    expect(screen.queryByText('Revisão de estágios')).toBeNull()
    expect(screen.queryByText('TCCs')).toBeNull()
    expect(screen.queryByText('Revisão de TCCs')).toBeNull()
  })

  it('nav do professor mostra Deliberar e Eventos prof. e não mostra Cursos', () => {
    renderNav({
      inicio: '/inicio',
      solicitacoes: '/solicitacoes',
      deliberar: '/solicitacoes?to=me',
      'eventos-professor': '/professor/eventos',
      'estagios-revisao': '/estagios?to=me',
      'tccs-revisao': '/tccs?to=me',
      contato: '/contato',
    })
    expect(screen.getByText('Deliberar')).toBeTruthy()
    expect(screen.getByText('Eventos (prof.)')).toBeTruthy()
    expect(screen.getByText('Revisão de estágios')).toBeTruthy()
    expect(screen.getByText('Revisão de TCCs')).toBeTruthy()
    expect(screen.queryByText('TCCs')).toBeNull()
    expect(screen.queryByText('Cursos')).toBeNull()
    expect(screen.queryByText('Estágios')).toBeNull()
  })

  it('nav do egresso mostra o início do portal e não mostra rotas de aluno', () => {
    renderNav({
      'egresso-inicio': '/egresso/inicio',
      contato: '/contato',
    })
    expect(screen.getByRole('link', { name: 'Início' }).getAttribute('href')).toBe('/egresso/inicio')
    expect(screen.getByRole('link', { name: 'SO2 · SEPT/UFPR' }).getAttribute('href')).toBe(
      '/egresso/inicio',
    )
    expect(screen.getByText('Contato')).toBeTruthy()
    expect(screen.queryByText('Solicitações')).toBeNull()
    expect(screen.queryByText('Formativas')).toBeNull()
    expect(screen.queryByText('Estágios')).toBeNull()
    expect(screen.queryByText('TCCs')).toBeNull()
    expect(screen.queryByText('Eventos')).toBeNull()
  })

  it('some a nav no primeiro acesso', () => {
    renderNav({ inicio: '/inicio', cursos: '/secretaria/cursos', contato: '/contato' }, true)
    expect(screen.queryByText('Cursos')).toBeNull()
    expect(screen.queryByText('Início')).toBeNull()
  })
})
