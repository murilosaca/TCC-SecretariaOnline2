package br.ufpr.sept.so2.modules.publico.api;

import br.ufpr.sept.so2.shared.config.ContatoProperties;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/publico")
@Tag(name = "Público", description = "Endpoints sem autenticação (RF-F0-004)")
public class PublicoController {

    private final ContatoProperties contatoProperties;

    public PublicoController(ContatoProperties contatoProperties) {
        this.contatoProperties = contatoProperties;
    }

    @GetMapping("/contato")
    @Operation(summary = "Informações de contato da secretaria")
    public ContatoResponse contato() {
        return new ContatoResponse(
                contatoProperties.getNome(),
                contatoProperties.getEndereco(),
                contatoProperties.getTelefone(),
                contatoProperties.getEmail(),
                contatoProperties.getHorario()
        );
    }

    public record ContatoResponse(
            String nome,
            String endereco,
            String telefone,
            String email,
            String horario
    ) {
    }
}
