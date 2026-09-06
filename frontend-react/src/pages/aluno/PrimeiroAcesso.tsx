import { TelaPendente } from '../comum/TelaPendente'

export function PrimeiroAcesso() {
  return (
    <TelaPendente
      rf="RF-F1-002"
      titulo="Primeiro acesso"
      descricao="Enquanto senhaAlterada = false o restante do sistema fica bloqueado. Exige senha forte e aceite LGPD."
    />
  )
}
