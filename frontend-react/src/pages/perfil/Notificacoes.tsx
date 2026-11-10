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
  dndAtivo: boolean
  dndInicio: string
  dndFim: string
  digest: 'IMEDIATO' | 'RESUMO'
}

const DND_PADRAO_INICIO = '22:00'
const DND_PADRAO_FIM = '07:00'

function dePreferencia(pref: NotificacaoPreferencia): Formulario {
  const dndInicio = pref.dndInicio ?? ''
  const dndFim = pref.dndFim ?? ''
  return {
    canais: {
      CRITICAL: { ...pref.canais.CRITICAL },
      HIGH: { ...pref.canais.HIGH },
      MEDIUM: { ...pref.canais.MEDIUM },
      LOW: { ...pref.canais.LOW },
    },
    dndAtivo: Boolean(dndInicio || dndFim),
    dndInicio,
    dndFim,
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
        dndInicio: form.dndAtivo && form.dndInicio ? form.dndInicio : null,
        dndFim: form.dndAtivo && form.dndFim ? form.dndFim : null,
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

  function alternarDnd(ativo: boolean) {
    if (!form) {
      return
    }
    if (ativo) {
      setForm({
        ...form,
        dndAtivo: true,
        dndInicio: form.dndInicio || DND_PADRAO_INICIO,
        dndFim: form.dndFim || DND_PADRAO_FIM,
      })
      return
    }
    setForm({ ...form, dndAtivo: false, dndInicio: '', dndFim: '' })
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
        <div className="notificacoes-grid">
        <section className="panel">
          <h2>Canais</h2>
          <p className="muted notificacoes-ajuda">Escolha onde receber cada nível de prioridade.</p>
          <ul className="canais-lista">
            {PRIORIDADES.map((prioridade) => (
              <li key={prioridade.id} className="canais-item">
                <div className="canais-meta">
                  <span className="canais-titulo">{prioridade.label}</span>
                  <span className="muted">{prioridade.descricao}</span>
                </div>
                <div className="canais-chips" role="group" aria-label={`Canais para ${prioridade.label}`}>
                  <CanalChip
                    rotulo={`E-mail para prioridade ${prioridade.id}`}
                    texto="E-mail"
                    ligado={form.canais[prioridade.id].email}
                    bloqueado={prioridade.id === 'CRITICAL'}
                    onToggle={() => alternar(prioridade.id, 'email')}
                  />
                  <CanalChip
                    rotulo={`No aplicativo para prioridade ${prioridade.id}`}
                    texto="No app"
                    ligado={form.canais[prioridade.id].inApp}
                    bloqueado={prioridade.id === 'CRITICAL'}
                    onToggle={() => alternar(prioridade.id, 'inApp')}
                  />
                </div>
              </li>
            ))}
          </ul>
        </section>

        <section className="panel">
          <h2>Entrega</h2>
          <fieldset className="entrega-opcoes">
            <legend className="sr-only">Modo de entrega</legend>
            <label className="entrega-opcao">
              <input
                type="radio"
                name="digest"
                checked={form.digest === 'IMEDIATO'}
                onChange={() => setForm({ ...form, digest: 'IMEDIATO' })}
              />
              <span className="entrega-opcao-texto">
                <span className="entrega-opcao-titulo">Imediato</span>
                <span className="muted">Recebe cada notificação assim que ela acontece.</span>
              </span>
            </label>
            <label className="entrega-opcao">
              <input
                type="radio"
                name="digest"
                checked={form.digest === 'RESUMO'}
                onChange={() => setForm({ ...form, digest: 'RESUMO' })}
              />
              <span className="entrega-opcao-texto">
                <span className="entrega-opcao-titulo">Resumo</span>
                <span className="muted">Agrupa avisos em um resumo periódico, em vez de um a um.</span>
              </span>
            </label>
          </fieldset>

          <label className="checkbox-row notificacoes-dnd-toggle">
            <input
              type="checkbox"
              checked={form.dndAtivo}
              onChange={(event) => alternarDnd(event.target.checked)}
            />
            Usar horário de não perturbe
          </label>
          <p className="muted notificacoes-ajuda">
            {form.dndAtivo
              ? 'Nesse intervalo, notificações não críticas ficam em silêncio.'
              : 'Desmarcado: você pode ser notificado o dia todo.'}
          </p>
          {form.dndAtivo && (
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
          )}
        </section>
        </div>

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

function CanalChip({
  rotulo,
  texto,
  ligado,
  bloqueado,
  onToggle,
}: {
  rotulo: string
  texto: string
  ligado: boolean
  bloqueado: boolean
  onToggle: () => void
}) {
  return (
    <button
      type="button"
      role="switch"
      aria-label={rotulo}
      aria-checked={ligado}
      className={ligado ? 'canal-chip is-on' : 'canal-chip'}
      disabled={bloqueado}
      onClick={onToggle}
    >
      {texto}
    </button>
  )
}
