/** Espelha os redirects planos de App.tsx (F5.6–F5.9). */
import { render, screen } from '@testing-library/react'
import { MemoryRouter, Navigate, Route, Routes } from 'react-router-dom'
import { describe, expect, it } from 'vitest'

function RotasPlanas() {
  return (
    <Routes>
      <Route path="secretaria/cursos" element={<span>cursos-oficial</span>} />
      <Route path="secretaria/disciplinas" element={<span>disciplinas-oficial</span>} />
      <Route path="secretaria/alunos" element={<span>alunos-oficial</span>} />
      <Route path="secretaria/calendarios" element={<span>calendarios-oficial</span>} />
      <Route path="cursos" element={<Navigate to="/secretaria/cursos" replace />} />
      <Route path="disciplinas" element={<Navigate to="/secretaria/disciplinas" replace />} />
      <Route path="alunos" element={<Navigate to="/secretaria/alunos" replace />} />
      <Route path="calendarios" element={<Navigate to="/secretaria/calendarios" replace />} />
      <Route path="erro/:codigo" element={<span>erro-404</span>} />
      <Route path="*" element={<Navigate to="/erro/404" replace />} />
    </Routes>
  )
}

describe('rotas planas F5.6–F5.9', () => {
  it.each([
    ['/cursos', 'cursos-oficial'],
    ['/disciplinas', 'disciplinas-oficial'],
    ['/alunos', 'alunos-oficial'],
    ['/calendarios', 'calendarios-oficial'],
  ])('redireciona %s para a rota /secretaria/*', (path, destino) => {
    render(
      <MemoryRouter initialEntries={[path]}>
        <RotasPlanas />
      </MemoryRouter>,
    )
    expect(screen.getByText(destino)).toBeTruthy()
    expect(screen.queryByText('erro-404')).toBeNull()
  })
})
