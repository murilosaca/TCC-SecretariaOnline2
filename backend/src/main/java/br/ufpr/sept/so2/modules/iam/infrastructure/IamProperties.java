package br.ufpr.sept.so2.modules.iam.infrastructure;

import br.ufpr.sept.so2.modules.iam.application.IamSettings;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.iam")
public class IamProperties implements IamSettings {

    private long accessTtlSeconds = 900;
    private long refreshTtlSeconds = 604800;
    private long resetTtlSeconds = 86400;
    private String cookieName = "so2_refresh";
    private boolean cookieSecure = false;
    private String cookieSameSite = "Lax";
    private String issuer = "so2";
    private String audience = "so2-api";
    private String resetAudience = "password-reset";
    private String frontendBaseUrl = "http://localhost:5173";
    private String jwtPrivateKeyPem = "";
    private String jwtPublicKeyPem = "";
    private int maxFalhasConsecutivas = 10;
    private int minutosBloqueio = 15;
    private int loginPorMinuto = 5;
    private int recuperarPorHora = 3;
    private final Argon2 argon2 = new Argon2();
    private final Seed seed = new Seed();

    @Override
    public long accessTtlSeconds() {
        return accessTtlSeconds;
    }

    @Override
    public long refreshTtlSeconds() {
        return refreshTtlSeconds;
    }

    @Override
    public long resetTtlSeconds() {
        return resetTtlSeconds;
    }

    @Override
    public int maxFalhasConsecutivas() {
        return maxFalhasConsecutivas;
    }

    @Override
    public int minutosBloqueio() {
        return minutosBloqueio;
    }

    @Override
    public String frontendBaseUrl() {
        return frontendBaseUrl;
    }

    public long getAccessTtlSeconds() {
        return accessTtlSeconds;
    }

    public void setAccessTtlSeconds(long accessTtlSeconds) {
        this.accessTtlSeconds = accessTtlSeconds;
    }

    public long getRefreshTtlSeconds() {
        return refreshTtlSeconds;
    }

    public void setRefreshTtlSeconds(long refreshTtlSeconds) {
        this.refreshTtlSeconds = refreshTtlSeconds;
    }

    public long getResetTtlSeconds() {
        return resetTtlSeconds;
    }

    public void setResetTtlSeconds(long resetTtlSeconds) {
        this.resetTtlSeconds = resetTtlSeconds;
    }

    public String getCookieName() {
        return cookieName;
    }

    public void setCookieName(String cookieName) {
        this.cookieName = cookieName;
    }

    public boolean isCookieSecure() {
        return cookieSecure;
    }

    public void setCookieSecure(boolean cookieSecure) {
        this.cookieSecure = cookieSecure;
    }

    public String getCookieSameSite() {
        return cookieSameSite;
    }

    public void setCookieSameSite(String cookieSameSite) {
        this.cookieSameSite = cookieSameSite;
    }

    public String getIssuer() {
        return issuer;
    }

    public void setIssuer(String issuer) {
        this.issuer = issuer;
    }

    public String getAudience() {
        return audience;
    }

    public void setAudience(String audience) {
        this.audience = audience;
    }

    public String getResetAudience() {
        return resetAudience;
    }

    public void setResetAudience(String resetAudience) {
        this.resetAudience = resetAudience;
    }

    public String getFrontendBaseUrl() {
        return frontendBaseUrl;
    }

    public void setFrontendBaseUrl(String frontendBaseUrl) {
        this.frontendBaseUrl = frontendBaseUrl;
    }

    public String getJwtPrivateKeyPem() {
        return jwtPrivateKeyPem;
    }

    public void setJwtPrivateKeyPem(String jwtPrivateKeyPem) {
        this.jwtPrivateKeyPem = jwtPrivateKeyPem;
    }

    public String getJwtPublicKeyPem() {
        return jwtPublicKeyPem;
    }

    public void setJwtPublicKeyPem(String jwtPublicKeyPem) {
        this.jwtPublicKeyPem = jwtPublicKeyPem;
    }

    public int getMaxFalhasConsecutivas() {
        return maxFalhasConsecutivas;
    }

    public void setMaxFalhasConsecutivas(int maxFalhasConsecutivas) {
        this.maxFalhasConsecutivas = maxFalhasConsecutivas;
    }

    public int getMinutosBloqueio() {
        return minutosBloqueio;
    }

    public void setMinutosBloqueio(int minutosBloqueio) {
        this.minutosBloqueio = minutosBloqueio;
    }

    public int getLoginPorMinuto() {
        return loginPorMinuto;
    }

    public void setLoginPorMinuto(int loginPorMinuto) {
        this.loginPorMinuto = loginPorMinuto;
    }

    public int getRecuperarPorHora() {
        return recuperarPorHora;
    }

    public void setRecuperarPorHora(int recuperarPorHora) {
        this.recuperarPorHora = recuperarPorHora;
    }

    public Argon2 getArgon2() {
        return argon2;
    }

    public Seed getSeed() {
        return seed;
    }

    public static class Argon2 {
        private int saltLength = 16;
        private int hashLength = 32;
        private int parallelism = 1;
        private int memoryKb = 48128;
        private int iterations = 1;

        public int getSaltLength() {
            return saltLength;
        }

        public void setSaltLength(int saltLength) {
            this.saltLength = saltLength;
        }

        public int getHashLength() {
            return hashLength;
        }

        public void setHashLength(int hashLength) {
            this.hashLength = hashLength;
        }

        public int getParallelism() {
            return parallelism;
        }

        public void setParallelism(int parallelism) {
            this.parallelism = parallelism;
        }

        public int getMemoryKb() {
            return memoryKb;
        }

        public void setMemoryKb(int memoryKb) {
            this.memoryKb = memoryKb;
        }

        public int getIterations() {
            return iterations;
        }

        public void setIterations(int iterations) {
            this.iterations = iterations;
        }
    }

    public static class Seed {
        private boolean enabled = false;
        private String password = "";

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }

        public String getPassword() {
            return password;
        }

        public void setPassword(String password) {
            this.password = password;
        }
    }
}
