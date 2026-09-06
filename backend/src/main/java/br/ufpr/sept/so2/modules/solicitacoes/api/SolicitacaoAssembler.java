package br.ufpr.sept.so2.modules.solicitacoes.api;

import br.ufpr.sept.so2.modules.solicitacoes.api.dto.RequestTypeResponse;
import br.ufpr.sept.so2.modules.solicitacoes.api.dto.SolicitacaoEventoResponse;
import br.ufpr.sept.so2.modules.solicitacoes.api.dto.SolicitacaoResponse;
import br.ufpr.sept.so2.modules.solicitacoes.domain.Solicitacao;
import br.ufpr.sept.so2.modules.solicitacoes.domain.TipoSolicitacao;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;

import java.time.OffsetDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Component
public class SolicitacaoAssembler {

    private static final TypeReference<Map<String, Object>> MAPA = new TypeReference<>() {
    };

    private final ObjectMapper objectMapper;

    public SolicitacaoAssembler(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public SolicitacaoResponse from(Solicitacao solicitacao, List<String> authorities, boolean detalhe) {
        OffsetDateTime agora = OffsetDateTime.now();
        boolean vencido = solicitacao.prazoVencido(agora);
        Map<String, String> links = new LinkedHashMap<>();
        String self = "/requests/" + solicitacao.getId();
        links.put("self", self);
        if (authorities.contains("request.deliberate")) {
            links.put("deliberar", self + "/transitions");
        }
        List<SolicitacaoEventoResponse> eventos = detalhe
                ? solicitacao.getEventos().stream().map(SolicitacaoEventoResponse::from).toList()
                : List.of();
        return new SolicitacaoResponse(
                solicitacao.getId(),
                solicitacao.getProtocolo().getValor(),
                solicitacao.getTipoCodigo(),
                solicitacao.getTipoNome(),
                solicitacao.getTipoVersao(),
                solicitacao.getEstado(),
                readMap(solicitacao.getPayloadJson()),
                detalhe ? readMap(solicitacao.getFormSchemaSnapshot()) : null,
                solicitacao.getPrazoEm(),
                vencido,
                vencido ? "ATRASADO" : "NO_PRAZO",
                solicitacao.getCreatedAt(),
                solicitacao.getUpdatedAt(),
                eventos,
                links
        );
    }

    public RequestTypeResponse from(TipoSolicitacao tipo) {
        return new RequestTypeResponse(
                tipo.getId(),
                tipo.getCodigo(),
                tipo.getNome(),
                tipo.getDescricao(),
                tipo.getStatus(),
                tipo.getPrazoDias(),
                tipo.getVersao(),
                readMap(tipo.getFormSchema()),
                readMap(tipo.getWorkflowJson()),
                Map.of("self", "/request-types/" + tipo.getCodigo())
        );
    }

    private Map<String, Object> readMap(String json) {
        if (json == null || json.isBlank()) {
            return Map.of();
        }
        try {
            return objectMapper.readValue(json, MAPA);
        } catch (Exception ex) {
            return Map.of();
        }
    }
}
