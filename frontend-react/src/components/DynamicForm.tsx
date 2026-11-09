import type { FormField } from '../lib/formSchema'

type Props = {
  fields: FormField[]
  values: Record<string, unknown>
  errors: Record<string, string>
  disabled?: boolean
  onChange: (name: string, value: unknown) => void
}

function ehTextoLongo(field: FormField): boolean {
  if (field.name === 'observacao' || field.name === 'mensagem' || field.name === 'declaracao') {
    return true
  }
  if (field.name === 'finalidade' || field.name === 'assunto') {
    return false
  }
  return (field.maxLength ?? 0) > 280
}

export function DynamicForm({ fields, values, errors, disabled, onChange }: Props) {
  if (fields.length === 0) {
    return <p className="muted">Este tipo não define campos no formulário.</p>
  }
  return (
    <div className="grid dynamic-form-stack">
      {fields.map((field) => {
        const error = errors[field.name]
        const value = values[field.name]
        const inputId = `campo-${field.name}`
        return (
          <label key={field.name} htmlFor={inputId}>
            {field.title}
            {field.required ? ' *' : ''}
            {field.enumValues ? (
              <select
                id={inputId}
                disabled={disabled}
                className={error ? 'invalid' : undefined}
                value={value == null ? '' : String(value)}
                onChange={(event) => onChange(field.name, event.target.value)}
              >
                <option value="">Selecione</option>
                {field.enumValues.map((option) => (
                  <option key={String(option)} value={String(option)}>
                    {String(option)}
                  </option>
                ))}
              </select>
            ) : field.type === 'boolean' ? (
              <input
                id={inputId}
                type="checkbox"
                disabled={disabled}
                checked={Boolean(value)}
                onChange={(event) => onChange(field.name, event.target.checked)}
              />
            ) : field.type === 'number' || field.type === 'integer' ? (
              <input
                id={inputId}
                type="number"
                disabled={disabled}
                className={error ? 'invalid' : undefined}
                value={value == null ? '' : String(value)}
                onChange={(event) =>
                  onChange(field.name, event.target.value === '' ? '' : Number(event.target.value))
                }
              />
            ) : field.format === 'date' ? (
              <input
                id={inputId}
                type="date"
                disabled={disabled}
                className={error ? 'invalid' : undefined}
                value={value == null ? '' : String(value)}
                onChange={(event) => onChange(field.name, event.target.value)}
              />
            ) : ehTextoLongo(field) ? (
              <textarea
                id={inputId}
                disabled={disabled}
                className={error ? 'invalid' : undefined}
                maxLength={field.maxLength}
                rows={field.name === 'observacao' || field.name === 'mensagem' ? 6 : 4}
                value={value == null ? '' : String(value)}
                onChange={(event) => onChange(field.name, event.target.value)}
              />
            ) : (
              <input
                id={inputId}
                type="text"
                disabled={disabled}
                className={error ? 'invalid' : undefined}
                maxLength={field.maxLength}
                value={value == null ? '' : String(value)}
                onChange={(event) => onChange(field.name, event.target.value)}
              />
            )}
            {field.description && <span className="muted">{field.description}</span>}
            {error && <span className="field-error">{error}</span>}
          </label>
        )
      })}
    </div>
  )
}
