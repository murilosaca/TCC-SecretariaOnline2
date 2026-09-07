import { Navigate, Route, Routes } from 'react-router-dom'
import { RedirectIfAuthenticated, RequireAuth } from './auth/guards'
import { AppLayout } from './layouts/AppLayout'
import { AuthLayout } from './layouts/AuthLayout'
import { Eventos } from './pages/aluno/Eventos'
import { FormativaDetalhe } from './pages/aluno/FormativaDetalhe'
import { Formativas } from './pages/aluno/Formativas'
import { ProfessorEventoDetalhe } from './pages/professor/ProfessorEventoDetalhe'
import { ProfessorEventoNova } from './pages/professor/ProfessorEventoNova'
import { ProfessorEventoOperacao } from './pages/professor/ProfessorEventoOperacao'
import { ProfessorEventos } from './pages/professor/ProfessorEventos'
import { NovaSolicitacao } from './pages/aluno/NovaSolicitacao'
import { PresencaEvento } from './pages/aluno/PresencaEvento'
import { PrimeiroAcesso } from './pages/aluno/PrimeiroAcesso'
import { SolicitacaoDetalhe } from './pages/aluno/SolicitacaoDetalhe'
import { Solicitacoes } from './pages/aluno/Solicitacoes'
import { Inicio } from './pages/inicio/Inicio'
import { Contato } from './pages/publico/Contato'
import { Erro } from './pages/publico/Erro'
import { Login } from './pages/publico/Login'
import { NovaSenha } from './pages/publico/NovaSenha'
import { RecuperarSenha } from './pages/publico/RecuperarSenha'
import { VerificarCertificado } from './pages/publico/VerificarCertificado'
import { VerificarProtocolo } from './pages/publico/VerificarProtocolo'
import { Alunos } from './pages/secretaria/Alunos'
import { Calendarios } from './pages/secretaria/Calendarios'
import { Cursos } from './pages/secretaria/Cursos'
import { Disciplinas } from './pages/secretaria/Disciplinas'

export default function App() {
  return (
    <Routes>
      <Route element={<AuthLayout />}>
        <Route element={<RedirectIfAuthenticated />}>
          <Route path="login" element={<Login />} />
          <Route path="recuperar-senha" element={<RecuperarSenha />} />
        </Route>
        <Route path="nova-senha" element={<NovaSenha />} />
        <Route path="contato" element={<Contato />} />
        <Route path="erro/:codigo" element={<Erro />} />
        <Route path="publico/verificar-protocolo" element={<VerificarProtocolo />} />
        <Route path="publico/verificar-protocolo/:id" element={<VerificarProtocolo />} />
        <Route path="publico/verificar-certificado/:hash" element={<VerificarCertificado />} />
      </Route>
      <Route element={<RequireAuth />}>
        <Route element={<AppLayout />}>
          <Route path="inicio" element={<Inicio />} />
          <Route path="primeiro-acesso" element={<PrimeiroAcesso />} />
          <Route path="solicitacoes/nova" element={<NovaSolicitacao />} />
          <Route path="solicitacoes/:id" element={<SolicitacaoDetalhe />} />
          <Route path="solicitacoes" element={<Solicitacoes />} />
          <Route path="eventos/:id/presenca" element={<PresencaEvento />} />
          <Route path="eventos" element={<Eventos />} />
          <Route path="formativas/:id" element={<FormativaDetalhe />} />
          <Route path="formativas" element={<Formativas />} />
          <Route path="professor/eventos/nova" element={<ProfessorEventoNova />} />
          <Route path="professor/eventos/:id/operacao" element={<ProfessorEventoOperacao />} />
          <Route path="professor/eventos/:id" element={<ProfessorEventoDetalhe />} />
          <Route path="professor/eventos" element={<ProfessorEventos />} />
          <Route path="secretaria/cursos" element={<Cursos />} />
          <Route path="secretaria/disciplinas" element={<Disciplinas />} />
          <Route path="secretaria/alunos" element={<Alunos />} />
          <Route path="secretaria/calendarios" element={<Calendarios />} />
        </Route>
      </Route>
      <Route path="/" element={<Navigate to="/inicio" replace />} />
      <Route path="cursos" element={<Navigate to="/secretaria/cursos" replace />} />
      <Route path="disciplinas" element={<Navigate to="/secretaria/disciplinas" replace />} />
      <Route path="alunos" element={<Navigate to="/secretaria/alunos" replace />} />
      <Route path="*" element={<Navigate to="/erro/404" replace />} />
    </Routes>
  )
}
