import { useQuery } from '@tanstack/react-query'
import { academicoApi } from '../../api/academico'

export function Contato() {
  const { data, isLoading, isError } = useQuery({
    queryKey: ['contato'],
    queryFn: academicoApi.contato,
  })

  return (
    <section className="page">
      <h1>Contato da secretaria</h1>
      <p className="lead">Informações públicas, sem autenticação (RF-F0-004).</p>
      {isLoading && <p className="muted">Carregando…</p>}
      {isError && (
        <div className="banner danger" role="alert">
          Não foi possível carregar o contato da secretaria.
        </div>
      )}
      {data && (
        <article className="panel">
          <h2>{data.nome}</h2>
          <p>{data.endereco}</p>
          <p>Telefone: {data.telefone}</p>
          <p>
            E-mail: <a href={`mailto:${data.email}`}>{data.email}</a>
          </p>
          <p>Horário: {data.horario}</p>
        </article>
      )}
    </section>
  )
}
