package br.ufpr.sept.so2.modules.formativas.application.ports

import br.ufpr.sept.so2.modules.formativas.domain.TipoComissao
import java.util.UUID

interface ComissaoMembroPort {
    fun cursosDoMembro(usuarioId: UUID, tipo: TipoComissao): Set<UUID>

    fun membrosDosCursos(cursoIds: Collection<UUID>, tipo: TipoComissao): List<UUID>

    fun ehMembro(usuarioId: UUID, cursoId: UUID, tipo: TipoComissao): Boolean

    fun adicionarSeAusente(cursoId: UUID, usuarioId: UUID, tipo: TipoComissao)
}
