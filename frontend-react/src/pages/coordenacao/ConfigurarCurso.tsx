import { FormEvent, useEffect, useMemo, useState } from 'react'
import { useParams } from 'react-router-dom'
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { ApiError } from '../../api/client'
import { coordenacaoApi } from '../../api/coordenacao'
import { useActions } from '../../hooks/useActions'
import type { ConfigCurso } from '../../models/coordenacao'

type Formulario = {
  horasFormativasMinimas: string
  duracaoCalendario: string
  bancaMembrosExternos: string
  bancaModalidade: ConfigCurso['bancaModalidade']
  regimento: string
}

const VAZIO: Formulario = {
  horasFormativasMinimas: '',
  duracaoCalendario: '15',
  bancaMembrosExternos: '1',
  bancaModalidade: 'PRESENCIAL',
  regimento: '',
}

function deConfig(config: ConfigCurso): Formulario {
  return {
    horasFormativasMinimas: String(config.horasFormativasMinimas),
    duracaoCalendario: String(config.duracaoCalendario),
    bancaMembrosExternos: String(config.bancaMembrosExternos),
    bancaModalidade: config.bancaModalidade,
    regimento: config.regimento,
  }
}

function horasValidas(valor: string): boolean {
  if (!/^\d+$/.test(valor.trim())) {
    return false
  }
  const n = Number(valor)
  return n >= 0 && n <= 1000
}

