import { useQuery } from '@tanstack/react-query'
import { Link } from 'react-router-dom'
import { academicoApi } from '../../api/academico'

export function Contato() {
  const { data, isLoading, isError } = useQuery({
    queryKey: ['contato'],
    queryFn: academicoApi.contato,
  })

  return (
    <section className="page auth-page">
      <p>
        <Link to="/login" className="ghost-link">
          ← Voltar
        </Link>
      </p>
      <h1>Contato</h1>
      <p className="lead">Secretaria Acadêmica — SEPT/UFPR</p>
      {isLoading && <p className="muted">Carregando…</p>}
      {isError && (
        <div className="banner danger" role="alert">
          Não foi possível carregar o contato da secretaria.
        </div>
      )}
      {data && (
        <dl className="auth-contact">
          <div>
            <dt>Unidade</dt>
            <dd>{data.nome}</dd>
          </div>
          <div>
            <dt>Endereço</dt>
            <dd>{data.endereco}</dd>
          </div>
          <div>
            <dt>Telefone</dt>
            <dd>{data.telefone}</dd>
          </div>
          <div>
            <dt>E-mail</dt>
            <dd>
              <a href={`mailto:${data.email}`}>{data.email}</a>
            </dd>
          </div>
          <div>
            <dt>Horário</dt>
            <dd>{data.horario}</dd>
          </div>
        </dl>
      )}
    </section>
  )
}
