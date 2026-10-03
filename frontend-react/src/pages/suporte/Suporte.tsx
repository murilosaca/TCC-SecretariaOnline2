import { FormEvent, useState } from 'react'
import { useMutation, useQuery } from '@tanstack/react-query'
import { ApiError } from '../../api/client'
import { suporteApi } from '../../api/suporte'

export function Suporte() {
  const faq = useQuery({ queryKey: ['faq'], queryFn: () => suporteApi.faq() })
  const [aberto, setAberto] = useState<string | null>(null)
  const [assunto, setAssunto] = useState('')
  const [mensagem, setMensagem] = useState('')
  const [erroCampo, setErroCampo] = useState<string | null>(null)
  const [protocolo, setProtocolo] = useState<string | null>(null)
  const abrir = useMutation({
    mutationFn: () => suporteApi.abrir(assunto.trim(), mensagem.trim()),
    onSuccess: (resposta) => {
      setProtocolo(resposta.protocolo)
      setAssunto('')
      setMensagem('')
      setErroCampo(null)
    },
  })
  const primeiro = faq.data?.[0]?.id
  const expandido = aberto ?? primeiro ?? null

  function alternar(id: string) {
    setAberto(expandido === id ? '' : id)
  }

  function enviar(event: FormEvent) {
    event.preventDefault()
    if (!assunto.trim()) {
      setErroCampo('Assunto é obrigatório')
      return
    }
    if (!mensagem.trim()) {
      setErroCampo('Mensagem é obrigatória')
      return
    }
    setErroCampo(null)
    abrir.mutate()
  }

  return (
    <section className="page suporte-layout" aria-labelledby="suporte-titulo">
      <h1 id="suporte-titulo">Suporte</h1>
      <div>
        <h2>Perguntas frequentes</h2>
        {faq.isLoading && <p className="muted">Carregando…</p>}
        {faq.isError && <p className="empty">Não foi possível carregar o FAQ.</p>}
        {faq.isSuccess && faq.data.length === 0 && <p className="empty">Nenhuma pergunta publicada.</p>}
        <div className="faq">
          {(faq.data ?? []).map((item) => {
            const visivel = expandido === item.id
            return (
              <div key={item.id}>
                <button
                  type="button"
                  aria-expanded={visivel}
                  onClick={() => alternar(item.id)}
                >
                  {item.pergunta}
                </button>
                {visivel && <p>{item.resposta}</p>}
              </div>
            )
          })}
        </div>
      </div>
      <form className="suporte-form" onSubmit={enviar}>
        <h2>Abrir ticket</h2>
        {protocolo && (
          <p className="banner success" role="status">
            Ticket aberto. Protocolo {protocolo}.
          </p>
        )}
        <label htmlFor="ticket-assunto">Assunto</label>
        <input
          id="ticket-assunto"
          value={assunto}
          onChange={(event) => setAssunto(event.target.value)}
          aria-invalid={erroCampo === 'Assunto é obrigatório'}
        />
        {erroCampo === 'Assunto é obrigatório' && <p className="banner danger">{erroCampo}</p>}
        <label htmlFor="ticket-mensagem">Mensagem</label>
        <textarea id="ticket-mensagem" value={mensagem} onChange={(event) => setMensagem(event.target.value)} />
        {erroCampo === 'Mensagem é obrigatória' && <p className="banner danger">{erroCampo}</p>}
        {abrir.isError && <p className="banner danger">{textoLimite(abrir.error)}</p>}
        <button type="submit" disabled={abrir.isPending}>
          Enviar ticket
        </button>
        <p>
          Ou contate: <a href="mailto:secretaria@ufpr.br">secretaria@ufpr.br</a>
        </p>
      </form>
    </section>
  )
}

function textoLimite(erro: unknown): string {
  if (erro instanceof ApiError && erro.status === 429) {
    const minutos = Math.max(1, Math.ceil((erro.retryAfterSeconds ?? 60) / 60))
    return `Limite de 3 tickets/hora atingido. Tente em ${minutos} min.`
  }
  if (erro instanceof ApiError) {
    return erro.message
  }
  return 'Não foi possível abrir o ticket.'
}
