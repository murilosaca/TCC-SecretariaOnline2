package br.ufpr.sept.so2.modules.iam.application.ports

import br.ufpr.sept.so2.modules.iam.application.AtribuicaoPerfis
import br.ufpr.sept.so2.modules.iam.application.AuthorityResumo
import br.ufpr.sept.so2.modules.iam.application.MatrizFgac
import br.ufpr.sept.so2.modules.iam.application.PerfilResumo
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import java.util.UUID

interface PerfilAcessoPort : AuthoritiesDePerfilPort {
    fun listar(q: String?, pageable: Pageable): Page<PerfilResumo>

    fun buscar(id: UUID): PerfilResumo

    fun criar(nome: String, descricao: String?, authorities: List<String>, atorId: UUID, ip: String?): PerfilResumo

    fun atualizar(id: UUID, descricao: String?, authorities: List<String>, atorId: UUID, ip: String?): PerfilResumo

    fun excluir(id: UUID, atorId: UUID, ip: String?)

    fun listarAutoridades(): MatrizFgac

    fun atualizarDescricao(nome: String, descricao: String, atorId: UUID, ip: String?): AuthorityResumo

    fun salvarMatriz(vinculos: Map<UUID, List<String>>, atorId: UUID, ip: String?)

    fun atribuicaoDe(usuarioId: UUID): AtribuicaoPerfis

    fun substituirPerfis(usuarioId: UUID, perfilIds: List<UUID>, atorId: UUID, ip: String?): AtribuicaoPerfis

    fun garantirVinculo(usuarioId: UUID, nomes: List<String>)
}
