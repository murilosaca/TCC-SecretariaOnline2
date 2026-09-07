package br.ufpr.sept.so2.modules.bff.application.ports;

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

public interface PeriodoVigenteQueryPort {

    Optional<PeriodoVigenteResumo> consultar(LocalDate data);

    record PeriodoVigenteResumo(UUID id, int ano, int semestre, LocalDate inicio, LocalDate fim) {

        public String rotulo() {
            return ano + "/" + semestre;
        }
    }
}
