package br.ufpr.sept.so2.shared.config

import br.ufpr.sept.so2.modules.iam.application.ports.JwtTokenService
import br.ufpr.sept.so2.modules.iam.application.ports.UsuarioRepository
import br.ufpr.sept.so2.modules.iam.infrastructure.IamProperties
import br.ufpr.sept.so2.modules.iam.infrastructure.security.AuthRateLimitFilter
import br.ufpr.sept.so2.modules.iam.infrastructure.security.FirstAccessGateFilter
import br.ufpr.sept.so2.modules.iam.infrastructure.security.JwtAuthenticationFilter
import br.ufpr.sept.so2.modules.presenca.infrastructure.EventoProperties
import br.ufpr.sept.so2.shared.api.ProblemResponses
import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.boot.web.servlet.FilterRegistrationBean
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import org.springframework.security.config.Customizer
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter
import org.springframework.security.web.header.writers.ReferrerPolicyHeaderWriter
import org.springframework.web.cors.CorsConfiguration
import org.springframework.web.cors.CorsConfigurationSource
import org.springframework.web.cors.UrlBasedCorsConfigurationSource

@Configuration
@EnableMethodSecurity
@EnableConfigurationProperties(
    CorsProperties::class,
    ContatoProperties::class,
    IamProperties::class,
    EventoProperties::class,
)
class SecurityConfig(
    private val corsProperties: CorsProperties,
    private val problemResponses: ProblemResponses,
) {

    @Bean
    fun jwtAuthenticationFilter(jwtTokenService: JwtTokenService): JwtAuthenticationFilter =
        JwtAuthenticationFilter(jwtTokenService, problemResponses)

    @Bean
    fun firstAccessGateFilter(usuarioRepository: UsuarioRepository): FirstAccessGateFilter =
        FirstAccessGateFilter(usuarioRepository, problemResponses)

    @Bean
    fun authRateLimitFilter(iamProperties: IamProperties, objectMapper: ObjectMapper): AuthRateLimitFilter =
        AuthRateLimitFilter(iamProperties, problemResponses, objectMapper)

    @Bean
    fun disableJwtFilterRegistration(filter: JwtAuthenticationFilter): FilterRegistrationBean<JwtAuthenticationFilter> {
        val registration = FilterRegistrationBean(filter)
        registration.isEnabled = false
        return registration
    }

    @Bean
    fun disableFirstAccessFilterRegistration(
        filter: FirstAccessGateFilter,
    ): FilterRegistrationBean<FirstAccessGateFilter> {
        val registration = FilterRegistrationBean(filter)
        registration.isEnabled = false
        return registration
    }

    @Bean
    fun disableRateLimitFilterRegistration(
        filter: AuthRateLimitFilter,
    ): FilterRegistrationBean<AuthRateLimitFilter> {
        val registration = FilterRegistrationBean(filter)
        registration.isEnabled = false
        return registration
    }

    @Bean
    fun securityFilterChain(
        http: HttpSecurity,
        jwtAuthenticationFilter: JwtAuthenticationFilter,
        firstAccessGateFilter: FirstAccessGateFilter,
        authRateLimitFilter: AuthRateLimitFilter,
    ): SecurityFilterChain {
        http
            .csrf { it.disable() }
            .cors(Customizer.withDefaults())
            .sessionManagement { session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS) }
            .headers { headers ->
                headers
                    .contentTypeOptions(Customizer.withDefaults())
                    .frameOptions { frame -> frame.deny() }
                    .referrerPolicy { referrer ->
                        referrer.policy(ReferrerPolicyHeaderWriter.ReferrerPolicy.STRICT_ORIGIN_WHEN_CROSS_ORIGIN)
                    }
            }
            .exceptionHandling { ex ->
                ex
                    .authenticationEntryPoint { request, response, _ ->
                        problemResponses.write(
                            request,
                            response,
                            HttpStatus.UNAUTHORIZED,
                            "Não autenticado",
                            "Token inválido ou expirado.",
                            "authentication-required",
                        )
                    }
                    .accessDeniedHandler { request, response, _ ->
                        problemResponses.write(
                            request,
                            response,
                            HttpStatus.FORBIDDEN,
                            "Acesso negado",
                            "Você não tem permissão para esta operação.",
                            "access-denied",
                        )
                    }
            }
            .authorizeHttpRequests { auth ->
                auth
                    .requestMatchers(
                        "/swagger-ui/**",
                        "/swagger-ui.html",
                        "/v3/api-docs/**",
                        "/actuator/health",
                        "/actuator/health/liveness",
                        "/actuator/health/readiness",
                        "/publico/**",
                    ).permitAll()
                    .requestMatchers(
                        "/auth/login",
                        "/auth/refresh",
                        "/auth/recuperar-senha",
                        "/auth/redefinir-senha",
                    ).permitAll()
                    .requestMatchers("/auth/primeiro-acesso", "/auth/me", "/auth/logout").authenticated()
                    .requestMatchers("/request-types", "/request-types/**", "/requests", "/requests/**").authenticated()
                    .requestMatchers("/bff/**").authenticated()
                    .requestMatchers("/events", "/events/**").authenticated()
                    .requestMatchers("/formativas", "/formativas/**").authenticated()
                    .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                    // CRUD acadêmico permanece aberto até o FGAC (F7) ter usuários/capabilities.
                    // Ver docs/auditoria-fundacao.md — plano @PreAuthorize.
                    .anyRequest().permitAll()
            }
            .addFilterBefore(authRateLimitFilter, UsernamePasswordAuthenticationFilter::class.java)
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter::class.java)
            .addFilterAfter(firstAccessGateFilter, JwtAuthenticationFilter::class.java)
        return http.build()
    }

    @Bean
    fun corsConfigurationSource(): CorsConfigurationSource {
        val configuration = CorsConfiguration()
        val origins = corsProperties.allowedOrigins
            .flatMap { value -> value.split(",") }
            .map { it.trim() }
            .filter { it.isNotBlank() }
        configuration.allowedOrigins = origins
        configuration.allowedMethods = listOf("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS")
        configuration.allowedHeaders = listOf("Authorization", "Content-Type", "Accept")
        configuration.allowCredentials = true
        configuration.maxAge = 3600L

        val source = UrlBasedCorsConfigurationSource()
        source.registerCorsConfiguration("/**", configuration)
        return source
    }
}
