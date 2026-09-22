import { Link, useParams } from 'react-router-dom'
import { useAuth } from '../../auth/AuthContext'
import { inicioDaSessao } from '../../auth/portal'

const MENSAGENS: Record<string, { titulo: string; mensagem: string }> = {
  '401': {
    titulo: 'Não autenticado',
    mensagem: 'Sua sessão expirou ou você ainda não entrou. Faça login para continuar.',
  },
  '403': {
    titulo: 'Acesso negado',
    mensagem: 'Você não tem permissão para acessar este recurso.',
  },
  '404': {
    titulo: 'Página não encontrada',
    mensagem: 'O endereço não existe ou o recurso foi removido.',
  },
  '500': {
    titulo: 'Erro interno',
    mensagem: 'Ocorreu um problema inesperado. Tente novamente ou contate o suporte.',
  },
}

export function Erro() {
  const codigo = useParams().codigo ?? '500'
  const info = MENSAGENS[codigo] ?? MENSAGENS['500']
  const { status, links } = useAuth()
  const autenticado = status === 'authenticated'
  const cta = ctaErro(codigo, autenticado, inicioDaSessao(links))

  return (
    <section className="page">
      <h1>
        {codigo} — {info.titulo}
      </h1>
      <p className="lead">{info.mensagem}</p>
      <Link to={cta.to} className="card" style={{ maxWidth: '20rem' }}>
        {cta.label}
      </Link>
    </section>
  )
}

function ctaErro(
  codigo: string,
  autenticado: boolean,
  inicio: string,
): { to: string; label: string } {
  if (codigo === '401' || !autenticado) {
    return { to: '/login', label: 'Fazer login' }
  }
  if (codigo === '403') {
    return { to: inicio, label: 'Ir ao início' }
  }
  return { to: inicio, label: 'Voltar ao início' }
}
