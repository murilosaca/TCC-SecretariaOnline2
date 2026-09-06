package br.ufpr.sept.so2.modules.iam.infrastructure.security;

import br.ufpr.sept.so2.modules.iam.infrastructure.IamProperties;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Component
public class RefreshCookieFactory {

    private final IamProperties properties;

    public RefreshCookieFactory(IamProperties properties) {
        this.properties = properties;
    }

    public ResponseCookie create(String refreshToken) {
        return base()
                .value(refreshToken)
                .maxAge(Duration.ofSeconds(properties.refreshTtlSeconds()))
                .build();
    }

    public ResponseCookie clear() {
        return base()
                .value("")
                .maxAge(Duration.ZERO)
                .build();
    }

    public String cookieName() {
        return properties.getCookieName();
    }

    private ResponseCookie.ResponseCookieBuilder base() {
        return ResponseCookie.from(properties.getCookieName(), "")
                .httpOnly(true)
                .secure(properties.isCookieSecure())
                .sameSite(properties.getCookieSameSite())
                .path("/auth");
    }
}
