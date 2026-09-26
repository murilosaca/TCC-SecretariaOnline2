import { Navigate, Route, Routes } from 'react-router-dom'
import { useAuth } from './auth/AuthContext'
import { PortalGate, RedirectIfAuthenticated, RequireAuth } from './auth/guards'
import { inicioDaSessao } from './auth/portal'
import { AppLayout } from './layouts/AppLayout'
import { AuthLayout } from './layouts/AuthLayout'
import { Eventos } from './pages/aluno/Eventos'
import { Certificados } from './pages/aluno/Certificados'
import { FormativaDetalhe } from './pages/aluno/FormativaDetalhe'
import { EstagioDetalhe } from './pages/estagios/EstagioDetalhe'
import { EstagiosRota } from './pages/estagios/EstagiosRota'
import { TccDetalhe } from './pages/tccs/TccDetalhe'
import { TccsRota } from './pages/tccs/TccsRota'
import { FormativasRota } from './pages/formativas/FormativasRota'
import { RevisarFormativa } from './pages/formativas/RevisarFormativa'
import { ProfessorEventoDetalhe } from './pages/professor/ProfessorEventoDetalhe'
import { ProfessorEventoNova } from './pages/professor/ProfessorEventoNova'
import { ProfessorEventoOperacao } from './pages/professor/ProfessorEventoOperacao'
import { ProfessorEventos } from './pages/professor/ProfessorEventos'
import { NovaSolicitacao } from './pages/aluno/NovaSolicitacao'
import { PresencaEvento } from './pages/aluno/PresencaEvento'
import { PrimeiroAcesso } from './pages/aluno/PrimeiroAcesso'
import { SolicitacaoDetalhe } from './pages/aluno/SolicitacaoDetalhe'
import { DeliberarSolicitacao } from './pages/solicitacoes/DeliberarSolicitacao'
import { SolicitacoesRota } from './pages/solicitacoes/SolicitacoesRota'
import { EgressoInicio } from './pages/egresso/EgressoInicio'
import { Inicio } from './pages/inicio/Inicio'
import { Contato } from './pages/publico/Contato'
import { Erro } from './pages/publico/Erro'
import { Login } from './pages/publico/Login'
import { NovaSenha } from './pages/publico/NovaSenha'
import { RecuperarSenha } from './pages/publico/RecuperarSenha'
import { VerificarCertificado } from './pages/publico/VerificarCertificado'
import { VerificarProtocolo } from './pages/publico/VerificarProtocolo'
import { Alunos } from './pages/secretaria/Alunos'
import { Diplomas } from './pages/secretaria/Diplomas'
import { EstagiosSecretaria } from './pages/secretaria/EstagiosSecretaria'
import { TccsSecretaria } from './pages/secretaria/TccsSecretaria'
import { Calendarios } from './pages/secretaria/Calendarios'
import { PoolCoe } from './pages/comissoes/PoolCoe'
import { PoolCaaf } from './pages/comissoes/PoolCaaf'
import { ConfigurarCurso } from './pages/coordenacao/ConfigurarCurso'
import { Cursos } from './pages/secretaria/Cursos'
import { Disciplinas } from './pages/secretaria/Disciplinas'
import { Usuarios } from './pages/admin/Usuarios'

export default function App() {
  return (
    <Routes>
      <Route element={<AuthLayout />}>
        <Route element={<RedirectIfAuthenticated />}>
          <Route path="login" element={<Login />} />
          <Route path="recuperar-senha" element={<RecuperarSenha />} />
        </Route>
        <Route path="nova-senha" element={<NovaSenha />} />
        <Route path="redefinir-senha" element={<NovaSenha />} />
        <Route path="contato" element={<Contato />} />
        <Route path="erro/:codigo" element={<Erro />} />
        <Route path="publico/verificar-protocolo" element={<VerificarProtocolo />} />
        <Route path="publico/verificar-protocolo/:id" element={<VerificarProtocolo />} />
        <Route path="publico/verificar-certificado/:hash" element={<VerificarCertificado />} />
      </Route>
      <Route element={<RequireAuth />}>
        <Route element={<PortalGate />}>
          <Route element={<AppLayout />}>
          <Route path="inicio" element={<Inicio />} />
          <Route path="egresso/inicio" element={<EgressoInicio />} />
          <Route path="primeiro-acesso" element={<PrimeiroAcesso />} />
          <Route path="solicitacoes/nova" element={<NovaSolicitacao />} />
          <Route path="solicitacoes/:id/deliberar" element={<DeliberarSolicitacao />} />
          <Route path="solicitacoes/:id" element={<SolicitacaoDetalhe />} />
          <Route path="solicitacoes" element={<SolicitacoesRota />} />
          <Route path="eventos/:id/presenca" element={<PresencaEvento />} />
          <Route path="eventos" element={<Eventos />} />
          <Route path="estagios/:id" element={<EstagioDetalhe />} />
          <Route path="estagios" element={<EstagiosRota />} />
          <Route path="tccs/:id" element={<TccDetalhe />} />
          <Route path="tccs" element={<TccsRota />} />
          <Route path="formativas/:id/revisar" element={<RevisarFormativa />} />
          <Route path="formativas/:id" element={<FormativaDetalhe />} />
          <Route path="formativas" element={<FormativasRota />} />
          <Route path="certificados" element={<Certificados />} />
          <Route path="professor/eventos/nova" element={<ProfessorEventoNova />} />
          <Route path="professor/eventos/:id/operacao" element={<ProfessorEventoOperacao />} />
          <Route path="professor/eventos/:id" element={<ProfessorEventoDetalhe />} />
          <Route path="professor/eventos" element={<ProfessorEventos />} />
          <Route path="secretaria/cursos" element={<Cursos />} />
          <Route path="secretaria/disciplinas" element={<Disciplinas />} />
          <Route path="secretaria/alunos" element={<Alunos />} />
          <Route path="secretaria/estagios" element={<EstagiosSecretaria />} />
          <Route path="secretaria/tccs" element={<TccsSecretaria />} />
          <Route path="secretaria/diplomas" element={<Diplomas />} />
          <Route path="secretaria/calendarios" element={<Calendarios />} />
          <Route path="admin/usuarios" element={<Usuarios />} />
          <Route path="coordenacao/cursos/:id/configurar" element={<ConfigurarCurso />} />
          <Route path="comissoes/coe" element={<PoolCoe />} />
          <Route path="comissoes/caaf" element={<PoolCaaf />} />
          </Route>
        </Route>
      </Route>
      <Route path="/" element={<Raiz />} />
      <Route path="cursos" element={<Navigate to="/secretaria/cursos" replace />} />
      <Route path="disciplinas" element={<Navigate to="/secretaria/disciplinas" replace />} />
      <Route path="alunos" element={<Navigate to="/secretaria/alunos" replace />} />
      <Route path="*" element={<Navigate to="/erro/404" replace />} />
    </Routes>
  )
}

function Raiz() {
  const { status, mustChangePassword, links } = useAuth()
  if (status === 'loading') {
    return (
      <div className="page" aria-busy="true">
        <p className="muted">Carregando…</p>
      </div>
    )
  }
  if (status !== 'authenticated') {
    return <Navigate to="/login" replace />
  }
  if (mustChangePassword) {
    return <Navigate to="/primeiro-acesso" replace />
  }
  return <Navigate to={inicioDaSessao(links)} replace />
}
