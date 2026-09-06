package br.ufpr.sept.so2.modules.academico.infrastructure.persistence;

import br.ufpr.sept.so2.modules.academico.domain.PeriodoLetivo;
import br.ufpr.sept.so2.shared.infrastructure.persistence.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

import java.time.LocalDate;

@Entity
@Table(name = "periodo_letivo")
public class PeriodoLetivoJpaEntity extends BaseEntity {

    @Column(nullable = false)
    private short ano;

    @Column(nullable = false)
    private short semestre;

    @Column(nullable = false)
    private LocalDate inicio;

    @Column(nullable = false)
    private LocalDate fim;

    @Column(nullable = false)
    private boolean ativo;

    protected PeriodoLetivoJpaEntity() {
    }

    public static PeriodoLetivoJpaEntity fromDomain(PeriodoLetivo periodo) {
        PeriodoLetivoJpaEntity entity = new PeriodoLetivoJpaEntity();
        entity.setId(periodo.getId());
        entity.merge(periodo);
        return entity;
    }

    public void merge(PeriodoLetivo periodo) {
        this.ano = (short) periodo.getAno();
        this.semestre = (short) periodo.getSemestre();
        this.inicio = periodo.getInicio();
        this.fim = periodo.getFim();
        this.ativo = periodo.isAtivo();
    }

    public PeriodoLetivo toDomain() {
        return new PeriodoLetivo(
                getId(),
                ano,
                semestre,
                inicio,
                fim,
                ativo,
                getCreatedAt(),
                getUpdatedAt()
        );
    }
}
