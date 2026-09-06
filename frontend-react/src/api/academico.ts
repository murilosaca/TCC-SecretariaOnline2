import type {
  Aluno,
  AlunoRequest,
  ContatoSecretaria,
  Curso,
  CursoRequest,
  Disciplina,
  DisciplinaRequest,
  PageResponse,
  PeriodoLetivo,
  PeriodoLetivoRequest,
} from '../models/academico'
import { api } from './client'

export const academicoApi = {
  listarCursos: (page = 0, size = 20) =>
    api.get<PageResponse<Curso>>('/academico/cursos', { page, size }),
  criarCurso: (body: CursoRequest) => api.post<Curso>('/academico/cursos', body),
  atualizarCurso: (id: string, body: CursoRequest) => api.put<Curso>(`/academico/cursos/${id}`, body),
  excluirCurso: (id: string) => api.delete(`/academico/cursos/${id}`),

  listarDisciplinas: (idCurso?: string, page = 0, size = 20) =>
    api.get<PageResponse<Disciplina>>('/academico/disciplinas', { idCurso, page, size }),
  criarDisciplina: (body: DisciplinaRequest) => api.post<Disciplina>('/academico/disciplinas', body),
  atualizarDisciplina: (id: string, body: DisciplinaRequest) =>
    api.put<Disciplina>(`/academico/disciplinas/${id}`, body),
  excluirDisciplina: (id: string) => api.delete(`/academico/disciplinas/${id}`),

  listarAlunos: (idCurso?: string, termo?: string, page = 0, size = 20) =>
    api.get<PageResponse<Aluno>>('/academico/alunos', { idCurso, termo, page, size }),
  criarAluno: (body: AlunoRequest) => api.post<Aluno>('/academico/alunos', body),
  atualizarAluno: (id: string, body: AlunoRequest) => api.put<Aluno>(`/academico/alunos/${id}`, body),
  excluirAluno: (id: string) => api.delete(`/academico/alunos/${id}`),

  contato: () => api.get<ContatoSecretaria>('/publico/contato'),

  listarPeriodos: (page = 0, size = 20) =>
    api.get<PageResponse<PeriodoLetivo>>('/academico/periodos', { page, size }),
  periodoVigente: () => api.get<PeriodoLetivo>('/academico/periodos/vigente'),
  criarPeriodo: (body: PeriodoLetivoRequest) => api.post<PeriodoLetivo>('/academico/periodos', body),
  atualizarPeriodo: (id: string, body: PeriodoLetivoRequest) =>
    api.put<PeriodoLetivo>(`/academico/periodos/${id}`, body),
  excluirPeriodo: (id: string) => api.delete(`/academico/periodos/${id}`),
}
