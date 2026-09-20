import { fireEvent, render, screen } from '@testing-library/react'
import { describe, expect, it, vi } from 'vitest'
import { PainelDeliberacao } from './PainelDeliberacao'

describe('PainelDeliberacao', () => {
  it('não mostra botões quando os _links de ação estão ausentes', () => {
    render(<PainelDeliberacao links={{ self: '/requests/1' }} onDeliberar={() => undefined} />)
    expect(screen.queryByRole('button', { name: 'Deferir' })).toBeNull()
    expect(screen.queryByRole('button', { name: 'Indeferir' })).toBeNull()
    expect(screen.getByRole('status').textContent).toMatch(/Nenhuma ação/i)
  })

  it('mostra só as ações presentes em _links e bloqueia indeferimento curto', () => {
    const onDeliberar = vi.fn()
    render(
      <PainelDeliberacao
        links={{
          deferir: '/requests/1/transitions',
          indeferir: '/requests/1/transitions',
        }}
        onDeliberar={onDeliberar}
      />,
    )
    expect(screen.getByRole('button', { name: 'Deferir' })).toBeTruthy()
    expect(screen.queryByRole('button', { name: 'Solicitar ajustes' })).toBeNull()

    fireEvent.click(screen.getByRole('button', { name: 'Indeferir' }))
    expect(onDeliberar).not.toHaveBeenCalled()
    expect(screen.getByRole('alert').textContent).toMatch(/mín. 20 caracteres/i)

    fireEvent.change(screen.getByLabelText('Parecer'), {
      target: { value: 'Deferido para comprovação de vínculo.' },
    })
    fireEvent.click(screen.getByRole('button', { name: 'Deferir' }))
    expect(onDeliberar).toHaveBeenCalledWith('DEFER', 'Deferido para comprovação de vínculo.')
  })
})
