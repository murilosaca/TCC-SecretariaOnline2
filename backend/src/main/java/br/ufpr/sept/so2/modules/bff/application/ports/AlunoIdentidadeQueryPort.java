package br.ufpr.sept.so2.modules.bff.application.ports;

import java.util.UUID;

public interface AlunoIdentidadeQueryPort {

    AlunoIdentidade consultar(UUID usuarioId);

    record AlunoIdentidade(String nome, String curso) {
    }
}
