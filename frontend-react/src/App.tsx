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
import { NovaFormativa } from './pages/formativas/NovaFormativa'
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
import { Relatorios } from './pages/coordenacao/Relatorios'
import { Cursos } from './pages/secretaria/Cursos'
import { Disciplinas } from './pages/secretaria/Disciplinas'
import { Estatisticas } from './pages/secretaria/Estatisticas'
import { Atrasados } from './pages/secretaria/Atrasados'
import { Atendimentos } from './pages/secretaria/Atendimentos'
import { AutorizacoesImagem } from './pages/secretaria/AutorizacoesImagem'
import { Exportacoes } from './pages/secretaria/Exportacoes'
import { Importacoes } from './pages/secretaria/Importacoes'
import { Egressos } from './pages/secretaria/Egressos'
import { EventosSecretaria } from './pages/secretaria/EventosSecretaria'
import { EventoSecretariaFormulario } from './pages/secretaria/EventoSecretariaNova'
import { EventoSecretariaOperacao } from './pages/secretaria/EventoSecretariaOperacao'
import { MeusAtendimentos } from './pages/aluno/MeusAtendimentos'
import { AuditLog } from './pages/admin/AuditLog'
import { Autoridades } from './pages/admin/Autoridades'
import { Jobs } from './pages/admin/Jobs'
import { Perfis } from './pages/admin/Perfis'
import { TiposSolicitacao } from './pages/admin/TiposSolicitacao'
import { TemplatesComunicacao } from './pages/admin/TemplatesComunicacao'
import { Usuarios } from './pages/admin/Usuarios'
import { Comunicacao } from './pages/comunicacao/Comunicacao'
import { PublicarComunicado } from './pages/comunicacao/PublicarComunicado'
import { Notificacoes } from './pages/perfil/Notificacoes'
import { Perfil } from './pages/perfil/Perfil'
import { Seguranca } from './pages/perfil/Seguranca'
import { Suporte } from './pages/suporte/Suporte'

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
          <Route path="perfil/seguranca" element={<Seguranca />} />
          <Route path="perfil/notificacoes" element={<Notificacoes />} />
          <Route path="perfil" element={<Perfil />} />
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
          <Route path="formativas/nova" element={<NovaFormativa />} />
          <Route path="formativas/:id/revisar" element={<RevisarFormativa />} />
          <Route path="formativas/:id" element={<FormativaDetalhe />} />
          <Route path="formativas" element={<FormativasRota />} />
          <Route path="certificados" element={<Certificados />} />
          <Route path="meus-atendimentos" element={<MeusAtendimentos />} />
          <Route path="comunicacao/publicar" element={<PublicarComunicado />} />
          <Route path="comunicacao" element={<Comunicacao />} />
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
          <Route path="secretaria/estatisticas" element={<Estatisticas />} />
          <Route path="secretaria/atrasados" element={<Atrasados />} />
          <Route path="secretaria/atendimentos" element={<Atendimentos />} />
          <Route path="secretaria/egressos" element={<Egressos />} />
          <Route path="secretaria/eventos/nova" element={<EventoSecretariaFormulario />} />
          <Route path="secretaria/eventos/:id/operacao" element={<EventoSecretariaOperacao />} />
          <Route path="secretaria/eventos/:id" element={<EventoSecretariaFormulario />} />
          <Route path="secretaria/eventos" element={<EventosSecretaria />} />
          <Route path="secretaria/autorizacoes-imagem" element={<AutorizacoesImagem />} />
          <Route path="secretaria/importacoes" element={<Importacoes />} />
          <Route path="secretaria/exportacoes" element={<Exportacoes />} />
          <Route path="admin/usuarios" element={<Usuarios />} />
          <Route path="admin/audit-log" element={<AuditLog />} />
          <Route path="admin/jobs" element={<Jobs />} />
          <Route path="admin/perfis" element={<Perfis />} />
          <Route path="admin/autoridades" element={<Autoridades />} />
          <Route path="admin/tipos-solicitacao" element={<TiposSolicitacao />} />
          <Route path="admin/templates-comunicacao" element={<TemplatesComunicacao />} />
          <Route path="suporte" element={<Suporte />} />
          <Route path="coordenacao/cursos/:id/configurar" element={<ConfigurarCurso />} />
          <Route path="coordenacao/relatorios" element={<Relatorios />} />
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
