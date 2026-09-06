import { Link } from 'react-router-dom'

export function Inicio() {
  return (
    <section className="page">
      <h1>Secretaria Online 2</h1>
      <p className="lead">
        Portal web em React 18 + Vite. Rotas alinhadas a `docs/telas-figma.md` (RNF-POR-01).
      </p>
      <div className="cards">
        <Link to="/secretaria/cursos" className="card">
          <h2>Cursos</h2>
          <p>Cadastro institucional (RF-F5-004-a).</p>
        </Link>
        <Link to="/secretaria/disciplinas" className="card">
          <h2>Disciplinas</h2>
          <p>Vínculo por curso e código único (RF-F5-004-b).</p>
        </Link>
        <Link to="/secretaria/alunos" className="card">
          <h2>Alunos</h2>
          <p>Cadastro com GRR e situação acadêmica (RF-F5-003).</p>
        </Link>
        <Link to="/secretaria/calendarios" className="card">
          <h2>Calendários</h2>
          <p>Períodos letivos sem sobreposição (RF-F5-004-c).</p>
        </Link>
        <Link to="/contato" className="card">
          <h2>Contato</h2>
          <p>Página pública da secretaria (RF-F0-004).</p>
        </Link>
        <Link to="/login" className="card">
          <h2>Login</h2>
          <p>Tela P0 prevista. IAM ainda não implementado (RF-F0-001).</p>
        </Link>
      </div>
    </section>
  )
}
