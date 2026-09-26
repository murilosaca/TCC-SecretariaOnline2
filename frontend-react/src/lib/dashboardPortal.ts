/** Painel do aluno = attendance.view_open ou request.view_own (mesmo critério do BFF). */
export function ePainelAluno(authorities: string[]): boolean {
  return authorities.includes('attendance.view_open') || authorities.includes('request.view_own')
}

/** Painel do professor = dashboard.view_self_professor e sem painel do aluno. */
export function ePainelProfessor(authorities: string[]): boolean {
  return authorities.includes('dashboard.view_self_professor') && !ePainelAluno(authorities)
}
