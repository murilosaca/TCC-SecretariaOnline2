package br.ufpr.sept.so2.modules.coordenacao.infrastructure.persistence

import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID

interface ElegibilidadeHorasJpaRepository : JpaRepository<ElegibilidadeHorasJpaEntity, UUID>
