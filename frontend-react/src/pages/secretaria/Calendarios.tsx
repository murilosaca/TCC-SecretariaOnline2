import { FormEvent, useState } from 'react'
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { academicoApi } from '../../api/academico'
import { useActions } from '../../hooks/useActions'
import type { PeriodoLetivo } from '../../models/academico'

const anoAtual = new Date().getFullYear()
const vazio = {
  ano: anoAtual,
  semestre: 1,
  inicio: `${anoAtual}-02-01`,
  fim: `${anoAtual}-07-15`,
}

export function Calendarios() {
  const queryClient = useQueryClient()
  const [form, setForm] = useState(vazio)
  const [editandoId, setEditandoId] = useState<string | null>(null)
  const [erro, setErro] = useState<string | null>(null)

  const vigente = useQuery({
    queryKey: ['periodo-vigente'],
    queryFn: academicoApi.periodoVigente,
    retry: false,
  })
  const lista = useQuery({
    queryKey: ['periodos'],
    queryFn: () => academicoApi.listarPeriodos(),
  })

  const salvar = useMutation({
    mutationFn: () =>
      editandoId ? academicoApi.atualizarPeriodo(editandoId, form) : academicoApi.criarPeriodo(form),
    onSuccess: () => {
      setForm(vazio)
      setEditandoId(null)
      queryClient.invalidateQueries({ queryKey: ['periodos'] })
      queryClient.invalidateQueries({ queryKey: ['periodo-vigente'] })
    },
    onError: () => setErro('Falha ao salvar o período. Confira sobreposição e semestre (1 ou 2).'),
  })

  const excluir = useMutation({
    mutationFn: (id: string) => academicoApi.excluirPeriodo(id),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['periodos'] })
      queryClient.invalidateQueries({ queryKey: ['periodo-vigente'] })
    },
    onError: () => setErro('Falha ao excluir o período.'),
  })

  function onSubmit(event: FormEvent) {
    event.preventDefault()
    salvar.mutate()
  }

  function editar(periodo: PeriodoLetivo) {
    setEditandoId(periodo.id)
    setForm({
      ano: periodo.ano,
      semestre: periodo.semestre,
      inicio: periodo.inicio,
      fim: periodo.fim,
    })
  }

  return (
    <section className="page">
      <header className="page-head">
        <h1>Calendário acadêmico</h1>
        <p>Períodos letivos sem sobreposição (RF-F5-004-c / F5.9). Eventos semânticos ainda não entram nesta fundação.</p>
      </header>
      {vigente.isError && (
        <div className="banner danger" role="status">
          Não há período letivo vigente hoje. Cadastre o intervalo atual antes de seguir com trâmites que dependem do calendário.
        </div>
      )}
      {vigente.data && (
        <p className="muted">
          Vigente: {vigente.data.ano}/{vigente.data.semestre} ({vigente.data.inicio} a {vigente.data.fim})
        </p>
      )}
      {(erro || lista.isError) && (
        <div className="banner danger" role="alert">
          {erro ?? 'Não foi possível carregar os períodos.'}
        </div>
      )}
      <form className="panel" onSubmit={onSubmit}>
        <h2>{editandoId ? 'Editar período' : 'Novo período'}</h2>
        <div className="grid">
          <label>
            Ano
            <input
              type="number"
              value={form.ano}
              onChange={(e) => setForm({ ...form, ano: Number(e.target.value) })}
              required
            />
          </label>
          <label>
            Semestre
            <select
              value={form.semestre}
              onChange={(e) => setForm({ ...form, semestre: Number(e.target.value) })}
            >
              <option value={1}>1</option>
              <option value={2}>2</option>
            </select>
          </label>
          <label>
            Início
            <input
              type="date"
              value={form.inicio}
              onChange={(e) => setForm({ ...form, inicio: e.target.value })}
              required
            />
          </label>
          <label>
            Fim
            <input type="date" value={form.fim} onChange={(e) => setForm({ ...form, fim: e.target.value })} required />
          </label>
        </div>
        <button type="submit" disabled={salvar.isPending}>
          Salvar
        </button>
      </form>
      {lista.isLoading && <p className="muted">Carregando períodos…</p>}
      {lista.data && lista.data.content.length === 0 && <p className="empty">Nenhum período cadastrado.</p>}
      {lista.data && lista.data.content.length > 0 && (
        <table>
          <thead>
            <tr>
              <th>Período</th>
              <th>Início</th>
              <th>Fim</th>
              <th>Ativo</th>
              <th></th>
            </tr>
          </thead>
          <tbody>
            {lista.data.content.map((periodo) => (
              <PeriodoLinha
                key={periodo.id}
                periodo={periodo}
                onEditar={editar}
                onExcluir={() => excluir.mutate(periodo.id)}
              />
            ))}
          </tbody>
        </table>
      )}
    </section>
  )
}

function PeriodoLinha({
  periodo,
  onEditar,
  onExcluir,
}: {
  periodo: PeriodoLetivo
  onEditar: (periodo: PeriodoLetivo) => void
  onExcluir: () => void
}) {
  const actions = useActions(periodo._links)
  return (
    <tr>
      <td>
        {periodo.ano}/{periodo.semestre}
      </td>
      <td>{periodo.inicio}</td>
      <td>{periodo.fim}</td>
      <td>{periodo.ativo ? 'sim' : 'não'}</td>
      <td className="actions">
        {actions.can('atualizar') && (
          <button type="button" className="ghost" onClick={() => onEditar(periodo)}>
            Editar
          </button>
        )}
        {actions.can('excluir') && (
          <button type="button" className="danger" onClick={onExcluir}>
            Excluir
          </button>
        )}
      </td>
    </tr>
  )
}
