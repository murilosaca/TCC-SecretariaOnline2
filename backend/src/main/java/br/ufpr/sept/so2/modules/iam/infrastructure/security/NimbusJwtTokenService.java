package br.ufpr.sept.so2.modules.iam.infrastructure.security;

import br.ufpr.sept.so2.modules.iam.application.ports.JwtTokenService;
import br.ufpr.sept.so2.modules.iam.domain.Usuario;
import br.ufpr.sept.so2.modules.iam.infrastructure.IamProperties;
import com.nimbusds.jose.JOSEObjectType;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.crypto.RSASSASigner;
import com.nimbusds.jose.crypto.RSASSAVerifier;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.security.KeyPair;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.UUID;

@Component
public class NimbusJwtTokenService implements JwtTokenService {

    private static final Logger LOG = LoggerFactory.getLogger(NimbusJwtTokenService.class);

    private final IamProperties properties;
    private final RSAPrivateKey privateKey;
    private final RSAPublicKey publicKey;

    public NimbusJwtTokenService(IamProperties properties) {
        this.properties = properties;
        KeyPair pair = resolveKeyPair(properties);
        this.privateKey = (RSAPrivateKey) pair.getPrivate();
        this.publicKey = (RSAPublicKey) pair.getPublic();
    }

    @Override
    public String emitAccessToken(Usuario usuario) {
        Instant agora = Instant.now();
        JWTClaimsSet claims = new JWTClaimsSet.Builder()
                .subject(usuario.getId().toString())
                .issuer(properties.getIssuer())
                .audience(properties.getAudience())
                .jwtID(UUID.randomUUID().toString())
                .issueTime(Date.from(agora))
                .expirationTime(Date.from(agora.plusSeconds(properties.accessTtlSeconds())))
                .claim("authorities", usuario.getAuthorities())
                .claim("mustChangePassword", usuario.precisaPrimeiroAcesso())
                .build();
        return sign(claims);
    }

    @Override
    public String emitResetToken(Usuario usuario) {
        Instant agora = Instant.now();
        JWTClaimsSet claims = new JWTClaimsSet.Builder()
                .subject(usuario.getId().toString())
                .issuer(properties.getIssuer())
                .audience(properties.getResetAudience())
                .jwtID(UUID.randomUUID().toString())
                .issueTime(Date.from(agora))
                .expirationTime(Date.from(agora.plusSeconds(properties.resetTtlSeconds())))
                .build();
        return sign(claims);
    }

    @Override
    public AccessTokenClaims parseAccessToken(String token) {
        JWTClaimsSet claims = parse(token, properties.getAudience());
        List<String> authorities = readAuthorities(claims);
        boolean mustChange = Boolean.TRUE.equals(claims.getClaim("mustChangePassword"));
        return new AccessTokenClaims(
                UUID.fromString(claims.getSubject()),
                authorities,
                mustChange,
                claims.getJWTID()
        );
    }

    @Override
    public ResetTokenClaims parseResetToken(String token) {
        JWTClaimsSet claims = parse(token, properties.getResetAudience());
        return new ResetTokenClaims(UUID.fromString(claims.getSubject()), claims.getJWTID());
    }

    private String sign(JWTClaimsSet claims) {
        try {
            JWSHeader header = new JWSHeader.Builder(JWSAlgorithm.RS256)
                    .type(JOSEObjectType.JWT)
                    .keyID("so2-iam")
                    .build();
            SignedJWT jwt = new SignedJWT(header, claims);
            jwt.sign(new RSASSASigner(privateKey));
            return jwt.serialize();
        } catch (Exception ex) {
            throw new IllegalStateException("Falha ao assinar JWT", ex);
        }
    }

    private JWTClaimsSet parse(String token, String audienceEsperada) {
        try {
            SignedJWT jwt = SignedJWT.parse(token);
            if (!jwt.verify(new RSASSAVerifier(publicKey))) {
                throw new IllegalArgumentException("assinatura");
            }
            JWTClaimsSet claims = jwt.getJWTClaimsSet();
            Date exp = claims.getExpirationTime();
            if (exp == null || exp.before(new Date())) {
                throw new IllegalArgumentException("expirado");
            }
            if (claims.getAudience() == null || !claims.getAudience().contains(audienceEsperada)) {
                throw new IllegalArgumentException("audience");
            }
            if (claims.getSubject() == null || claims.getJWTID() == null) {
                throw new IllegalArgumentException("claims");
            }
            return claims;
        } catch (IllegalArgumentException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new IllegalArgumentException("JWT inválido", ex);
        }
    }

    @SuppressWarnings("unchecked")
    private static List<String> readAuthorities(JWTClaimsSet claims) {
        Object raw = claims.getClaim("authorities");
        if (raw instanceof List<?> list) {
            return list.stream().map(String::valueOf).toList();
        }
        return List.of();
    }

    private static KeyPair resolveKeyPair(IamProperties properties) {
        String privatePem = properties.getJwtPrivateKeyPem();
        String publicPem = properties.getJwtPublicKeyPem();
        if (privatePem != null && !privatePem.isBlank() && publicPem != null && !publicPem.isBlank()) {
            return new KeyPair(PemKeys.parsePublic(publicPem), PemKeys.parsePrivate(privatePem));
        }
        LOG.warn("Par RSA JWT ausente nas variáveis de ambiente; gerando chave efêmera (dev/test).");
        return PemKeys.generateRsa2048();
    }
}
