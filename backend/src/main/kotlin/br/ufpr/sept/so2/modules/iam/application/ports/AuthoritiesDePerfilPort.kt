package br.ufpr.sept.so2.modules.iam.application.ports

import java.util.UUID

/**
 * União das authorities dos perfis do usuário.
 * null = nenhum perfil vinculado (vale a lista direta em usuario_authority).
 */
interface AuthoritiesDePerfilPort {
    fun uniao(usuarioId: UUID): List<String>?
}
