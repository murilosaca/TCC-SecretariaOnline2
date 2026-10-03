package br.ufpr.sept.so2.modules.iam.infrastructure

import br.ufpr.sept.so2.modules.iam.application.CatalogoPapeis
import br.ufpr.sept.so2.modules.iam.infrastructure.persistence.AuthorityJpaEntity
import br.ufpr.sept.so2.modules.iam.infrastructure.persistence.AuthorityJpaRepository
import br.ufpr.sept.so2.modules.iam.infrastructure.persistence.PerfilJpaEntity
import br.ufpr.sept.so2.modules.iam.infrastructure.persistence.PerfilJpaRepository
import org.springframework.boot.ApplicationArguments
import org.springframework.boot.ApplicationRunner
import org.springframework.core.annotation.Order
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

@Component
@Order(5)
class PerfilSistemaInitializer(
    private val authorityRepo: AuthorityJpaRepository,
    private val perfilRepo: PerfilJpaRepository,
) : ApplicationRunner {

    @Transactional
    override fun run(args: ApplicationArguments) {
        CatalogoPapeis.descricoes.forEach { (nome, descricao) ->
            if (!authorityRepo.existsById(nome)) {
                val entity = AuthorityJpaEntity()
                entity.nome = nome
                entity.descricao = descricao
                entity.modulo = CatalogoPapeis.modulo(nome)
                entity.sistema = true
                authorityRepo.save(entity)
            }
        }
        CatalogoPapeis.papeis.forEach { (nome, authorities) ->
            if (perfilRepo.findByNomeIgnoreCase(nome).isEmpty) {
                val perfil = PerfilJpaEntity()
                perfil.nome = nome
                perfil.descricao = "Perfil de sistema $nome"
                perfil.tipo = CatalogoPapeis.SYSTEM
                perfil.authorities = authorities.toMutableSet()
                perfilRepo.save(perfil)
            }
        }
    }
}
