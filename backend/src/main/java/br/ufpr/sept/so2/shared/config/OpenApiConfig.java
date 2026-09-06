package br.ufpr.sept.so2.shared.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI secretariaOnlineOpenApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("Secretaria Online 2 — API")
                        .version("0.1.0")
                        .description("API REST do SO2 (SEPT/UFPR). Documentação gerada pelo SpringDoc / OpenAPI 3.")
                        .contact(new Contact()
                                .name("SEPT / UFPR")
                                .email("secretaria.sept@ufpr.br"))
                        .license(new License().name("Uso acadêmico — TCC ADS/UFPR")))
                .servers(List.of(new Server().url("/").description("Servidor atual")));
    }
}
