/** Marca SEPT/UFPR para o shell público (AuthLayout). */
export function LogoSept({ className }: { className?: string }) {
  return (
    <svg
      className={className}
      viewBox="0 0 140 120"
      role="img"
      aria-label="SEPT — Setor de Educação Profissional e Tecnológica"
      xmlns="http://www.w3.org/2000/svg"
    >
      <path
        d="M20 78 C20 42 50 22 70 22 C90 22 120 42 120 78"
        fill="none"
        stroke="#0f4c81"
        strokeWidth="8"
        strokeLinecap="round"
      />
      <path
        d="M34 78 C34 50 52 34 70 34 C88 34 106 50 106 78"
        fill="none"
        stroke="#0f4c81"
        strokeWidth="5"
        strokeLinecap="round"
      />
      <text
        x="70"
        y="100"
        textAnchor="middle"
        fill="#0f4c81"
        fontFamily="Segoe UI, system-ui, sans-serif"
        fontSize="22"
        fontWeight="700"
        letterSpacing="2"
      >
        SEPT
      </text>
    </svg>
  )
}
