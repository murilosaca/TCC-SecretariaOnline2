package br.ufpr.sept.so2.modules.solicitacoes.domain;

import br.ufpr.sept.so2.shared.domain.exception.ConflitoEstadoException;
import org.junit.jupiter.api.Test;

import java.time.OffsetDateTime;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SolicitacaoTest {

    private static final OffsetDateTime AGORA = OffsetDateTime.parse("2026-03-01T12:00:00Z");

    @Test
    void transicaoInvalidaEhRejeitada() {
        Solicitacao solicitacao = aberta();
        ConflitoEstadoException ex = assertThrows(
                ConflitoEstadoException.class,
                () -> solicitacao.transicionar(
                        UUID.fromString("01800000-0000-7000-8000-0000000000ee"),
                        workflow(),
                        "CLOSE",
                        solicitacao.getSolicitanteId(),
                        "não permitido",
                        AGORA
                )
        );
        assertTrue(ex.getMessage().contains("EM_ANALISE --CLOSE-->"));
        assertEquals("EM_ANALISE", solicitacao.getEstado());
        assertEquals(1, solicitacao.getEventos().size());
    }

    @Test
    void deferMoveParaDeliberadaERegistraEvento() {
        Solicitacao solicitacao = aberta();
        solicitacao.transicionar(
                UUID.fromString("01800000-0000-7000-8000-0000000000ee"),
                workflow(),
                "DEFER",
                solicitacao.getSolicitanteId(),
                "Deferido.",
                AGORA
        );
        assertEquals("DELIBERADA", solicitacao.getEstado());
        assertEquals(2, solicitacao.getEventos().size());
        SolicitacaoEvento ultimo = solicitacao.getEventos().get(1);
        assertEquals(Solicitacao.EVENTO_TRANSICAO, ultimo.tipo());
        assertEquals("EM_ANALISE", ultimo.estadoDe());
        assertEquals("DELIBERADA", ultimo.estadoPara());
    }

    private static Solicitacao aberta() {
        TipoSolicitacao tipo = new TipoSolicitacao(
                UUID.fromString("01800000-0000-7000-8000-000000000001"),
                "DECLARACAO_SIMPLES",
                "Declaração simples",
                "teste",
                TipoSolicitacao.PUBLISHED,
                "{}",
                "{}",
                15,
                1,
                AGORA,
                AGORA
        );
        return Solicitacao.abrir(
                UUID.fromString("01800000-0000-7000-8000-0000000000aa"),
                UUID.fromString("01800000-0000-7000-8000-0000000000bb"),
                tipo,
                UUID.fromString("01800000-0000-7000-8000-0000000000cc"),
                Protocolo.formatar(2026, 1),
                "{\"finalidade\":\"vinculo\"}",
                workflow(),
                AGORA
        );
    }

    private static WorkflowDefinicao workflow() {
        return new WorkflowDefinicao("EM_ANALISE", Map.of(
                "EM_ANALISE", Map.of(
                        "DEFER", "DELIBERADA",
                        "INDEFER", "INDEFERIDA",
                        "REQUEST_ADJUST", "EM_AJUSTE"
                ),
                "EM_AJUSTE", Map.of("RESUBMIT", "EM_ANALISE"),
                "DELIBERADA", Map.of("CLOSE", "CONCLUIDA"),
                "INDEFERIDA", Map.of(),
                "CONCLUIDA", Map.of()
        ));
    }
}
