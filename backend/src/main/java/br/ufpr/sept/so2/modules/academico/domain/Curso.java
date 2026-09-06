package br.ufpr.sept.so2.modules.academico.domain;

import java.time.OffsetDateTime;
import java.util.UUID;

public class Curso {

    private final UUID id;
    private String nome;
    private String sigla;
    private String codigo;
    private UUID idCoordenador;
    private int horasFormativasMinimas;
    private boolean ativo;
    private final OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;

    public Curso(
            UUID id,
            String nome,
            String sigla,
            String codigo,
            UUID idCoordenador,
            int horasFormativasMinimas,
            boolean ativo,
            OffsetDateTime createdAt,
            OffsetDateTime updatedAt
    ) {
        this.id = id;
        this.nome = nome;
        this.sigla = sigla;
        this.codigo = codigo;
        this.idCoordenador = idCoordenador;
        this.horasFormativasMinimas = horasFormativasMinimas;
        this.ativo = ativo;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public void atualizar(String nome, String sigla, String codigo, UUID idCoordenador, Integer horasFormativasMinimas, Boolean ativo) {
        if (nome != null) {
            this.nome = nome;
        }
        if (sigla != null) {
            this.sigla = sigla;
        }
        if (codigo != null) {
            this.codigo = codigo;
        }
        if (idCoordenador != null) {
            this.idCoordenador = idCoordenador;
        }
        if (horasFormativasMinimas != null) {
            this.horasFormativasMinimas = horasFormativasMinimas;
        }
        if (ativo != null) {
            this.ativo = ativo;
        }
        this.updatedAt = OffsetDateTime.now();
    }

    public UUID getId() {
        return id;
    }

    public String getNome() {
        return nome;
    }

    public String getSigla() {
        return sigla;
    }

    public String getCodigo() {
        return codigo;
    }

    public UUID getIdCoordenador() {
        return idCoordenador;
    }

    public int getHorasFormativasMinimas() {
        return horasFormativasMinimas;
    }

    public boolean isAtivo() {
        return ativo;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }

    public OffsetDateTime getUpdatedAt() {
        return updatedAt;
    }
}
