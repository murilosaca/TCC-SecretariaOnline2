import { Link } from 'react-router-dom'
import { useQuery } from '@tanstack/react-query'
import { eventosApi } from '../../api/eventos'
import { useActions } from '../../hooks/useActions'
import { rotuloCodigo } from '../../lib/rotulos'
import type { Evento } from '../../models/evento'

export function Eventos() {
  const lista = useQuery({
    queryKey: ['eventos', 'me'],
    queryFn: () => eventosApi.listarMeus(),
  })

  return (
    <section className="page">
      <header className="page-head">
        <div>
          <h1>Eventos</h1>
          <p className="muted">
            Eventos formativos do seu curso: consulte datas, carga horária e situação da presença.
            O botão Presença aparece só enquanto a janela de validação estiver aberta.
          </p>
        </div>
      </header>

      {lista.isError && (
        <div className="banner danger" role="alert">
          Não foi possível carregar os eventos.{' '}
          <button type="button" onClick={() => lista.refetch()}>
            Tentar de novo
          </button>
        </div>
      )}

      {lista.isLoading && (
        <p className="muted" aria-busy="true">
          Carregando eventos…
        </p>
      )}
      {lista.data && (
        <table>
          <thead>
            <tr>
              <th scope="col">Título</th>
              <th scope="col">Período</th>
              <th scope="col">Estado</th>
              <th scope="col">CH</th>
              <th scope="col">Presença</th>
              <th scope="col">Ações</th>
            </tr>
          </thead>
          <tbody>
            {lista.data.content.length === 0 ? (
              <tr>
                <td colSpan={6} className="tabela-vazio" role="status">
                  Nenhum evento disponível no momento.
                </td>
              </tr>
            ) : (
              lista.data.content.map((item) => <Linha key={item.id} item={item} />)
            )}
          </tbody>
        </table>
      )}
    </section>
  )
}

function Linha({ item }: { item: Evento }) {
  const actions = useActions(item._links)
  const inicio = new Date(item.inicioEm).toLocaleString('pt-BR')
  const fim = new Date(item.fimEm).toLocaleString('pt-BR')
  return (
    <tr>
      <td>{item.titulo}</td>
      <td>
        {inicio} — {fim}
      </td>
      <td>
        <span className={`badge estado-${item.estado.toLowerCase()}`}>{rotuloEstado(item.estado)}</span>
        {item.janelaAtiva && <span className="badge">Janela aberta</span>}
      </td>
      <td>{item.cargaHoraria}h</td>
      <td>{rotuloSituacao(item.situacaoPresenca)}</td>
      <td className="actions">
        {actions.can('sessao') && (
          <Link to={`/eventos/${item.id}/presenca`} className="ghost-link">
            Presença
          </Link>
        )}
      </td>
    </tr>
  )
}

function rotuloEstado(estado: string): string {
  return rotuloCodigo(estado)
}

function rotuloSituacao(situacao: string): string {
  return rotuloCodigo(situacao)
}
