package br.ufpr.sept.so2.modules.presenca.infrastructure.persistence

import org.springframework.data.jpa.repository.JpaRepository

interface PresencaDispositivoJpaRepository : JpaRepository<PresencaDispositivoJpaEntity, PresencaDispositivoId>
