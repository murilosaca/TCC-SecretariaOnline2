import { Link, useParams } from 'react-router-dom'
import { useAuth } from '../../auth/AuthContext'

const MENSAGENS: Record<string, { titulo: string; mensagem: string }> = {
  '401': {
    titulo: 'Não autenticado',
    mensagem: 'Sua sessão expirou ou você ainda não entrou. Faça login para continuar.',
  },
  '403': {
    titulo: 'Acesso negado',
    mensagem: 'Você não tem permissão para esta operação.',
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
  const { status } = useAuth()
  const autenticado = status === 'authenticated'
  const cta = ctaErro(codigo, autenticado)

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

function ctaErro(codigo: string, autenticado: boolean): { to: string; label: string } {
  if (codigo === '401') {
    return { to: '/login', label: 'Fazer login' }
  }
  if (codigo === '403') {
    return autenticado
      ? { to: '/inicio', label: 'Voltar ao início' }
      : { to: '/login', label: 'Fazer login' }
  }
  return autenticado
    ? { to: '/inicio', label: 'Voltar ao início' }
    : { to: '/login', label: 'Fazer login' }
}
