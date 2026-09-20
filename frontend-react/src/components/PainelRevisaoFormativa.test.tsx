import { fireEvent, render, screen } from '@testing-library/react'
import { describe, expect, it, vi } from 'vitest'
import { PainelRevisaoFormativa } from './PainelRevisaoFormativa'

describe('PainelRevisaoFormativa', () => {
  it('não mostra botões quando os _links de ação estão ausentes', () => {
    render(<PainelRevisaoFormativa links={{ self: '/formativas/1' }} onRevisar={() => undefined} />)
    expect(screen.queryByRole('button', { name: 'Aprovar' })).toBeNull()
    expect(screen.queryByRole('button', { name: 'Indeferir' })).toBeNull()
    expect(screen.getByRole('status').textContent).toMatch(/Nenhuma ação/i)
  })

  it('mostra só as ações presentes em _links e bloqueia indeferimento curto', () => {
    const onRevisar = vi.fn()
    render(
      <PainelRevisaoFormativa
        links={{
          aprovar: '/formativas/1/aprovar',
          indeferir: '/formativas/1/indeferir',
        }}
        onRevisar={onRevisar}
      />,
    )
    expect(screen.getByRole('button', { name: 'Aprovar' })).toBeTruthy()
    expect(screen.getByRole('button', { name: 'Indeferir' })).toBeTruthy()

    fireEvent.click(screen.getByRole('button', { name: 'Indeferir' }))
    expect(onRevisar).not.toHaveBeenCalled()
    expect(screen.getByRole('alert').textContent).toMatch(/mín. 20 caracteres/i)

    fireEvent.change(screen.getByLabelText('Parecer'), {
      target: { value: 'Presença validada; horas da oficina mantidas.' },
    })
    fireEvent.click(screen.getByRole('button', { name: 'Aprovar' }))
    expect(onRevisar).toHaveBeenCalledWith('APROVAR', 'Presença validada; horas da oficina mantidas.')
  })
})
