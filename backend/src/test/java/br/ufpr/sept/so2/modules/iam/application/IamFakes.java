package br.ufpr.sept.so2.modules.iam.application;

import br.ufpr.sept.so2.modules.iam.application.ports.AuditLogPort;
import br.ufpr.sept.so2.modules.iam.application.ports.JtiBlacklistRepository;
import br.ufpr.sept.so2.modules.iam.application.ports.JwtTokenService;
import br.ufpr.sept.so2.modules.iam.application.ports.OpaqueTokenHasher;
import br.ufpr.sept.so2.modules.iam.application.ports.OutboxPort;
import br.ufpr.sept.so2.modules.iam.application.ports.PasswordHasher;
import br.ufpr.sept.so2.modules.iam.application.ports.RefreshTokenRepository;
import br.ufpr.sept.so2.modules.iam.application.ports.SenhaHistoricoRepository;
import br.ufpr.sept.so2.modules.iam.application.ports.UsuarioRepository;
import br.ufpr.sept.so2.modules.iam.domain.IdentificadorLogin;
import br.ufpr.sept.so2.modules.iam.domain.RefreshSessao;
import br.ufpr.sept.so2.modules.iam.domain.Usuario;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

final class IamFakes {

    private IamFakes() {
    }

    static final class Settings implements IamSettings {
        @Override
        public long accessTtlSeconds() {
            return 900;
        }

        @Override
        public long refreshTtlSeconds() {
            return 604800;
        }

        @Override
        public long resetTtlSeconds() {
            return 86400;
        }

        @Override
        public int maxFalhasConsecutivas() {
            return 10;
        }

        @Override
        public int minutosBloqueio() {
            return 15;
        }

        @Override
        public String frontendBaseUrl() {
            return "http://localhost:5173";
        }
    }

    static final class Hasher implements PasswordHasher {
        int dummyCalls;

        @Override
        public String hash(String raw) {
            return "h:" + raw;
        }

        @Override
        public boolean matches(String raw, String hash) {
            return ("h:" + raw).equals(hash);
        }

        @Override
        public void matchesDummy() {
            dummyCalls++;
        }
    }

    static final class TokenHasher implements OpaqueTokenHasher {
        @Override
        public String hash(String rawToken) {
            return "th:" + rawToken;
        }
    }

    static final class Jwt implements JwtTokenService {
        String lastReset = "reset.jwt";

        @Override
        public String emitAccessToken(Usuario usuario) {
            return "access:" + usuario.getId() + ":" + usuario.precisaPrimeiroAcesso();
        }

        @Override
        public String emitResetToken(Usuario usuario) {
            lastReset = "reset:" + usuario.getId();
            return lastReset;
        }

        @Override
        public AccessTokenClaims parseAccessToken(String token) {
            throw new UnsupportedOperationException();
        }

        @Override
        public ResetTokenClaims parseResetToken(String token) {
            if (token == null || !token.startsWith("reset:")) {
                throw new IllegalArgumentException("jwt");
            }
            return new ResetTokenClaims(UUID.fromString(token.substring("reset:".length())), "jti-1");
        }
    }

    static final class Usuarios implements UsuarioRepository {
        final Map<UUID, Usuario> byId = new HashMap<>();

        @Override
        public Usuario save(Usuario usuario) {
            byId.put(usuario.getId(), usuario);
            return usuario;
        }

        @Override
        public Optional<Usuario> findById(UUID id) {
            return Optional.ofNullable(byId.get(id));
        }

        @Override
        public Optional<Usuario> findByIdentificador(IdentificadorLogin identificador) {
            return byId.values().stream().filter(usuario -> corresponde(usuario, identificador)).findFirst();
        }

        @Override
        public Optional<Usuario> findByEmail(String email) {
            return byId.values().stream()
                    .filter(usuario -> email.equals(usuario.getEmailInstitucional().getValue())
                            || (usuario.getEmailPessoal() != null && email.equals(usuario.getEmailPessoal().getValue())))
                    .findFirst();
        }

        private static boolean corresponde(Usuario usuario, IdentificadorLogin identificador) {
            if (identificador.getTipo() == IdentificadorLogin.Tipo.GRR) {
                return usuario.getGrr() != null && identificador.getValor().equals(usuario.getGrr().getValue());
            }
            return identificador.getValor().equals(usuario.getEmailInstitucional().getValue())
                    || (usuario.getEmailPessoal() != null
                    && identificador.getValor().equals(usuario.getEmailPessoal().getValue()));
        }
    }

    static final class RefreshTokens implements RefreshTokenRepository {
        final Map<String, RefreshSessao> byHash = new HashMap<>();

        @Override
        public RefreshSessao save(RefreshSessao sessao) {
            byHash.put(sessao.getTokenHash(), sessao);
            return sessao;
        }

        @Override
        public Optional<RefreshSessao> lockByTokenHash(String tokenHash) {
            return Optional.ofNullable(byHash.get(tokenHash));
        }

        @Override
        public void revokeAllByUsuarioId(UUID usuarioId) {
            byHash.values().stream()
                    .filter(sessao -> sessao.getUsuarioId().equals(usuarioId))
                    .forEach(sessao -> sessao.revogar(OffsetDateTime.now()));
        }
    }

    static final class Historico implements SenhaHistoricoRepository {
        final Map<UUID, List<String>> hashes = new HashMap<>();

        @Override
        public void append(UUID usuarioId, String senhaHash) {
            hashes.computeIfAbsent(usuarioId, ignored -> new ArrayList<>()).add(senhaHash);
        }

        @Override
        public List<String> findLastHashes(UUID usuarioId, int limite) {
            List<String> lista = hashes.getOrDefault(usuarioId, List.of());
            int from = Math.max(0, lista.size() - limite);
            return lista.subList(from, lista.size());
        }
    }

    static final class Jtis implements JtiBlacklistRepository {
        final Map<String, OffsetDateTime> itens = new HashMap<>();

        @Override
        public void add(String jti, OffsetDateTime expiresAt) {
            itens.put(jti, expiresAt);
        }

        @Override
        public boolean contains(String jti) {
            return itens.containsKey(jti);
        }
    }

    static final class Outbox implements OutboxPort {
        final List<String> tipos = new ArrayList<>();
        final List<String> payloads = new ArrayList<>();

        @Override
        public void enqueue(String tipo, String payload) {
            tipos.add(tipo);
            payloads.add(payload);
        }
    }

    static final class Audit implements AuditLogPort {
        final List<String> tipos = new ArrayList<>();

        @Override
        public void append(String tipo, UUID atorId, String payload, String ip) {
            tipos.add(tipo);
        }
    }
}
