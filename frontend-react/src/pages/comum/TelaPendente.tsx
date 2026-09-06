interface TelaPendenteProps {
  rf: string
  titulo: string
  descricao: string
}

export function TelaPendente({ rf, titulo, descricao }: TelaPendenteProps) {
  return (
    <section className="page">
      <h1>{titulo}</h1>
      <p className="lead">{descricao}</p>
      <p className="muted">
        Tela prevista em {rf} e em `docs/telas-figma.md`. O fluxo ainda não tem API nesta fundação
        (módulo `iam` / motor de solicitações).
      </p>
    </section>
  )
}
