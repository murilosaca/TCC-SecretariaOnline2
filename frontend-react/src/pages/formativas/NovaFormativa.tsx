import { FormEvent, useState } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import { useMutation, useQuery } from '@tanstack/react-query'
import { ApiError } from '../../api/client'
import { formativasApi } from '../../api/formativas'

export function NovaFormativa() {
  const navigate = useNavigate()
  const [tipoId, setTipoId] = useState('')
  const [horas, setHoras] = useState('')
  const [arquivo, setArquivo] = useState<File | null>(null)
  const [erro, setErro] = useState<string | null>(null)

  const tipos = useQuery({
    queryKey: ['formativas', 'tipos'],
    queryFn: formativasApi.tipos,
  })
  const enviar = useMutation({
    mutationFn: () => {
      if (!arquivo) {
        throw new Error('Sem comprovante')
      }
      return formativasApi.submeter(tipoId, Number(horas), arquivo)
    },
    onSuccess: () => navigate('/formativas'),
    onError: (error) => {
      setErro(error instanceof ApiError ? error.message : 'Não foi possível enviar a atividade.')
    },
  })

  const horasOk = /^\d+$/.test(horas.trim()) && Number(horas) > 0
  const podeEnviar = Boolean(tipoId) && horasOk && arquivo !== null && !enviar.isPending

  function onSubmit(event: FormEvent) {
    event.preventDefault()
    if (!podeEnviar) {
      return
    }
    setErro(null)
    enviar.mutate()
  }

  return (
    <section className="page">
      <p>
        <Link to="/formativas" className="ghost-link">
          ← Formativas
        </Link>
      </p>
      <header className="page-head">
        <div>
          <h1>Nova atividade</h1>
          <p className="muted">Declare as horas e anexe o comprovante. A CAAF valida depois.</p>
        </div>
      </header>

      {tipos.isLoading && (
        <p className="muted" aria-busy="true">
          Carregando atividades…
        </p>
      )}
      {tipos.isError && (
        <div className="banner danger" role="alert">
          {tipos.error instanceof ApiError && tipos.error.status === 403
            ? 'Você não tem permissão para submeter atividades.'
            : 'Não foi possível carregar as atividades.'}{' '}
          <button type="button" onClick={() => tipos.refetch()}>
            Tentar de novo
          </button>
        </div>
      )}
      {tipos.data && tipos.data.content.length === 0 && (
        <p className="empty" role="status">
          Nenhuma atividade disponível para o seu curso.
        </p>
      )}
      {erro && (
        <div className="banner danger" role="alert">
          {erro}
        </div>
      )}

      <form className="panel" onSubmit={onSubmit}>
        <label>
          Atividade
          <select value={tipoId} onChange={(event) => setTipoId(event.target.value)}>
            <option value="">Selecione</option>
            {tipos.data?.content.map((tipo) => (
              <option key={tipo.id} value={tipo.id}>
                {tipo.nome}
              </option>
            ))}
          </select>
        </label>
        <label>
          Horas
          <input inputMode="numeric" value={horas} onChange={(event) => setHoras(event.target.value)} />
        </label>
        <label>
          Comprovante
          <input
            type="file"
            accept="application/pdf,image/jpeg,image/png"
            onChange={(event) => setArquivo(event.target.files?.[0] ?? null)}
          />
        </label>
        <button type="submit" disabled={!podeEnviar}>
          {enviar.isPending ? 'Enviando…' : 'Enviar'}
        </button>
      </form>
    </section>
  )
}
