import type { JsonSchema, JsonSchemaProperty } from '../models/solicitacao'

export type FormField = {
  name: string
  title: string
  type: 'string' | 'number' | 'integer' | 'boolean'
  required: boolean
  enumValues?: Array<string | number>
  format?: string
  minLength?: number
  maxLength?: number
  description?: string
}

export function fieldsFromSchema(schema?: JsonSchema | null): FormField[] {
  if (!schema?.properties) {
    return []
  }
  const required = new Set(schema.required ?? [])
  return Object.entries(schema.properties).map(([name, property]) => ({
    name,
    title: property.title ?? name,
    type: property.type ?? 'string',
    required: required.has(name),
    enumValues: property.enum,
    format: property.format,
    minLength: property.minLength,
    maxLength: property.maxLength,
    description: property.description,
  }))
}

export function validateAgainstSchema(
  schema: JsonSchema | undefined,
  values: Record<string, unknown>,
): Record<string, string> {
  const errors: Record<string, string> = {}
  for (const field of fieldsFromSchema(schema)) {
    const raw = values[field.name]
    const property: JsonSchemaProperty = schema?.properties?.[field.name] ?? {}
    if (field.required && isEmpty(raw)) {
      errors[field.name] = 'Campo obrigatório.'
      continue
    }
    if (isEmpty(raw)) {
      continue
    }
    if (field.type === 'string' && typeof raw === 'string') {
      if (property.minLength && raw.length < property.minLength) {
        errors[field.name] = `Mínimo de ${property.minLength} caracteres.`
      }
      if (property.maxLength && raw.length > property.maxLength) {
        errors[field.name] = `Máximo de ${property.maxLength} caracteres.`
      }
    }
    if (field.enumValues && !field.enumValues.map(String).includes(String(raw))) {
      errors[field.name] = 'Valor fora da lista permitida.'
    }
  }
  return errors
}

function isEmpty(value: unknown): boolean {
  return value === undefined || value === null || value === ''
}
