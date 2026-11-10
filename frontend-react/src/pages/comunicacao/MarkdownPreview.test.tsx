import { render, screen } from '@testing-library/react'
import { describe, expect, it } from 'vitest'
import { MarkdownPreview } from './MarkdownPreview'

describe('MarkdownPreview', () => {
  it('renderiza o subset ampliado de Markdown', () => {
    render(
      <MarkdownPreview
        texto={[
          '# Título 1',
          '## Título 2',
          '### Título 3',
          'Texto com **negrito**, *itálico* e `código`.',
          '',
          '- item a',
          '- item b',
          '',
          '1. primeiro',
          '2. segundo',
          '',
          '> citação',
          '',
          '[UFPR](https://www.ufpr.br)',
          '',
          '---',
        ].join('\n')}
      />,
    )

    expect(screen.getByRole('heading', { level: 3, name: 'Título 1' })).toBeTruthy()
    expect(screen.getByRole('heading', { level: 2, name: 'Título 2' })).toBeTruthy()
    expect(screen.getByRole('heading', { level: 3, name: 'Título 3' })).toBeTruthy()
    expect(screen.getByText('negrito').closest('strong')).toBeTruthy()
    expect(screen.getByText('itálico').closest('em')).toBeTruthy()
    expect(screen.getByText('código').closest('code')).toBeTruthy()
    expect(screen.getByText('item a').closest('ul')).toBeTruthy()
    expect(screen.getByText('primeiro').closest('ol')).toBeTruthy()
    expect(screen.getByText('citação').closest('blockquote')).toBeTruthy()
    expect(screen.getByRole('link', { name: 'UFPR' }).getAttribute('href')).toBe('https://www.ufpr.br')
    expect(screen.getByRole('region', { name: 'Pré-visualização' }).querySelector('hr')).toBeTruthy()
  })
})
