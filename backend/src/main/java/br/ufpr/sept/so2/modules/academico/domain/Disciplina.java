package br.ufpr.sept.so2.modules.academico.domain;

import java.time.OffsetDateTime;
import java.util.UUID;

public class Disciplina {

    private final UUID id;
    private final UUID idCurso;
    private String codigo;
    private String nome;
    private int periodo;
    private int cargaHorariaTotal;
    private int creditos;
    private boolean ativa;
    private final OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;

    public Disciplina(
            UUID id,
            UUID idCurso,
            String codigo,
            String nome,
            int periodo,
            int cargaHorariaTotal,
            int creditos,
            boolean ativa,
            OffsetDateTime createdAt,
            OffsetDateTime updatedAt
    ) {
        this.id = id;
        this.idCurso = idCurso;
        this.codigo = codigo;
        this.nome = nome;
        this.periodo = periodo;
        this.cargaHorariaTotal = cargaHorariaTotal;
        this.creditos = creditos;
        this.ativa = ativa;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public void atualizar(String codigo, String nome, Integer periodo, Integer cargaHorariaTotal, Integer creditos, Boolean ativa) {
        if (codigo != null) {
            this.codigo = codigo;
        }
        if (nome != null) {
            this.nome = nome;
        }
        if (periodo != null) {
            this.periodo = periodo;
        }
        if (cargaHorariaTotal != null) {
            this.cargaHorariaTotal = cargaHorariaTotal;
        }
        if (creditos != null) {
            this.creditos = creditos;
        }
        if (ativa != null) {
            this.ativa = ativa;
        }
        this.updatedAt = OffsetDateTime.now();
    }

    public UUID getId() {
        return id;
    }

    public UUID getIdCurso() {
        return idCurso;
    }

    public String getCodigo() {
        return codigo;
    }

    public String getNome() {
        return nome;
    }

    public int getPeriodo() {
        return periodo;
    }

    public int getCargaHorariaTotal() {
        return cargaHorariaTotal;
    }

    public int getCreditos() {
        return creditos;
    }

    public boolean isAtiva() {
        return ativa;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }

    public OffsetDateTime getUpdatedAt() {
        return updatedAt;
    }
}
