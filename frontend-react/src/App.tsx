import { Navigate, Route, Routes } from 'react-router-dom'
import { AppLayout } from './layouts/AppLayout'
import { AuthLayout } from './layouts/AuthLayout'
import { PrimeiroAcesso } from './pages/aluno/PrimeiroAcesso'
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
        <Route path="login" element={<Login />} />
        <Route path="recuperar-senha" element={<RecuperarSenha />} />
        <Route path="nova-senha" element={<NovaSenha />} />
        <Route path="contato" element={<Contato />} />
        <Route path="erro/:codigo" element={<Erro />} />
        <Route path="publico/verificar-protocolo/:id" element={<VerificarProtocolo />} />
        <Route path="publico/verificar-certificado/:hash" element={<VerificarCertificado />} />
      </Route>
      <Route element={<AppLayout />}>
        <Route path="inicio" element={<Inicio />} />
        <Route path="primeiro-acesso" element={<PrimeiroAcesso />} />
        <Route path="secretaria/cursos" element={<Cursos />} />
        <Route path="secretaria/disciplinas" element={<Disciplinas />} />
        <Route path="secretaria/alunos" element={<Alunos />} />
        <Route path="secretaria/calendarios" element={<Calendarios />} />
      </Route>
      <Route path="/" element={<Navigate to="/inicio" replace />} />
      <Route path="cursos" element={<Navigate to="/secretaria/cursos" replace />} />
      <Route path="disciplinas" element={<Navigate to="/secretaria/disciplinas" replace />} />
      <Route path="alunos" element={<Navigate to="/secretaria/alunos" replace />} />
      <Route path="*" element={<Navigate to="/erro/404" replace />} />
    </Routes>
  )
}
