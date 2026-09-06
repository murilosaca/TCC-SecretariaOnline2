package br.ufpr.sept.so2.modules.solicitacoes.application;

import br.ufpr.sept.so2.modules.iam.application.ports.AuditLogPort;
import br.ufpr.sept.so2.modules.iam.application.ports.OutboxPort;
import br.ufpr.sept.so2.modules.solicitacoes.application.ports.ProtocoloSequenciaPort;
import br.ufpr.sept.so2.modules.solicitacoes.application.ports.SolicitacaoRepository;
import br.ufpr.sept.so2.modules.solicitacoes.application.ports.TipoSolicitacaoRepository;
import br.ufpr.sept.so2.modules.solicitacoes.domain.Protocolo;
import br.ufpr.sept.so2.modules.solicitacoes.domain.Solicitacao;
import br.ufpr.sept.so2.modules.solicitacoes.domain.TipoSolicitacao;
import br.ufpr.sept.so2.modules.solicitacoes.domain.WorkflowDefinicao;
import br.ufpr.sept.so2.shared.domain.exception.DadoInvalidoException;
import br.ufpr.sept.so2.shared.domain.exception.RecursoNaoEncontradoException;
import br.ufpr.sept.so2.shared.infrastructure.Uuids;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.Map;
import java.util.UUID;

@Service
public class CriarSolicitacaoUseCase {

    private static final ZoneId FUSO_SEPT = ZoneId.of("America/Sao_Paulo");

    private final TipoSolicitacaoRepository tipoRepository;
    private final SolicitacaoRepository solicitacaoRepository;
    private final ProtocoloSequenciaPort protocoloSequenciaPort;
    private final FormSchemaValidator formSchemaValidator;
    private final WorkflowJsonParser workflowJsonParser;
    private final OutboxPort outboxPort;
    private final AuditLogPort auditLogPort;
    private final ObjectMapper objectMapper;

    public CriarSolicitacaoUseCase(
            TipoSolicitacaoRepository tipoRepository,
            SolicitacaoRepository solicitacaoRepository,
            ProtocoloSequenciaPort protocoloSequenciaPort,
            FormSchemaValidator formSchemaValidator,
            WorkflowJsonParser workflowJsonParser,
            OutboxPort outboxPort,
            AuditLogPort auditLogPort,
            ObjectMapper objectMapper
    ) {
        this.tipoRepository = tipoRepository;
        this.solicitacaoRepository = solicitacaoRepository;
        this.protocoloSequenciaPort = protocoloSequenciaPort;
        this.formSchemaValidator = formSchemaValidator;
        this.workflowJsonParser = workflowJsonParser;
        this.outboxPort = outboxPort;
        this.auditLogPort = auditLogPort;
        this.objectMapper = objectMapper;
    }

    @Transactional
    public Solicitacao execute(UUID solicitanteId, String tipoCodigo, Map<String, Object> payload, String ip) {
        TipoSolicitacao tipo = tipoRepository.findByCodigo(tipoCodigo)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Tipo de solicitação não encontrado."));
        if (!tipo.isPublished()) {
            throw new RecursoNaoEncontradoException("Tipo de solicitação não encontrado.");
        }
        formSchemaValidator.validar(tipo.getFormSchema(), payload);
        WorkflowDefinicao workflow = workflowJsonParser.parse(tipo.getWorkflowJson());
        OffsetDateTime agora = OffsetDateTime.now();
        int ano = agora.atZoneSameInstant(FUSO_SEPT).getYear();
        Protocolo protocolo = protocoloSequenciaPort.proximo(ano);
        Solicitacao solicitacao = Solicitacao.abrir(
                Uuids.v7(),
                Uuids.v7(),
                tipo,
                solicitanteId,
                protocolo,
                toJson(payload),
                workflow,
                agora
        );
        Solicitacao persistida = solicitacaoRepository.save(solicitacao);
        String evento = toJson(Map.of(
                "solicitacaoId", persistida.getId().toString(),
                "protocolo", persistida.getProtocolo().getValor(),
                "tipoCodigo", persistida.getTipoCodigo(),
                "solicitanteId", solicitanteId.toString()
        ));
        outboxPort.enqueue("solicitacao.criada", evento);
        auditLogPort.append("solicitacao.criada", solicitanteId, evento, ip);
        return persistida;
    }

    private String toJson(Object valor) {
        try {
            return objectMapper.writeValueAsString(valor);
        } catch (JsonProcessingException ex) {
            throw new DadoInvalidoException("Não foi possível serializar o formulário.");
        }
    }
}
