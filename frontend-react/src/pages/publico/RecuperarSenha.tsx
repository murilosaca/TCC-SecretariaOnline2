import { TelaPendente } from '../comum/TelaPendente'

export function RecuperarSenha() {
  return (
    <TelaPendente
      rf="RF-F0-002"
      titulo="Recuperar senha"
      descricao="Informe e-mail ou GRR. A resposta da API será anti-enumeração, igual exista ou não a conta."
    />
  )
}
