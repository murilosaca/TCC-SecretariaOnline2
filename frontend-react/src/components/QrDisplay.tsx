import { useEffect, useState } from 'react'
import QRCode from 'qrcode'

type Props = {
  token: string
}

export function QrDisplay({ token }: Props) {
  const [src, setSrc] = useState<string | null>(null)

  useEffect(() => {
    let cancelado = false
    void QRCode.toDataURL(token, {
      width: 280,
      margin: 1,
      errorCorrectionLevel: 'M',
    }).then((url) => {
      if (!cancelado) {
        setSrc(url)
      }
    }).catch(() => {
      if (!cancelado) {
        setSrc(null)
      }
    })
    return () => {
      cancelado = true
    }
  }, [token])

  return (
    <figure className="qr-display">
      {src ? (
        <img src={src} width={280} height={280} alt="QR de presença" />
      ) : (
        <p className="pin-display token-fallback" aria-label="Token QR de presença">
          {token}
        </p>
      )}
      <figcaption className="muted">Token da janela ativa · renovado a cada 5 min</figcaption>
    </figure>
  )
}
