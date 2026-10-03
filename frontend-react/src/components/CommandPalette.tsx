import { useEffect, useMemo, useRef, useState, type KeyboardEvent } from 'react'
import { useNavigate } from 'react-router-dom'
import { buscarGlobal, type BuscaHit, type BuscaResultado } from '../api/busca'

const GRUPOS: { chave: keyof BuscaResultado; titulo: string }[] = [
  { chave: 'alunos', titulo: 'Alunos' },
  { chave: 'solicitacoes', titulo: 'Solicitações' },
  { chave: 'eventos', titulo: 'Eventos' },
  { chave: 'usuarios', titulo: 'Usuários' },
]

type ItemLista = BuscaHit & { grupo: string }

export function CommandPalette({ aberto, onFechar }: { aberto: boolean; onFechar: () => void }) {
  const navigate = useNavigate()
  const inputRef = useRef<HTMLInputElement>(null)
  const [q, setQ] = useState('')
  const [resultado, setResultado] = useState<BuscaResultado | null>(null)
  const [carregando, setCarregando] = useState(false)
  const [erro, setErro] = useState<string | null>(null)
  const [ativo, setAtivo] = useState(0)

  const itens = useMemo(() => achatar(resultado), [resultado])

  useEffect(() => {
    if (!aberto) {
      return
    }
    inputRef.current?.focus()
  }, [aberto])

  useEffect(() => {
    if (!aberto) {
      return
    }
    const termo = q.trim()
    if (termo.length < 2) {
      setResultado(null)
      setCarregando(false)
      setErro(null)
      return
    }
    const controle = new AbortController()
    const timer = window.setTimeout(() => {
      const estouro = window.setTimeout(() => controle.abort(), 5000)
      setCarregando(true)
      setErro(null)
      buscarGlobal(termo, controle.signal)
        .then((resposta) => {
          setResultado(resposta)
          setAtivo(0)
        })
        .catch((falha: unknown) => {
          if (falha instanceof DOMException && falha.name === 'AbortError') {
            setErro('A busca demorou demais. Tente novamente.')
            return
          }
          setErro('Não foi possível buscar agora.')
        })
        .finally(() => {
          window.clearTimeout(estouro)
          setCarregando(false)
        })
    }, 200)
    return () => {
      window.clearTimeout(timer)
      controle.abort()
    }
  }, [q, aberto])

  if (!aberto) {
    return null
  }

  function fechar() {
    setQ('')
    setResultado(null)
    onFechar()
  }

  function ir(item: ItemLista) {
    fechar()
    navigate(item.href)
  }

  function tecla(event: KeyboardEvent) {
    if (event.key === 'Escape') {
      event.preventDefault()
      fechar()
      return
    }
    if (itens.length === 0) {
      return
    }
    if (event.key === 'ArrowDown') {
      event.preventDefault()
      setAtivo((indice) => Math.min(indice + 1, itens.length - 1))
    } else if (event.key === 'ArrowUp') {
      event.preventDefault()
      setAtivo((indice) => Math.max(indice - 1, 0))
    } else if (event.key === 'Enter') {
      event.preventDefault()
      ir(itens[ativo])
    }
  }

  const termo = q.trim()
  const vazio = termo.length >= 2 && resultado && itens.length === 0 && !carregando

  return (
    <div className="command-palette" role="presentation" onMouseDown={fechar}>
      <div
        className="command-panel"
        role="dialog"
        aria-label="Busca global"
        onMouseDown={(event) => event.stopPropagation()}
        onKeyDown={tecla}
      >
        <div className="palette-bar">
          <input
            ref={inputRef}
            role="combobox"
            aria-expanded={itens.length > 0}
            aria-controls="busca-resultados"
            aria-activedescendant={itens[ativo] ? `busca-item-${itens[ativo].id}` : undefined}
            aria-label="Termo da busca"
            value={q}
            onChange={(event) => setQ(event.target.value)}
          />
          <button type="button" className="palette-cancel ghost" onClick={fechar}>
            Cancelar
          </button>
        </div>
        {termo.length < 2 && <p className="muted">Digite ao menos 2 caracteres.</p>}
        {carregando && <p className="muted">Buscando…</p>}
        {erro && <p className="banner danger">{erro}</p>}
        {vazio && <p className="empty">Nenhum resultado encontrado para '{termo}'</p>}
        <div id="busca-resultados">
          {GRUPOS.map((grupo) => {
            const lista = (resultado?.[grupo.chave] ?? []) as BuscaHit[]
            if (lista.length === 0) {
              return null
            }
            return (
              <section key={grupo.chave}>
                <h2>{grupo.titulo}</h2>
                <ul>
                  {lista.map((item) => {
                    const indice = itens.findIndex((candidato) => candidato.id === item.id)
                    return (
                      <li key={item.id}>
                        <button
                          id={`busca-item-${item.id}`}
                          type="button"
                          className={indice === ativo ? 'ativo' : undefined}
                          onClick={() => ir({ ...item, grupo: grupo.titulo })}
                        >
                          <strong>{item.titulo}</strong>
                          <span className="muted"> {item.subtitulo}</span>
                        </button>
                      </li>
                    )
                  })}
                </ul>
              </section>
            )
          })}
        </div>
      </div>
    </div>
  )
}

function achatar(resultado: BuscaResultado | null): ItemLista[] {
  if (!resultado) {
    return []
  }
  return GRUPOS.flatMap((grupo) =>
    ((resultado[grupo.chave] ?? []) as BuscaHit[]).map((item) => ({ ...item, grupo: grupo.titulo })),
  )
}
