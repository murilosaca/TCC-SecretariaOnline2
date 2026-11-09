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
    return [
      <p key="vazio" className="muted">
        A pré-visualização aparece aqui.
      </p>,
    ]
  }

  const linhas = texto.split('\n')
  const nos: ReactNode[] = []
  let i = 0

  while (i < linhas.length) {
    const linha = linhas[i]
    const chave = `l-${i}`

    if (/^\s*([-*_] *){3,}\s*$/.test(linha)) {
      nos.push(<hr key={chave} />)
      i += 1
      continue
    }

    if (linha.startsWith('### ')) {
      nos.push(<h3 key={chave}>{inline(linha.slice(4))}</h3>)
      i += 1
      continue
    }
    if (linha.startsWith('## ')) {
      nos.push(<h2 key={chave}>{inline(linha.slice(3))}</h2>)
      i += 1
      continue
    }
    if (linha.startsWith('# ')) {
      nos.push(<h3 key={chave}>{inline(linha.slice(2))}</h3>)
      i += 1
      continue
    }

    if (linha.startsWith('> ')) {
      const citacao: string[] = []
      while (i < linhas.length && linhas[i].startsWith('> ')) {
        citacao.push(linhas[i].slice(2))
        i += 1
      }
      nos.push(
        <blockquote key={chave}>
          {citacao.map((trecho, indice) => (
            <p key={`${chave}-q-${indice}`}>{inline(trecho)}</p>
          ))}
        </blockquote>,
      )
      continue
    }

    if (/^[-*] /.test(linha)) {
      const itens: string[] = []
      while (i < linhas.length && /^[-*] /.test(linhas[i])) {
        itens.push(linhas[i].slice(2))
        i += 1
      }
      nos.push(
        <ul key={chave}>
          {itens.map((item, indice) => (
            <li key={`${chave}-u-${indice}`}>{inline(item)}</li>
          ))}
        </ul>,
      )
      continue
    }

    if (/^\d+\. /.test(linha)) {
      const itens: string[] = []
      while (i < linhas.length && /^\d+\. /.test(linhas[i])) {
        itens.push(linhas[i].replace(/^\d+\. /, ''))
        i += 1
      }
      nos.push(
        <ol key={chave}>
          {itens.map((item, indice) => (
            <li key={`${chave}-o-${indice}`}>{inline(item)}</li>
          ))}
        </ol>,
      )
      continue
    }

    if (!linha.trim()) {
      nos.push(<br key={chave} />)
      i += 1
      continue
    }

    nos.push(<p key={chave}>{inline(linha)}</p>)
    i += 1
  }

  return nos
}

function inline(texto: string): ReactNode[] {
  return dividir(texto, /(`[^`]+`|\[[^\]]+\]\([^)\s]+\)|\*\*[^*]+\*\*)/g).flatMap((parte, indice) => {
    if (parte.startsWith('`') && parte.endsWith('`') && parte.length > 2) {
      return (
        <code key={`c-${indice}`} className="md-code">
          {parte.slice(1, -1)}
        </code>
      )
    }
    const link = /^\[([^\]]+)\]\(([^)\s]+)\)$/.exec(parte)
    if (link) {
      return (
        <a key={`a-${indice}`} href={link[2]} target="_blank" rel="noreferrer">
          {inlineItalico(link[1])}
        </a>
      )
    }
    if (parte.startsWith('**') && parte.endsWith('**') && parte.length > 4) {
      return <strong key={`b-${indice}`}>{inlineItalico(parte.slice(2, -2))}</strong>
    }
    return inlineItalico(parte, `t-${indice}`)
  })
}

function inlineItalico(texto: string, chave = 'i'): ReactNode[] {
  return dividir(texto, /(\*[^*]+\*|_[^_]+_)/g).map((parte, indice) => {
    if (
      (parte.startsWith('*') && parte.endsWith('*') && parte.length > 2) ||
      (parte.startsWith('_') && parte.endsWith('_') && parte.length > 2)
    ) {
      return <em key={`${chave}-${indice}`}>{parte.slice(1, -1)}</em>
    }
    return <span key={`${chave}-${indice}`}>{parte}</span>
  })
}

function dividir(texto: string, padrao: RegExp): string[] {
  const partes = texto.split(padrao)
  return partes.filter((parte) => parte !== undefined && parte !== '')
}
