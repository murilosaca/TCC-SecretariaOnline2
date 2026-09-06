package br.ufpr.sept.so2.modules.academico.domain;

import br.ufpr.sept.so2.shared.domain.exception.DadoInvalidoException;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.UUID;

public class PeriodoLetivo {

    private final UUID id;
    private int ano;
    private int semestre;
    private LocalDate inicio;
    private LocalDate fim;
    private boolean ativo;
    private final OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;

    public PeriodoLetivo(
            UUID id,
            int ano,
            int semestre,
            LocalDate inicio,
            LocalDate fim,
            boolean ativo,
            OffsetDateTime createdAt,
            OffsetDateTime updatedAt
    ) {
        validar(ano, semestre, inicio, fim);
        this.id = id;
        this.ano = ano;
        this.semestre = semestre;
        this.inicio = inicio;
        this.fim = fim;
        this.ativo = ativo;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public void atualizar(Integer ano, Integer semestre, LocalDate inicio, LocalDate fim, Boolean ativo) {
        int novoAno = ano != null ? ano : this.ano;
        int novoSemestre = semestre != null ? semestre : this.semestre;
        LocalDate novoInicio = inicio != null ? inicio : this.inicio;
        LocalDate novoFim = fim != null ? fim : this.fim;
        validar(novoAno, novoSemestre, novoInicio, novoFim);
        this.ano = novoAno;
        this.semestre = novoSemestre;
        this.inicio = novoInicio;
        this.fim = novoFim;
        if (ativo != null) {
            this.ativo = ativo;
        }
        this.updatedAt = OffsetDateTime.now();
    }

    public boolean sobrepoe(PeriodoLetivo outro) {
        return inicio.isBefore(outro.fim) && outro.inicio.isBefore(fim);
    }

    public boolean vigenteEm(LocalDate data) {
        return ativo && !data.isBefore(inicio) && !data.isAfter(fim);
    }

    private static void validar(int ano, int semestre, LocalDate inicio, LocalDate fim) {
        if (ano < 2000 || ano > 2100) {
            throw new DadoInvalidoException("Ano letivo deve estar entre 2000 e 2100.");
        }
        if (semestre != 1 && semestre != 2) {
            throw new DadoInvalidoException("Semestre deve ser 1 ou 2.");
        }
        if (inicio == null || fim == null) {
            throw new DadoInvalidoException("Início e fim do período são obrigatórios.");
        }
        if (!fim.isAfter(inicio)) {
            throw new DadoInvalidoException("A data de fim deve ser posterior à de início.");
        }
    }

    public UUID getId() {
        return id;
    }

    public int getAno() {
        return ano;
    }

    public int getSemestre() {
        return semestre;
    }

    public LocalDate getInicio() {
        return inicio;
    }

    public LocalDate getFim() {
        return fim;
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
