import { describe, expect, it } from 'vitest'
import { fieldsFromSchema, validateAgainstSchema } from './formSchema'
import type { JsonSchema } from '../models/solicitacao'

const schema: JsonSchema = {
  type: 'object',
  required: ['finalidade'],
  properties: {
    finalidade: { type: 'string', title: 'Finalidade da declaração', minLength: 5 },
    observacao: { type: 'string', title: 'Observação' },
  },
}

describe('formSchema', () => {
  it('deriva campos do JSON Schema sem tela por tipo', () => {
    const fields = fieldsFromSchema(schema)
    expect(fields.map((field) => field.name)).toEqual(['finalidade', 'observacao'])
    expect(fields[0].required).toBe(true)
    expect(fields[1].required).toBe(false)
  })

  it('valida obrigatório e minLength', () => {
    expect(validateAgainstSchema(schema, {}).finalidade).toBe('Campo obrigatório.')
    expect(validateAgainstSchema(schema, { finalidade: 'abc' }).finalidade).toContain('Mínimo')
    expect(validateAgainstSchema(schema, { finalidade: 'vínculo acadêmico' })).toEqual({})
  })
})
