import { FormEvent, useEffect, useMemo, useState } from 'react'
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { ApiError } from '../../api/client'
import { perfilApi } from '../../api/perfil'
import { useActions } from '../../hooks/useActions'
import {
  PRIORIDADES,
  type CanalPreferencia,
  type NotificacaoPreferencia,
  type PrioridadeNotificacao,
} from '../../models/perfil'
import { PerfilNav } from './PerfilNav'

type Formulario = {
  canais: Record<PrioridadeNotificacao, CanalPreferencia>
  dndInicio: string
  dndFim: string
  digest: 'IMEDIATO' | 'RESUMO'
}

function dePreferencia(pref: NotificacaoPreferencia): Formulario {
  return {
    canais: {
      CRITICAL: { ...pref.canais.CRITICAL },
      HIGH: { ...pref.canais.HIGH },
      MEDIUM: { ...pref.canais.MEDIUM },
      LOW: { ...pref.canais.LOW },
    },
    dndInicio: pref.dndInicio ?? '',
    dndFim: pref.dndFim ?? '',
    digest: pref.digest,
  }
}

function igual(form: Formulario, base: Formulario): boolean {
  return JSON.stringify(form) === JSON.stringify(base)
}

export function Notificacoes() {
  const queryClient = useQueryClient()
  const consulta = useQuery({ queryKey: ['perfil', 'notificacoes'], queryFn: perfilApi.notificacoes })
  const actions = useActions(consulta.data?._links)
  const [form, setForm] = useState<Formulario | null>(null)
  const [base, setBase] = useState<Formulario | null>(null)
  const [toast, setToast] = useState<string | null>(null)
  const [erro, setErro] = useState<string | null>(null)

  useEffect(() => {
    if (!consulta.data) {
      return
    }
    const atual = dePreferencia(consulta.data)
    setForm(atual)
    setBase(atual)
  }, [consulta.data])

  const dirty = useMemo(() => (form && base ? !igual(form, base) : false), [form, base])

  const salvar = useMutation({
    mutationFn: () => {
      const href = actions.href('update')
      if (!href || !form) {
        throw new Error('Sem _links.update')
      }
      return perfilApi.salvarNotificacoes(href, {
        canais: form.canais,
        dndInicio: form.dndInicio || null,
        dndFim: form.dndFim || null,
        digest: form.digest,
      })
    },
    onSuccess: (pref) => {
      queryClient.setQueryData(['perfil', 'notificacoes'], pref)
      setToast('Preferências de notificação salvas.')
      setErro(null)
    },
    onError: (error) => {
      setToast(null)
      setErro(error instanceof ApiError ? error.message : 'Não foi possível salvar as preferências.')
    },
  })

  function alternar(prioridade: PrioridadeNotificacao, canal: 'email' | 'inApp') {
    if (!form || prioridade === 'CRITICAL') {
      return
    }
    setForm({
      ...form,
      canais: {
        ...form.canais,
        [prioridade]: {
          ...form.canais[prioridade],
          [canal]: !form.canais[prioridade][canal],
        },
      },
    })
  }

  function onSubmit(event: FormEvent) {
    event.preventDefault()
    if (!dirty || !actions.can('update') || salvar.isPending) {
      return
    }
    salvar.mutate()
  }

  if (consulta.isError) {
    return (
      <section className="page">
        <h1>Notificações</h1>
        <PerfilNav />
        <div className="banner danger" role="alert">
          {consulta.error instanceof ApiError ? consulta.error.message : 'Não foi possível carregar as preferências.'}
        </div>
      </section>
    )
  }

  if (consulta.isLoading || !form || !base) {
    return (
      <section className="page" aria-busy="true">
        <h1>Notificações</h1>
        <p className="muted">Carregando preferências…</p>
      </section>
    )
  }

  return (
    <section className="page">
      <h1>Notificações</h1>
      <p className="lead">Canais por prioridade, horário de não perturbe e modo de entrega. Sem push.</p>
      <PerfilNav />
      {toast && (
        <div className="banner success" role="status">
          {toast}
        </div>
      )}
      {erro && (
        <div className="banner danger" role="alert">
          {erro}
        </div>
      )}
      <form className="settings-stack" onSubmit={onSubmit}>
        <section className="panel">
          <h2>Canais</h2>
          <table>
            <thead>
              <tr>
                <th>Prioridade</th>
                <th>E-mail</th>
                <th>No aplicativo</th>
              </tr>
            </thead>
            <tbody>
              {PRIORIDADES.map((prioridade) => (
                <tr key={prioridade.id}>
                  <th scope="row">{prioridade.label}</th>
                  <td>
                    <CanalSwitch
                      rotulo={`E-mail para prioridade ${prioridade.id}`}
                      ligado={form.canais[prioridade.id].email}
                      bloqueado={prioridade.id === 'CRITICAL'}
                      onToggle={() => alternar(prioridade.id, 'email')}
                    />
                  </td>
                  <td>
                    <CanalSwitch
                      rotulo={`No aplicativo para prioridade ${prioridade.id}`}
                      ligado={form.canais[prioridade.id].inApp}
                      bloqueado={prioridade.id === 'CRITICAL'}
                      onToggle={() => alternar(prioridade.id, 'inApp')}
                    />
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </section>
        <section className="panel">
          <h2>Não perturbe</h2>
          <div className="grid">
            <label>
              Início
              <input
                type="time"
                value={form.dndInicio}
                onChange={(event) => setForm({ ...form, dndInicio: event.target.value })}
              />
            </label>
            <label>
              Fim
              <input
                type="time"
                value={form.dndFim}
                onChange={(event) => setForm({ ...form, dndFim: event.target.value })}
              />
            </label>
          </div>
        </section>
        <section className="panel">
          <h2>Entrega</h2>
          <label className="checkbox-row">
            <input
              type="radio"
              name="digest"
              checked={form.digest === 'IMEDIATO'}
              onChange={() => setForm({ ...form, digest: 'IMEDIATO' })}
            />
            Imediato
          </label>
          <label className="checkbox-row">
            <input
              type="radio"
              name="digest"
              checked={form.digest === 'RESUMO'}
              onChange={() => setForm({ ...form, digest: 'RESUMO' })}
            />
            Resumo
          </label>
        </section>
        {actions.can('update') && (
          <div className="form-footer">
            <button
              type="button"
              className="ghost"
              onClick={() => {
                setForm(base)
                setToast(null)
                setErro(null)
              }}
            >
              Cancelar
            </button>
            <button type="submit" disabled={!dirty || salvar.isPending}>
              {salvar.isPending ? 'Salvando…' : 'Salvar preferências'}
            </button>
          </div>
        )}
      </form>
    </section>
  )
}

function CanalSwitch({
  rotulo,
  ligado,
  bloqueado,
  onToggle,
}: {
  rotulo: string
  ligado: boolean
  bloqueado: boolean
  onToggle: () => void
}) {
  return (
    <input
      type="checkbox"
      role="switch"
      aria-label={rotulo}
      checked={ligado}
      disabled={bloqueado}
      onChange={onToggle}
    />
  )
}
