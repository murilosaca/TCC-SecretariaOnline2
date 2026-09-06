import { TelaPendente } from '../comum/TelaPendente'

export function VerificarProtocolo() {
  return (
    <TelaPendente
      rf="RF-F0-006"
      titulo="Verificar protocolo"
      descricao="Consulta pública do PDF de protocolo, sem autenticação."
    />
  )
}
