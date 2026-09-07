package br.ufpr.sept.so2.shared.config

import io.swagger.v3.oas.models.Components
import io.swagger.v3.oas.models.OpenAPI
import io.swagger.v3.oas.models.info.Contact
import io.swagger.v3.oas.models.info.Info
import io.swagger.v3.oas.models.info.License
import io.swagger.v3.oas.models.security.SecurityScheme
import io.swagger.v3.oas.models.servers.Server
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class OpenApiConfig {

    @Bean
    fun secretariaOnlineOpenApi(): OpenAPI =
        OpenAPI()
            .info(
                Info()
                    .title("Secretaria Online 2 — API")
                    .version("0.1.0")
                    .description("API REST do SO2 (SEPT/UFPR). Documentação gerada pelo SpringDoc / OpenAPI 3.")
                    .contact(
                        Contact()
                            .name("SEPT / UFPR")
                            .email("secretaria.sept@ufpr.br"),
                    )
                    .license(License().name("Uso acadêmico — TCC ADS/UFPR")),
            )
            .servers(listOf(Server().url("/").description("Servidor atual")))
            .components(
                Components().addSecuritySchemes(
                    "bearer-jwt",
                    SecurityScheme()
                        .type(SecurityScheme.Type.HTTP)
                        .scheme("bearer")
                        .bearerFormat("JWT")
                        .description("Access token RS256 em memória. Refresh via cookie httpOnly so2_refresh."),
                ),
            )
}
