package br.ufpr.sept.so2.modules.solicitacoes.infrastructure.persistence;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface ProtocoloSeqJpaRepository extends JpaRepository<ProtocoloSeqJpaEntity, Integer> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select s from ProtocoloSeqJpaEntity s where s.ano = :ano")
    Optional<ProtocoloSeqJpaEntity> lockByAno(@Param("ano") Integer ano);
}
