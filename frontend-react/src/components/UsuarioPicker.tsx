import { useEffect, useState } from 'react'
import { useQuery } from '@tanstack/react-query'
import { iamApi } from '../api/admin'
import type { UsuarioOpcao } from '../models/iam'

type Props = {
  label: string
  value: string | null
  onChange: (id: string | null, opcao?: UsuarioOpcao | null) => void
  selectedLabel?: string
  multiple?: false
  required?: boolean
  disabled?: boolean
}

type MultiProps = {
  label: string
  values: string[]
  onChange: (ids: string[], labels: Record<string, string>) => void
  selectedLabels?: Record<string, string>
  multiple: true
  required?: boolean
  disabled?: boolean
}

export function UsuarioPicker(props: Props | MultiProps) {
  const [termo, setTermo] = useState('')
  const [debounced, setDebounced] = useState('')

  useEffect(() => {
    const t = window.setTimeout(() => setDebounced(termo.trim()), 300)
    return () => window.clearTimeout(t)
  }, [termo])

  const busca = useQuery({
    queryKey: ['iam-usuarios', debounced],
    queryFn: () => iamApi.buscarUsuarios(debounced || undefined, 0, 20),
    enabled: !props.disabled,
  })

  const opcoes = busca.data?.content ?? []

  if (props.multiple) {
    const selecionados = new Set(props.values)
    return (
      <fieldset className="usuario-picker" disabled={props.disabled}>
        <legend>{props.label}</legend>
        <label>
          Buscar
          <input
            value={termo}
            onChange={(e) => setTermo(e.target.value)}
            placeholder="Nome, e-mail ou GRR"
            aria-label={`Buscar ${props.label.toLowerCase()}`}
          />
        </label>
        {busca.isLoading && <p className="muted">Buscando…</p>}
        {busca.isError && (
          <p className="empty" role="alert">
            Não foi possível buscar usuários.
          </p>
        )}
        {busca.isSuccess && opcoes.length === 0 && (
          <p className="empty">Nenhum usuário encontrado.</p>
        )}
        {opcoes.length > 0 && (
          <ul className="usuario-picker-list" role="listbox" aria-multiselectable="true">
            {opcoes.map((u) => {
              const marcado = selecionados.has(u.id)
              return (
                <li key={u.id}>
                  <label>
                    <input
                      type="checkbox"
                      checked={marcado}
                      onChange={() => {
                        const labels = { ...(props.selectedLabels ?? {}) }
                        let next: string[]
                        if (marcado) {
                          next = props.values.filter((id) => id !== u.id)
                        } else {
                          next = [...props.values, u.id]
                          labels[u.id] = `${u.nome} · ${u.emailInstitucional}`
                        }
                        props.onChange(next, labels)
                      }}
                    />
                    <span>
                      {u.nome} · {u.emailInstitucional}
                      {u.grr ? ` · ${u.grr}` : ''}
                    </span>
                  </label>
                </li>
              )
            })}
          </ul>
        )}
        {props.values.length > 0 && (
          <p className="muted">
            Selecionados:{' '}
            {props.values
              .map((id) => props.selectedLabels?.[id] ?? id.slice(0, 8))
              .join(', ')}
          </p>
        )}
      </fieldset>
    )
  }

  return (
    <label className="usuario-picker">
      {props.label}
      <input
        value={termo}
        onChange={(e) => setTermo(e.target.value)}
        placeholder="Nome, e-mail ou GRR"
        disabled={props.disabled}
        aria-label={`Buscar ${props.label.toLowerCase()}`}
      />
      {props.value && (
        <p className="muted">
          Selecionado: {props.selectedLabel ?? props.value}
          {' · '}
          <button type="button" className="ghost" onClick={() => props.onChange(null, null)}>
            Limpar
          </button>
        </p>
      )}
      {busca.isLoading && <p className="muted">Buscando…</p>}
      {busca.isError && (
        <p className="empty" role="alert">
          Não foi possível buscar usuários.
        </p>
      )}
      {busca.isSuccess && opcoes.length === 0 && debounced && (
        <p className="empty">Nenhum usuário encontrado.</p>
      )}
      {opcoes.length > 0 && (
        <select
          value={props.value ?? ''}
          required={props.required}
          disabled={props.disabled}
          onChange={(e) => {
            const id = e.target.value || null
            const opcao = opcoes.find((u) => u.id === id) ?? null
            props.onChange(id, opcao)
          }}
        >
          <option value="">Selecione…</option>
          {props.value &&
            !opcoes.some((u) => u.id === props.value) &&
            props.selectedLabel && (
              <option value={props.value}>{props.selectedLabel}</option>
            )}
          {opcoes.map((u) => (
            <option key={u.id} value={u.id}>
              {u.nome} · {u.emailInstitucional}
              {u.grr ? ` · ${u.grr}` : ''}
            </option>
          ))}
        </select>
      )}
    </label>
  )
}
