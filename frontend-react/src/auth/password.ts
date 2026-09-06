export const PASSWORD_MIN = 12

export const passwordRules = [
  { id: 'tamanho', label: 'Mínimo de 12 caracteres', test: (value: string) => value.length >= PASSWORD_MIN },
  { id: 'maiuscula', label: 'Uma letra maiúscula', test: (value: string) => /[A-Z]/.test(value) },
  { id: 'minuscula', label: 'Uma letra minúscula', test: (value: string) => /[a-z]/.test(value) },
  { id: 'numero', label: 'Um número', test: (value: string) => /\d/.test(value) },
  { id: 'especial', label: 'Um caractere especial', test: (value: string) => /[^A-Za-z0-9]/.test(value) },
] as const

export function isStrongPassword(value: string): boolean {
  return passwordRules.every((rule) => rule.test(value))
}

export function passwordScore(value: string): 0 | 1 | 2 | 3 | 4 {
  if (!value) {
    return 0
  }
  const met = passwordRules.filter((rule) => rule.test(value)).length
  if (met <= 1 || value.length < 8) {
    return 1
  }
  if (met <= 3) {
    return 2
  }
  if (met === 4) {
    return 3
  }
  return 4
}

export function isValidEmail(value: string): boolean {
  return /^[a-zA-Z0-9._%+\-]+@[a-zA-Z0-9.\-]+\.[a-zA-Z]{2,}$/.test(value.trim())
}