export function ConfigurarCurso() {
  const { id = '' } = useParams()
  const queryClient = useQueryClient()
  const [form, setForm] = useState<Formulario>(VAZIO)
  const [base, setBase] = useState<Formulario>(VAZIO)
  const [confirmarSaida, setConfirmarSaida] = useState(false)
  const [toast, setToast] = useState<string | null>(null)

  const consulta = useQuery({
    queryKey: ['coordenacao', 'config', id],
    queryFn: () => coordenacaoApi.obterConfig(id),
    enabled: Boolean(id),
  })
  const actions = useActions(consulta.data?._links)
  const podeSalvar = actions.can('update')
  const hrefUpdate = actions.href('update')
  const forbidden = consulta.isError && consulta.error instanceof ApiError && consulta.error.status === 403

  useEffect(() => {
    if (!consulta.data) {
      return
    }
    const atual = deConfig(consulta.data)
    setForm(atual)
    setBase(atual)
    setConfirmarSaida(false)
  }, [consulta.data])

  const dirty = useMemo(
    () =>
      form.horasFormativasMinimas !== base.horasFormativasMinimas ||
      form.duracaoCalendario !== base.duracaoCalendario ||
      form.bancaMembrosExternos !== base.bancaMembrosExternos ||
      form.bancaModalidade !== base.bancaModalidade ||
      form.regimento !== base.regimento,
    [form, base],
  )
  const horasOk = horasValidas(form.horasFormativasMinimas)

  const salvar = useMutation({
    mutationFn: () => {
      if (!hrefUpdate) {
        throw new Error('Sem _links.update')
      }
      return coordenacaoApi.atualizarConfig(hrefUpdate, {
        horasFormativasMinimas: Number(form.horasFormativasMinimas),
        duracaoCalendario: Number(form.duracaoCalendario),
        bancaMembrosExternos: Number(form.bancaMembrosExternos),
        bancaModalidade: form.bancaModalidade,
        regimento: form.regimento,
      })
    },
    onSuccess: (config) => {
      queryClient.setQueryData(['coordenacao', 'config', id], config)
      setToast('Configuração salva')
    },
  })

  function onSubmit(event: FormEvent) {
    event.preventDefault()
    if (!podeSalvar || !dirty || !horasOk) {
      return
    }
    salvar.mutate()
  }

  function pedirCancelar() {
    if (!dirty) {
      return
    }
    setConfirmarSaida(true)
  }

  function confirmarDescarte() {
    setForm(base)
    setConfirmarSaida(false)
  }

  return (
    <section className="page">
      <header className="page-head">
        <h1>Configurar curso</h1>
        <p className="muted">
          {consulta.data ? `${consulta.data.nome} · ${consulta.data.sigla}` : 'Parâmetros da coordenação (F6.1).'}
        </p>
      </header>

      {consulta.isLoading && (
        <p className="muted" aria-busy="true">
          Carregando configuração…
        </p>
      )}

      {forbidden && (
        <div className="banner danger" role="alert">
          Você não é coordenador deste curso
        </div>
      )}
      {consulta.isError && !forbidden && (
        <div className="banner danger" role="alert">
          Não foi possível carregar a configuração.{' '}
          <button type="button" onClick={() => consulta.refetch()}>
            Tentar de novo
          </button>
        </div>
      )}
      {toast && (
        <div className="banner success" role="status">
          {toast}
        </div>
      )}
      {salvar.isError && (
        <div className="banner danger" role="alert">
          {salvar.error instanceof ApiError ? salvar.error.message : 'Falha ao salvar a configuração.'}
        </div>
      )}
      {confirmarSaida && (
        <div className="banner warning" role="alertdialog" aria-labelledby="descartar-titulo">
          <p id="descartar-titulo">Deseja descartar as alterações?</p>
          <div className="actions">
            <button type="button" onClick={confirmarDescarte}>
              Descartar
            </button>
            <button type="button" className="ghost" onClick={() => setConfirmarSaida(false)}>
              Continuar editando
            </button>
          </div>
        </div>
      )}

      {consulta.data && (
        <form className="form-cards" onSubmit={onSubmit}>
          <article className="card">
            <h2>Horas formativas</h2>
            <label htmlFor="horas-minimas">
              Horas formativas mínimas
              <input
                id="horas-minimas"
                type="number"
                min={0}
                max={1000}
                value={form.horasFormativasMinimas}
                disabled={!podeSalvar}
                onChange={(e) => setForm({ ...form, horasFormativasMinimas: e.target.value })}
                aria-invalid={!horasOk}
                aria-describedby={!horasOk ? 'horas-erro' : undefined}
              />
            </label>
            {!horasOk && (
              <p id="horas-erro" className="banner danger" role="alert">
                Valor deve ser entre 0 e 1000
              </p>
            )}
          </article>

          <article className="card">
            <h2>Calendário letivo</h2>
            <label htmlFor="duracao-calendario">
              Duração (semanas)
              <select
                id="duracao-calendario"
                value={form.duracaoCalendario}
                disabled={!podeSalvar}
                onChange={(e) => setForm({ ...form, duracaoCalendario: e.target.value })}
              >
                <option value="15">15 semanas</option>
                <option value="18">18 semanas</option>
              </select>
            </label>
          </article>

          <article className="card">
            <h2>Banca de TCC</h2>
            <div className="grid">
              <label htmlFor="banca-membros">
                Membros externos
                <select
                  id="banca-membros"
                  value={form.bancaMembrosExternos}
                  disabled={!podeSalvar}
                  onChange={(e) => setForm({ ...form, bancaMembrosExternos: e.target.value })}
                >
                  <option value="1">1</option>
                  <option value="2">2</option>
                </select>
              </label>
              <label htmlFor="banca-modalidade">
                Modalidade
                <select
                  id="banca-modalidade"
                  value={form.bancaModalidade}
                  disabled={!podeSalvar}
                  onChange={(e) =>
                    setForm({
                      ...form,
                      bancaModalidade: e.target.value as ConfigCurso['bancaModalidade'],
                    })
                  }
                >
                  <option value="PRESENCIAL">Presencial</option>
                  <option value="REMOTO">Remoto</option>
                  <option value="HIBRIDO">Híbrido</option>
                </select>
              </label>
            </div>
          </article>

          <article className="card">
            <h2>Regimento</h2>
            <label htmlFor="regimento">
              Texto do regimento
              <textarea
                id="regimento"
                rows={8}
                maxLength={10000}
                value={form.regimento}
                disabled={!podeSalvar}
                onChange={(e) => setForm({ ...form, regimento: e.target.value })}
              />
            </label>
          </article>

          <div className="actions">
            <button type="submit" disabled={!podeSalvar || !dirty || !horasOk || salvar.isPending}>
              Salvar
            </button>
            <button type="button" className="ghost" disabled={!dirty} onClick={pedirCancelar}>
              Cancelar
            </button>
          </div>
        </form>
      )}
    </section>
  )
}
