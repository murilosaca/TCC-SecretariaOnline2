import type { RefObject } from 'react'

type Props = {
  value: string
  onChange: (proximo: string) => void
  textareaRef: RefObject<HTMLTextAreaElement | null>
}

type Acao = {
  rotulo: string
  ariaLabel: string
  aplicar: (valor: string, inicio: number, fim: number) => { texto: string; cursor: number }
}

const ACOES: Acao[] = [
  {
    rotulo: 'Negrito',
    ariaLabel: 'Negrito',
    aplicar: (valor, inicio, fim) => envolver(valor, inicio, fim, '**', '**', 'negrito'),
  },
  {
    rotulo: 'Itálico',
    ariaLabel: 'Itálico',
    aplicar: (valor, inicio, fim) => envolver(valor, inicio, fim, '*', '*', 'itálico'),
  },
  {
    rotulo: 'Título',
    ariaLabel: 'Inserir título',
    aplicar: (valor, inicio, fim) => prefixarLinha(valor, inicio, fim, '## '),
  },
  {
    rotulo: 'Lista',
    ariaLabel: 'Lista',
    aplicar: (valor, inicio, fim) => prefixarLinha(valor, inicio, fim, '- '),
  },
  {
    rotulo: 'Numeração',
    ariaLabel: 'Numeração',
    aplicar: (valor, inicio, fim) => prefixarLinha(valor, inicio, fim, '1. '),
  },
  {
    rotulo: 'Citação',
    ariaLabel: 'Citação',
    aplicar: (valor, inicio, fim) => prefixarLinha(valor, inicio, fim, '> '),
  },
  {
    rotulo: 'Código',
    ariaLabel: 'Código',
    aplicar: (valor, inicio, fim) => envolver(valor, inicio, fim, '`', '`', 'código'),
  },
  {
    rotulo: 'Link',
    ariaLabel: 'Link',
    aplicar: (valor, inicio, fim) => {
      const selecionado = valor.slice(inicio, fim) || 'texto'
      const inserido = `[${selecionado}](https://)`
      return {
        texto: valor.slice(0, inicio) + inserido + valor.slice(fim),
        cursor: inicio + selecionado.length + 3 + 'https://'.length,
      }
    },
  },
  {
    rotulo: 'Separador',
    ariaLabel: 'Separador',
    aplicar: (valor, inicio) => {
      const precisaQuebraAntes = inicio > 0 && valor[inicio - 1] !== '\n'
      const precisaQuebraDepois = inicio < valor.length && valor[inicio] !== '\n'
      const inserido = `${precisaQuebraAntes ? '\n' : ''}---${precisaQuebraDepois ? '\n' : ''}`
      return {
        texto: valor.slice(0, inicio) + inserido + valor.slice(inicio),
        cursor: inicio + inserido.length,
      }
    },
  },
]

export function MarkdownToolbar({ value, onChange, textareaRef }: Props) {
  function aplicar(acao: Acao) {
    const campo = textareaRef.current
    const inicio = campo?.selectionStart ?? value.length
    const fim = campo?.selectionEnd ?? value.length
    const resultado = acao.aplicar(value, inicio, fim)
    onChange(resultado.texto)
    requestAnimationFrame(() => {
      const alvo = textareaRef.current
      if (!alvo) {
        return
      }
      alvo.focus()
      alvo.setSelectionRange(resultado.cursor, resultado.cursor)
    })
  }

  return (
    <div className="markdown-toolbar" role="toolbar" aria-label="Formatação Markdown">
      {ACOES.map((acao) => (
        <button
          key={acao.ariaLabel}
          type="button"
          className="markdown-toolbar-btn"
          aria-label={acao.ariaLabel}
          title={acao.ariaLabel}
          onClick={() => aplicar(acao)}
        >
          {acao.rotulo}
        </button>
      ))}
    </div>
  )
}

function envolver(
  valor: string,
  inicio: number,
  fim: number,
  antes: string,
  depois: string,
  placeholder: string,
): { texto: string; cursor: number } {
  const selecionado = valor.slice(inicio, fim)
  const miolo = selecionado || placeholder
  const inserido = `${antes}${miolo}${depois}`
  return {
    texto: valor.slice(0, inicio) + inserido + valor.slice(fim),
    cursor: selecionado
      ? inicio + inserido.length
      : inicio + antes.length + miolo.length,
  }
}

function prefixarLinha(
  valor: string,
  inicio: number,
  fim: number,
  prefixo: string,
): { texto: string; cursor: number } {
  const inicioLinha = valor.lastIndexOf('\n', Math.max(0, inicio - 1)) + 1
  const trecho = valor.slice(inicioLinha, fim)
  const linhas = trecho.length === 0 ? [''] : trecho.split('\n')
  const reescrito = linhas
    .map((linha) => (linha.startsWith(prefixo) ? linha : `${prefixo}${linha || 'item'}`))
    .join('\n')
  return {
    texto: valor.slice(0, inicioLinha) + reescrito + valor.slice(fim),
    cursor: inicioLinha + reescrito.length,
  }
}
