package br.ufpr.sept.so2.modules.bff.api.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@JsonInclude(JsonInclude.Include.ALWAYS)
public record AlunoDashboardResponse(
        SaudacaoResponse saudacao,
        PeriodoVigenteResponse periodoVigente,
        Boolean alertaPeriodoAusente,
        KpisResponse kpis,
        List<PendenciaResponse> pendencias,
        List<UltimaSolicitacaoResponse> ultimasSolicitacoes,
        List<ProximoEventoResponse> proximosEventos,
        @JsonProperty("_links") Map<String, String> links
) {

    @JsonInclude(JsonInclude.Include.ALWAYS)
    public record SaudacaoResponse(String nome, String curso) {
    }

    @JsonInclude(JsonInclude.Include.ALWAYS)
    public record PeriodoVigenteResponse(UUID id, int ano, int semestre, String rotulo, LocalDate inicio, LocalDate fim) {
    }

    @JsonInclude(JsonInclude.Include.ALWAYS)
    public record KpisResponse(
            KpiHorasFormativas horasFormativas,
            Integer solicitacoesAbertas,
            Integer eventosHoje,
            Integer certificados
    ) {
    }

    public record KpiHorasFormativas(int validadas, int requeridas) {
    }

    @JsonInclude(JsonInclude.Include.ALWAYS)
    public record PendenciaResponse(UUID id, String titulo, String estado, String href) {
    }

    @JsonInclude(JsonInclude.Include.ALWAYS)
    public record UltimaSolicitacaoResponse(
            UUID id,
            String protocolo,
            String tipoNome,
            String estado,
            OffsetDateTime prazoEm,
            boolean slaVencido
    ) {
    }

    @JsonInclude(JsonInclude.Include.ALWAYS)
    public record ProximoEventoResponse(
            UUID id,
            String titulo,
            OffsetDateTime inicioEm,
            OffsetDateTime fimEm,
            boolean janelaAtiva,
            String href
    ) {
    }
}
