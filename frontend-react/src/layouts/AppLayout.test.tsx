import { fireEvent, render, screen } from '@testing-library/react'
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

function renderNav(links: HateoasLinks, mustChangePassword = false, initialPath = '/') {
  auth.links = links
  auth.mustChangePassword = mustChangePassword
  return render(
    <MemoryRouter initialEntries={[initialPath]}>
      <AppLayout />
    </MemoryRouter>,
  )
}

function expandir(titulo: string) {
  const botao = screen.getByRole('button', { name: titulo })
  if (botao.getAttribute('aria-expanded') !== 'true') {
    fireEvent.click(botao)
  }
}

describe('AppLayout', () => {
  it('nav do aluno não mostra Cursos, Eventos do professor nem Revisão CAAF', () => {
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
    expandir('Acadêmico')
    expect(screen.getByText('Formativas')).toBeTruthy()
    expect(screen.getByText('Estágios')).toBeTruthy()
    expect(screen.getByText('TCCs')).toBeTruthy()
    expect(screen.queryByText('Revisão de TCCs')).toBeNull()
    expect(screen.queryByText('Cursos')).toBeNull()
    expect(screen.getByRole('link', { name: 'Eventos' }).getAttribute('href')).toBe('/eventos')
    expect(screen.queryByText('Revisão CAAF')).toBeNull()
    expect(screen.queryByText('Revisão de estágios')).toBeNull()
    expect(screen.queryByText('Cadastro de estágios')).toBeNull()
    expect(screen.queryByText('Cadastro de TCCs')).toBeNull()
    expect(screen.queryByText('Atribuição de estágios')).toBeNull()
  })

  it('nav do aluno agrupa Acadêmico, Comunicação e Configurações', () => {
    renderNav({
      inicio: '/inicio',
      solicitacoes: '/solicitacoes',
      formativas: '/formativas',
      estagios: '/estagios',
      tccs: '/tccs',
      eventos: '/eventos',
      certificados: '/certificados',
      comunicacao: '/comunicacao',
      'meus-atendimentos': '/meus-atendimentos',
      perfil: '/perfil',
      'perfil-seguranca': '/perfil/seguranca',
      'perfil-notificacoes': '/perfil/notificacoes',
      suporte: '/suporte',
      contato: '/contato',
    })
    expect(screen.getByRole('button', { name: 'Acadêmico' })).toBeTruthy()
    expect(screen.getByRole('button', { name: 'Comunicação' })).toBeTruthy()
    expect(screen.getByRole('button', { name: 'Configurações' })).toBeTruthy()
    expect(screen.queryByRole('button', { name: 'Trabalho' })).toBeNull()
    expect(screen.queryByRole('button', { name: 'Atendimentos' })).toBeNull()
    expect(screen.queryByRole('button', { name: 'Gestão' })).toBeNull()
    expect(screen.queryByRole('button', { name: 'Administração' })).toBeNull()

    expect(screen.queryByRole('link', { name: 'Formativas' })).toBeNull()
    expandir('Acadêmico')
    expect(screen.getByRole('link', { name: 'Formativas' })).toBeTruthy()
  })

  it('itens de cada grupo ficam em ordem alfabética pt-BR', () => {
    renderNav({
      inicio: '/inicio',
      certificados: '/certificados',
      estagios: '/estagios',
      eventos: '/eventos',
      formativas: '/formativas',
      solicitacoes: '/solicitacoes',
      tccs: '/tccs',
      deliberar: '/solicitacoes?to=me',
      'fila-solicitacoes': '/solicitacoes',
      'nova-interna': '/solicitacoes/nova',
      atrasados: '/secretaria/atrasados',
      atendimentos: '/secretaria/atendimentos',
      'autorizacoes-imagem': '/secretaria/autorizacoes-imagem',
      alunos: '/secretaria/alunos',
      cursos: '/secretaria/cursos',
      disciplinas: '/secretaria/disciplinas',
      contato: '/contato',
      perfil: '/perfil',
      suporte: '/suporte',
    })
    expandir('Acadêmico')
    const academico = [...screen.getByRole('group', { name: 'Acadêmico' }).querySelectorAll('a')].map(
      (el) => el.textContent,
    )
    expect(academico).toEqual([...academico].sort((a, b) => a!.localeCompare(b!, 'pt-BR')))

    expandir('Atendimentos')
    const atendimentos = [
      ...screen.getByRole('group', { name: 'Atendimentos' }).querySelectorAll('a'),
    ].map((el) => el.textContent)
    expect(atendimentos).toEqual([...atendimentos].sort((a, b) => a!.localeCompare(b!, 'pt-BR')))

    expandir('Gestão')
    const gestao = [...screen.getByRole('group', { name: 'Gestão' }).querySelectorAll('a')].map(
      (el) => el.textContent,
    )
    expect(gestao).toEqual([...gestao].sort((a, b) => a!.localeCompare(b!, 'pt-BR')))

    expandir('Configurações')
    const configuracoes = [
      ...screen.getByRole('group', { name: 'Configurações' }).querySelectorAll('a'),
    ].map((el) => el.textContent)
    expect(configuracoes).toEqual([...configuracoes].sort((a, b) => a!.localeCompare(b!, 'pt-BR')))
  })

  it('abre o grupo da rota ativa automaticamente', () => {
    renderNav(
      {
        inicio: '/inicio',
        solicitacoes: '/solicitacoes',
        formativas: '/formativas',
        contato: '/contato',
      },
      false,
      '/solicitacoes',
    )
    expect(screen.getByRole('button', { name: 'Acadêmico' }).getAttribute('aria-expanded')).toBe(
      'true',
    )
    expect(screen.getByRole('link', { name: 'Solicitações' })).toBeTruthy()
    expect(screen.getByRole('button', { name: 'Configurações' }).getAttribute('aria-expanded')).toBe(
      'false',
    )
  })

  it('em Solicitações (?to=me) abre Atendimentos e não Gestão', () => {
    renderNav(
      {
        inicio: '/inicio',
        deliberar: '/solicitacoes?to=me',
        'fila-solicitacoes': '/solicitacoes',
        cursos: '/secretaria/cursos',
        contato: '/contato',
      },
      false,
      '/solicitacoes?to=me',
    )
    expect(screen.getByRole('button', { name: 'Atendimentos' }).getAttribute('aria-expanded')).toBe(
      'true',
    )
    expect(screen.getByRole('button', { name: 'Gestão' }).getAttribute('aria-expanded')).toBe('false')
    expect(screen.getByRole('link', { name: 'Solicitações' }).className).toContain('active')
    expect(screen.getByRole('link', { name: 'Fila de solicitações' }).className).not.toContain('active')
  })

  it('em Nova interna só ela fica ativa (não Fila nem Solicitações)', () => {
    renderNav(
      {
        inicio: '/inicio',
        deliberar: '/solicitacoes?to=me',
        'fila-solicitacoes': '/solicitacoes',
        'nova-interna': '/solicitacoes/nova',
        contato: '/contato',
      },
      false,
      '/solicitacoes/nova',
    )
    expect(screen.getByRole('link', { name: 'Nova interna' }).className).toContain('active')
    expect(screen.getByRole('link', { name: 'Fila de solicitações' }).className).not.toContain('active')
    expect(screen.getByRole('link', { name: 'Solicitações' }).className).not.toContain('active')
  })

  it('na Fila de solicitações o item Solicitações não fica ativo', () => {
    renderNav(
      {
        inicio: '/inicio',
        deliberar: '/solicitacoes?to=me',
        'fila-solicitacoes': '/solicitacoes',
        'nova-interna': '/solicitacoes/nova',
        contato: '/contato',
      },
      false,
      '/solicitacoes',
    )
    expect(screen.getByRole('link', { name: 'Fila de solicitações' }).className).toContain('active')
    expect(screen.getByRole('link', { name: 'Solicitações' }).className).not.toContain('active')
    expect(screen.getByRole('link', { name: 'Nova interna' }).className).not.toContain('active')
  })

  it('nav da secretaria mostra Cursos/Alunos e Solicitações da fila deliberativa', () => {
    renderNav({
      inicio: '/inicio',
      deliberar: '/solicitacoes?to=me',
      cursos: '/secretaria/cursos',
      disciplinas: '/secretaria/disciplinas',
      alunos: '/secretaria/alunos',
      calendarios: '/secretaria/calendarios',
      contato: '/contato',
    })
    expandir('Gestão')
    expandir('Atendimentos')
    expect(screen.getByText('Cursos')).toBeTruthy()
    expect(screen.getByText('Alunos')).toBeTruthy()
    expect(screen.getByText('Calendário acadêmico')).toBeTruthy()
    expect(screen.getByRole('link', { name: 'Solicitações' }).getAttribute('href')).toBe(
      '/solicitacoes?to=me',
    )
    expect(screen.queryByText('Revisão CAAF')).toBeNull()
    expect(screen.queryByText('Atendimentos presenciais')).toBeNull()
    expect(screen.queryByText('Egressos')).toBeNull()
    expect(screen.queryByText('Estágios')).toBeNull()
    expect(screen.queryByText('Cadastro de estágios')).toBeNull()
    expect(screen.queryByText('Cadastro de TCCs')).toBeNull()
    expect(screen.queryByText('Revisão de estágios')).toBeNull()
    expect(screen.queryByText('Atribuição de estágios')).toBeNull()
    expect(screen.queryByText('TCCs')).toBeNull()
    expect(screen.queryByText('Revisão de TCCs')).toBeNull()
  })

  it('nav do membro CAAF mostra Pool CAAF e Revisão CAAF', () => {
    renderNav({
      inicio: '/inicio',
      'revisao-caaf': '/formativas?to=me',
      'comissoes-caaf': '/comissoes/caaf',
      contato: '/contato',
    })
    expandir('Trabalho')
    expect(screen.getByText('Pool CAAF')).toBeTruthy()
    expect(screen.getByText('Revisão CAAF')).toBeTruthy()
    expect(screen.queryByText('Atribuição de estágios')).toBeNull()
  })

  it('nav do professor mostra Solicitações e Eventos e não mostra Cursos', () => {
    renderNav({
      inicio: '/inicio',
      deliberar: '/solicitacoes?to=me',
      'eventos-professor': '/professor/eventos',
      'estagios-revisao': '/estagios?to=me',
      'comissoes-coe': '/comissoes/coe',
      'tccs-revisao': '/tccs?to=me',
      contato: '/contato',
    })
    expandir('Atendimentos')
    expandir('Trabalho')
    expect(screen.getByRole('link', { name: 'Solicitações' }).getAttribute('href')).toBe(
      '/solicitacoes?to=me',
    )
    expect(screen.getByRole('link', { name: 'Eventos' }).getAttribute('href')).toBe('/professor/eventos')
    expect(screen.getByText('Revisão de estágios')).toBeTruthy()
    expect(screen.getByText('Atribuição de estágios')).toBeTruthy()
    expect(screen.getByText('Revisão de TCCs')).toBeTruthy()
    expect(screen.queryByText('TCCs')).toBeNull()
    expect(screen.queryByText('Cursos')).toBeNull()
    expect(screen.queryByText('Estágios')).toBeNull()
    expect(screen.queryByText('Cadastro de estágios')).toBeNull()
    expect(screen.queryByText('Cadastro de TCCs')).toBeNull()
    expect(screen.queryByText('Configurar curso')).toBeNull()
  })

  it('mostra cadastro de estágios só quando o rel da secretaria existe', () => {
    renderNav({
      inicio: '/inicio',
      'estagios-secretaria': '/secretaria/estagios',
      contato: '/contato',
    })
    expandir('Gestão')
    expect(screen.getByRole('link', { name: 'Cadastro de estágios' }).getAttribute('href')).toBe(
      '/secretaria/estagios',
    )
    expect(screen.queryByText('Estágios')).toBeNull()
    expect(screen.queryByText('Revisão de estágios')).toBeNull()
    expect(screen.queryByText('Atribuição de estágios')).toBeNull()
    expect(screen.queryByText('Cadastro de TCCs')).toBeNull()
  })

  it('secretaria vê eventos próprios e não o item /professor/eventos', () => {
    renderNav({
      inicio: '/inicio',
      'eventos-secretaria': '/secretaria/eventos',
      atendimentos: '/secretaria/atendimentos',
      egressos: '/secretaria/egressos',
      contato: '/contato',
    })
    expandir('Gestão')
    expandir('Atendimentos')
    expect(screen.getByRole('link', { name: 'Eventos' }).getAttribute('href')).toBe('/secretaria/eventos')
    expect(screen.getByRole('link', { name: 'Atendimentos presenciais' }).getAttribute('href')).toBe(
      '/secretaria/atendimentos',
    )
    expect(screen.getByRole('link', { name: 'Egressos' }).getAttribute('href')).toBe('/secretaria/egressos')
    expect(screen.queryByRole('link', { name: 'Eventos' })).toBeTruthy()
    expect(
      screen.getAllByRole('link', { name: 'Eventos' }).every((link) => link.getAttribute('href') !== '/professor/eventos'),
    ).toBe(true)
  })

  it('aluno vê meus atendimentos só com o rel', () => {
    renderNav({
      inicio: '/inicio',
      'meus-atendimentos': '/meus-atendimentos',
      contato: '/contato',
    })
    expandir('Comunicação')
    expect(screen.getByRole('link', { name: 'Meus atendimentos' }).getAttribute('href')).toBe(
      '/meus-atendimentos',
    )
    expect(screen.queryByText('Atendimentos presenciais')).toBeNull()
  })

  it('mostra diplomas só quando o rel da secretaria existe', () => {
    renderNav({
      inicio: '/inicio',
      diplomas: '/secretaria/diplomas',
      contato: '/contato',
    })
    expandir('Gestão')
    expect(screen.getByRole('link', { name: 'Diplomas' }).getAttribute('href')).toBe(
      '/secretaria/diplomas',
    )
  })

  it('mostra cadastro de TCCs só quando o rel da secretaria existe', () => {
    renderNav({
      inicio: '/inicio',
      'tccs-secretaria': '/secretaria/tccs',
      contato: '/contato',
    })
    expandir('Gestão')
    expect(screen.getByRole('link', { name: 'Cadastro de TCCs' }).getAttribute('href')).toBe(
      '/secretaria/tccs',
    )
    expect(screen.queryByText('TCCs')).toBeNull()
    expect(screen.queryByText('Revisão de TCCs')).toBeNull()
    expect(screen.queryByText('Cadastro de estágios')).toBeNull()
  })

  it('nav do coordenador mostra Configurar curso, Relatórios e não mostra Cursos da secretaria', () => {
    renderNav({
      inicio: '/inicio',
      'configurar-curso': '/coordenacao/cursos/tads/configurar',
      relatorios: '/coordenacao/relatorios',
      contato: '/contato',
    })
    expandir('Coordenação')
    expect(screen.getByText('Configurar curso')).toBeTruthy()
    expect(screen.getByRole('link', { name: 'Relatórios' }).getAttribute('href')).toBe(
      '/coordenacao/relatorios',
    )
    expect(screen.queryByText('Cursos')).toBeNull()
    expect(screen.queryByText('Estatísticas')).toBeNull()
  })

  it('nav da secretaria mostra Estatísticas quando o rel existe', () => {
    renderNav({
      inicio: '/inicio',
      cursos: '/secretaria/cursos',
      estatisticas: '/secretaria/estatisticas',
      'fila-solicitacoes': '/solicitacoes',
      atrasados: '/secretaria/atrasados',
      contato: '/contato',
    })
    expandir('Gestão')
    expandir('Atendimentos')
    expect(screen.getByRole('link', { name: 'Estatísticas' }).getAttribute('href')).toBe(
      '/secretaria/estatisticas',
    )
    expect(screen.getByRole('link', { name: 'Fila de solicitações' }).getAttribute('href')).toBe(
      '/solicitacoes',
    )
    expect(screen.getByRole('link', { name: 'Solicitações atrasadas' }).getAttribute('href')).toBe(
      '/secretaria/atrasados',
    )
    expect(screen.queryByText('Relatórios')).toBeNull()
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
    expandir('Configurações')
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
