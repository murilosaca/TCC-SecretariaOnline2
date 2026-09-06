package br.ufpr.sept.so2.modules.academico.infrastructure.persistence;

import br.ufpr.sept.so2.modules.academico.domain.Disciplina;
import br.ufpr.sept.so2.shared.infrastructure.persistence.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

import java.util.UUID;

@Entity
@Table(name = "disciplina")
public class DisciplinaJpaEntity extends BaseEntity {

    @Column(name = "id_curso", nullable = false)
    private UUID idCurso;

    @Column(nullable = false, length = 20)
    private String codigo;

    @Column(nullable = false, length = 200)
    private String nome;

    @Column(nullable = false)
    private int periodo;

    @Column(name = "carga_horaria_total", nullable = false)
    private int cargaHorariaTotal;

    @Column(nullable = false)
    private int creditos;

    @Column(nullable = false)
    private boolean ativa;

    protected DisciplinaJpaEntity() {
    }

    public static DisciplinaJpaEntity fromDomain(Disciplina disciplina) {
        DisciplinaJpaEntity entity = new DisciplinaJpaEntity();
        entity.setId(disciplina.getId());
        entity.idCurso = disciplina.getIdCurso();
        entity.codigo = disciplina.getCodigo();
        entity.nome = disciplina.getNome();
        entity.periodo = disciplina.getPeriodo();
        entity.cargaHorariaTotal = disciplina.getCargaHorariaTotal();
        entity.creditos = disciplina.getCreditos();
        entity.ativa = disciplina.isAtiva();
        return entity;
    }

    public void merge(Disciplina disciplina) {
        this.codigo = disciplina.getCodigo();
        this.nome = disciplina.getNome();
        this.periodo = disciplina.getPeriodo();
        this.cargaHorariaTotal = disciplina.getCargaHorariaTotal();
        this.creditos = disciplina.getCreditos();
        this.ativa = disciplina.isAtiva();
    }

    public Disciplina toDomain() {
        return new Disciplina(
                getId(),
                idCurso,
                codigo,
                nome,
                periodo,
                cargaHorariaTotal,
                creditos,
                ativa,
                getCreatedAt(),
                getUpdatedAt()
        );
    }
}
