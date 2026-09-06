package br.ufpr.sept.so2.modules.academico.infrastructure.persistence;

import br.ufpr.sept.so2.modules.academico.domain.Curso;
import br.ufpr.sept.so2.shared.infrastructure.persistence.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

import java.util.UUID;

@Entity
@Table(name = "curso")
public class CursoJpaEntity extends BaseEntity {

    @Column(nullable = false, length = 200)
    private String nome;

    @Column(nullable = false, unique = true, length = 20)
    private String sigla;

    @Column(nullable = false, unique = true, length = 30)
    private String codigo;

    @Column(name = "id_coordenador")
    private UUID idCoordenador;

    @Column(name = "horas_formativas_minimas", nullable = false)
    private int horasFormativasMinimas;

    @Column(nullable = false)
    private boolean ativo;

    protected CursoJpaEntity() {
    }

    public static CursoJpaEntity fromDomain(Curso curso) {
        CursoJpaEntity entity = new CursoJpaEntity();
        entity.setId(curso.getId());
        entity.nome = curso.getNome();
        entity.sigla = curso.getSigla();
        entity.codigo = curso.getCodigo();
        entity.idCoordenador = curso.getIdCoordenador();
        entity.horasFormativasMinimas = curso.getHorasFormativasMinimas();
        entity.ativo = curso.isAtivo();
        return entity;
    }

    public void merge(Curso curso) {
        this.nome = curso.getNome();
        this.sigla = curso.getSigla();
        this.codigo = curso.getCodigo();
        this.idCoordenador = curso.getIdCoordenador();
        this.horasFormativasMinimas = curso.getHorasFormativasMinimas();
        this.ativo = curso.isAtivo();
    }

    public Curso toDomain() {
        return new Curso(
                getId(),
                nome,
                sigla,
                codigo,
                idCoordenador,
                horasFormativasMinimas,
                ativo,
                getCreatedAt(),
                getUpdatedAt()
        );
    }
}
