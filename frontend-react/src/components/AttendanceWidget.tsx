import { FormEvent, useEffect, useMemo, useState } from 'react'
import { useActions } from '../hooks/useActions'
import type { HateoasLinks } from '../models/academico'

type Props = {
  links?: HateoasLinks
  janelaExpira?: string | null
  faseDisponivel?: string | null
  pending?: boolean
  error?: string | null
  onConfirm: (pin: string, fase: string) => void
}

export function AttendanceWidget({
  links,
  janelaExpira,
  faseDisponivel,
  pending,
  error,
  onConfirm,
}: Props) {
  const actions = useActions(links)
  const [pin, setPin] = useState('')
  const [restante, setRestante] = useState(() => msRestantes(janelaExpira))

  useEffect(() => {
    setRestante(msRestantes(janelaExpira))
    if (!janelaExpira) {
      return
    }
    const timer = window.setInterval(() => setRestante(msRestantes(janelaExpira)), 1000)
    return () => window.clearInterval(timer)
  }, [janelaExpira])

  const podeEntrada = actions.can('confirmar-entrada') && restante > 0
  const podeSaida = actions.can('confirmar-saida') && restante > 0
  const fase = useMemo(() => {
    if (podeSaida && !podeEntrada) {
      return 'SAIDA'
    }
    return faseDisponivel ?? 'ENTRADA'
  }, [faseDisponivel, podeEntrada, podeSaida])

  if (!podeEntrada && !podeSaida) {
    return null
  }

  function enviar(event: FormEvent) {
    event.preventDefault()
    onConfirm(pin.trim(), fase)
  }

  return (
    <form className="attendance-widget" onSubmit={enviar}>
      {janelaExpira && (
        <p className="countdown" aria-live="polite">
          {formatarCountdown(restante)}
        </p>
      )}
      <label>
        PIN de presença
        <input
          type="text"
          inputMode="numeric"
          autoComplete="off"
          value={pin}
          onChange={(event) => setPin(event.target.value)}
          disabled={pending}
          required
        />
      </label>
      {error && (
        <p className="field-error" role="alert">
          {error}
        </p>
      )}
      <button type="submit" disabled={pending || !pin.trim()}>
        {pending ? 'Confirmando…' : 'Confirmar'}
      </button>
    </form>
  )
}

function msRestantes(iso?: string | null): number {
  if (!iso) {
    return 0
  }
  return Math.max(0, new Date(iso).getTime() - Date.now())
}

function formatarCountdown(ms: number): string {
  const total = Math.floor(ms / 1000)
  const minutos = Math.floor(total / 60)
  const segundos = total % 60
  return `${String(minutos).padStart(2, '0')}:${String(segundos).padStart(2, '0')}`
}
