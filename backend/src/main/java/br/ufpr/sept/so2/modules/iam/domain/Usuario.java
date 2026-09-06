package br.ufpr.sept.so2.modules.iam.domain;

import br.ufpr.sept.so2.shared.domain.exception.ConflitoEstadoException;
import br.ufpr.sept.so2.shared.domain.exception.DadoInvalidoException;
import br.ufpr.sept.so2.shared.domain.valueobject.Email;
import br.ufpr.sept.so2.shared.domain.valueobject.Grr;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class Usuario {

    private final UUID id;
    private Email emailInstitucional;
    private Email emailPessoal;
    private Grr grr;
    private String senhaHash;
    private boolean senhaAlterada;
    private OffsetDateTime lgpdAceiteEm;
    private String lgpdAceiteIp;
    private String lgpdAceiteUserAgent;
    private boolean ativo;
    private int falhasConsecutivas;
    private OffsetDateTime bloqueadoAte;
    private List<String> authorities;
    private final OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;

    public Usuario(
            UUID id,
            Email emailInstitucional,
            Email emailPessoal,
            Grr grr,
            String senhaHash,
            boolean senhaAlterada,
            OffsetDateTime lgpdAceiteEm,
            String lgpdAceiteIp,
            String lgpdAceiteUserAgent,
            boolean ativo,
            int falhasConsecutivas,
            OffsetDateTime bloqueadoAte,
            List<String> authorities,
            OffsetDateTime createdAt,
            OffsetDateTime updatedAt
    ) {
        this.id = id;
        this.emailInstitucional = emailInstitucional;
        this.emailPessoal = emailPessoal;
        this.grr = grr;
        this.senhaHash = senhaHash;
        this.senhaAlterada = senhaAlterada;
        this.lgpdAceiteEm = lgpdAceiteEm;
        this.lgpdAceiteIp = lgpdAceiteIp;
        this.lgpdAceiteUserAgent = lgpdAceiteUserAgent;
        this.ativo = ativo;
        this.falhasConsecutivas = falhasConsecutivas;
        this.bloqueadoAte = bloqueadoAte;
        this.authorities = authorities == null ? new ArrayList<>() : new ArrayList<>(authorities);
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public void liberarBloqueioSeExpirado(OffsetDateTime agora) {
        if (bloqueadoAte != null && !bloqueadoAte.isAfter(agora)) {
            bloqueadoAte = null;
            falhasConsecutivas = 0;
            this.updatedAt = agora;
        }
    }

    public boolean estaBloqueado(OffsetDateTime agora) {
        return bloqueadoAte != null && bloqueadoAte.isAfter(agora);
    }

    public boolean registrarFalha(OffsetDateTime agora, int maxFalhas, int minutosBloqueio) {
        falhasConsecutivas++;
        boolean bloqueou = falhasConsecutivas >= maxFalhas;
        if (bloqueou) {
            bloqueadoAte = agora.plusMinutes(minutosBloqueio);
        }
        this.updatedAt = agora;
        return bloqueou;
    }

    public void registrarLoginOk(OffsetDateTime agora) {
        falhasConsecutivas = 0;
        bloqueadoAte = null;
        this.updatedAt = agora;
    }

    public void completarPrimeiroAcesso(String novoHash, OffsetDateTime agora, String ip, String userAgent) {
        if (senhaAlterada) {
            throw new ConflitoEstadoException("O primeiro acesso já foi concluído.");
        }
        this.senhaHash = novoHash;
        this.senhaAlterada = true;
        this.lgpdAceiteEm = agora;
        this.lgpdAceiteIp = ip;
        this.lgpdAceiteUserAgent = truncar(userAgent, 300);
        this.updatedAt = agora;
    }

    public void redefinirSenha(String novoHash, OffsetDateTime agora) {
        if (novoHash == null || novoHash.isBlank()) {
            throw new DadoInvalidoException("Hash de senha obrigatório.");
        }
        this.senhaHash = novoHash;
        this.senhaAlterada = true;
        this.falhasConsecutivas = 0;
        this.bloqueadoAte = null;
        this.updatedAt = agora;
    }

    public boolean precisaPrimeiroAcesso() {
        return !senhaAlterada;
    }

    public boolean concederAuthorities(List<String> novas, OffsetDateTime agora) {
        boolean mudou = false;
        for (String authority : novas) {
            if (authority != null && !authority.isBlank() && !authorities.contains(authority)) {
                authorities.add(authority);
                mudou = true;
            }
        }
        if (mudou) {
            this.updatedAt = agora;
        }
        return mudou;
    }

    private static String truncar(String valor, int max) {
        if (valor == null) {
            return null;
        }
        return valor.length() <= max ? valor : valor.substring(0, max);
    }

    public UUID getId() {
        return id;
    }

    public Email getEmailInstitucional() {
        return emailInstitucional;
    }

    public Email getEmailPessoal() {
        return emailPessoal;
    }

    public Grr getGrr() {
        return grr;
    }

    public String getSenhaHash() {
        return senhaHash;
    }

    public boolean isSenhaAlterada() {
        return senhaAlterada;
    }

    public OffsetDateTime getLgpdAceiteEm() {
        return lgpdAceiteEm;
    }

    public String getLgpdAceiteIp() {
        return lgpdAceiteIp;
    }

    public String getLgpdAceiteUserAgent() {
        return lgpdAceiteUserAgent;
    }

    public boolean isAtivo() {
        return ativo;
    }

    public int getFalhasConsecutivas() {
        return falhasConsecutivas;
    }

    public OffsetDateTime getBloqueadoAte() {
        return bloqueadoAte;
    }

    public List<String> getAuthorities() {
        return List.copyOf(authorities);
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }

    public OffsetDateTime getUpdatedAt() {
        return updatedAt;
    }
}
