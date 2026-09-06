import { TelaPendente } from '../comum/TelaPendente'

export function NovaSenha() {
  return (
    <TelaPendente
      rf="RF-F0-003"
      titulo="Definir nova senha"
      descricao="Esta rota recebe ?token= do e-mail de recuperação. O token não deve ser persistido no navegador."
    />
  )
}
