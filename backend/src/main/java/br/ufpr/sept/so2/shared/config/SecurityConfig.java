package br.ufpr.sept.so2.shared.config;

import br.ufpr.sept.so2.modules.iam.application.ports.JwtTokenService;
import br.ufpr.sept.so2.modules.iam.application.ports.UsuarioRepository;
import br.ufpr.sept.so2.modules.iam.infrastructure.IamProperties;
import br.ufpr.sept.so2.modules.presenca.infrastructure.EventoProperties;
import br.ufpr.sept.so2.modules.iam.infrastructure.security.AuthRateLimitFilter;
import br.ufpr.sept.so2.modules.iam.infrastructure.security.FirstAccessGateFilter;
import br.ufpr.sept.so2.modules.iam.infrastructure.security.JwtAuthenticationFilter;
import br.ufpr.sept.so2.shared.api.ProblemResponses;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.security.web.header.writers.ReferrerPolicyHeaderWriter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

@Configuration
@EnableMethodSecurity
@EnableConfigurationProperties({CorsProperties.class, ContatoProperties.class, IamProperties.class, EventoProperties.class})
public class SecurityConfig {

    private final CorsProperties corsProperties;
    private final ProblemResponses problemResponses;

    public SecurityConfig(CorsProperties corsProperties, ProblemResponses problemResponses) {
        this.corsProperties = corsProperties;
        this.problemResponses = problemResponses;
    }

    @Bean
    public JwtAuthenticationFilter jwtAuthenticationFilter(JwtTokenService jwtTokenService) {
        return new JwtAuthenticationFilter(jwtTokenService, problemResponses);
    }

    @Bean
    public FirstAccessGateFilter firstAccessGateFilter(UsuarioRepository usuarioRepository) {
        return new FirstAccessGateFilter(usuarioRepository, problemResponses);
    }

    @Bean
    public AuthRateLimitFilter authRateLimitFilter(IamProperties iamProperties, ObjectMapper objectMapper) {
        return new AuthRateLimitFilter(iamProperties, problemResponses, objectMapper);
    }

    @Bean
    public FilterRegistrationBean<JwtAuthenticationFilter> disableJwtFilterRegistration(JwtAuthenticationFilter filter) {
        FilterRegistrationBean<JwtAuthenticationFilter> registration = new FilterRegistrationBean<>(filter);
        registration.setEnabled(false);
        return registration;
    }

    @Bean
    public FilterRegistrationBean<FirstAccessGateFilter> disableFirstAccessFilterRegistration(FirstAccessGateFilter filter) {
        FilterRegistrationBean<FirstAccessGateFilter> registration = new FilterRegistrationBean<>(filter);
        registration.setEnabled(false);
        return registration;
    }

    @Bean
    public FilterRegistrationBean<AuthRateLimitFilter> disableRateLimitFilterRegistration(AuthRateLimitFilter filter) {
        FilterRegistrationBean<AuthRateLimitFilter> registration = new FilterRegistrationBean<>(filter);
        registration.setEnabled(false);
        return registration;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http,
            JwtAuthenticationFilter jwtAuthenticationFilter,
            FirstAccessGateFilter firstAccessGateFilter,
            AuthRateLimitFilter authRateLimitFilter
    ) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .cors(Customizer.withDefaults())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .headers(headers -> headers
                        .contentTypeOptions(Customizer.withDefaults())
                        .frameOptions(frame -> frame.deny())
                        .referrerPolicy(referrer -> referrer.policy(
                                ReferrerPolicyHeaderWriter.ReferrerPolicy.STRICT_ORIGIN_WHEN_CROSS_ORIGIN)))
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint((request, response, authException) ->
                                problemResponses.write(
                                        request,
                                        response,
                                        HttpStatus.UNAUTHORIZED,
                                        "Não autenticado",
                                        "Token inválido ou expirado.",
                                        "authentication-required"
                                ))
                        .accessDeniedHandler((request, response, accessDeniedException) ->
                                problemResponses.write(
                                        request,
                                        response,
                                        HttpStatus.FORBIDDEN,
                                        "Acesso negado",
                                        "Você não tem permissão para esta operação.",
                                        "access-denied"
                                )))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                "/swagger-ui/**",
                                "/swagger-ui.html",
                                "/v3/api-docs/**",
                                "/actuator/health",
                                "/actuator/health/liveness",
                                "/actuator/health/readiness",
                                "/publico/**"
                        ).permitAll()
                        .requestMatchers(
                                "/auth/login",
                                "/auth/refresh",
                                "/auth/recuperar-senha",
                                "/auth/redefinir-senha"
                        ).permitAll()
                        .requestMatchers("/auth/primeiro-acesso", "/auth/me", "/auth/logout").authenticated()
                        .requestMatchers("/request-types", "/request-types/**", "/requests", "/requests/**").authenticated()
                        .requestMatchers("/bff/**").authenticated()
                        .requestMatchers("/events", "/events/**").authenticated()
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        // CRUD acadêmico permanece aberto até o FGAC (F7) ter usuários/capabilities.
                        // Ver docs/auditoria-fundacao.md — plano @PreAuthorize.
                        .anyRequest().permitAll())
                .addFilterBefore(authRateLimitFilter, UsernamePasswordAuthenticationFilter.class)
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
                .addFilterAfter(firstAccessGateFilter, JwtAuthenticationFilter.class);
        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        List<String> origins = corsProperties.getAllowedOrigins().stream()
                .flatMap(value -> Arrays.stream(value.split(",")))
                .map(String::trim)
                .filter(origin -> !origin.isBlank())
                .toList();
        configuration.setAllowedOrigins(origins);
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("Authorization", "Content-Type", "Accept"));
        configuration.setAllowCredentials(true);
        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}
