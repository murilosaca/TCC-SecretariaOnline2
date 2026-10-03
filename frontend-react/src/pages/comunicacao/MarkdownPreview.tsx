import type { ReactNode } from 'react'

export function MarkdownPreview({ texto }: { texto: string }) {
  return (
    <div className="markdown-preview" role="region" aria-label="Pré-visualização" aria-readonly="true">
      {interpretar(texto)}
    </div>
  )
}

function interpretar(texto: string): ReactNode[] {
  if (!texto.trim()) {
    return [<p key="vazio" className="muted">A pré-visualização aparece aqui.</p>]
  }
  return texto.split('\n').map((linha, indice) => {
    if (linha.startsWith('## ')) {
      return <h2 key={indice}>{inline(linha.slice(3))}</h2>
    }
    if (linha.startsWith('# ')) {
      return <h3 key={indice}>{inline(linha.slice(2))}</h3>
    }
    if (!linha.trim()) {
      return <br key={indice} />
    }
    return <p key={indice}>{inline(linha)}</p>
  })
}

function inline(texto: string): ReactNode[] {
  return texto.split(/(\*\*[^*]+\*\*)/g).map((parte, indice) => {
    if (parte.startsWith('**') && parte.endsWith('**') && parte.length > 4) {
      return <strong key={indice}>{parte.slice(2, -2)}</strong>
    }
    return <span key={indice}>{parte}</span>
  })
}
