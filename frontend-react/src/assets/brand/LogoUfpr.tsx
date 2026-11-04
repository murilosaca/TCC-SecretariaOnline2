/** Marca institucional UFPR para o shell público (AuthLayout). */
export function LogoUfpr({ className }: { className?: string }) {
  return (
    <svg
      className={className}
      viewBox="0 0 120 120"
      role="img"
      aria-label="Universidade Federal do Paraná"
      xmlns="http://www.w3.org/2000/svg"
    >
      <circle cx="60" cy="60" r="56" fill="#0f4c81" />
      <circle cx="60" cy="60" r="48" fill="none" stroke="#ffffff" strokeWidth="2.5" />
      <text
        x="60"
        y="68"
        textAnchor="middle"
        fill="#ffffff"
        fontFamily="Segoe UI, system-ui, sans-serif"
        fontSize="28"
        fontWeight="700"
        letterSpacing="1"
      >
        UFPR
      </text>
    </svg>
  )
}
