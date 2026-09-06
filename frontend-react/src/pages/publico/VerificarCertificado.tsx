import { TelaPendente } from '../comum/TelaPendente'

export function VerificarCertificado() {
  return (
    <TelaPendente
      rf="RF-F0-007"
      titulo="Verificar certificado"
      descricao="Consulta pública do hash do certificado digital gerado pelo sistema."
    />
  )
}
