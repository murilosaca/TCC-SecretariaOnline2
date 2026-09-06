package br.ufpr.sept.so2.modules.iam.infrastructure.persistence;

import br.ufpr.sept.so2.modules.iam.domain.Usuario;
import br.ufpr.sept.so2.shared.domain.valueobject.Email;
import br.ufpr.sept.so2.shared.domain.valueobject.Grr;
import br.ufpr.sept.so2.shared.infrastructure.persistence.BaseEntity;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Table;

import java.time.OffsetDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "usuario")
public class UsuarioJpaEntity extends BaseEntity {

    @Column(name = "email_institucional", nullable = false, unique = true, columnDefinition = "citext")
    private String emailInstitucional;

    @Column(name = "email_pessoal", unique = true, columnDefinition = "citext")
    private String emailPessoal;

    @Column(unique = true, length = 11)
    private String grr;

    @Column(name = "senha_hash", nullable = false, length = 255)
    private String senhaHash;

    @Column(name = "senha_alterada", nullable = false)
    private boolean senhaAlterada;

    @Column(name = "lgpd_aceite_em")
    private OffsetDateTime lgpdAceiteEm;

    @Column(name = "lgpd_aceite_ip", length = 64)
    private String lgpdAceiteIp;

    @Column(name = "lgpd_aceite_user_agent", length = 300)
    private String lgpdAceiteUserAgent;

    @Column(nullable = false)
    private boolean ativo;

    @Column(name = "falhas_consecutivas", nullable = false)
    private int falhasConsecutivas;

    @Column(name = "bloqueado_ate")
    private OffsetDateTime bloqueadoAte;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "usuario_authority", joinColumns = @JoinColumn(name = "usuario_id"))
    @Column(name = "authority", nullable = false, length = 80)
    private Set<String> authorities = new HashSet<>();

    protected UsuarioJpaEntity() {
    }

    public static UsuarioJpaEntity fromDomain(Usuario usuario) {
        UsuarioJpaEntity entity = new UsuarioJpaEntity();
        entity.setId(usuario.getId());
        entity.merge(usuario);
        return entity;
    }

    public void merge(Usuario usuario) {
        this.emailInstitucional = usuario.getEmailInstitucional().getValue();
        this.emailPessoal = usuario.getEmailPessoal() == null ? null : usuario.getEmailPessoal().getValue();
        this.grr = usuario.getGrr() == null ? null : usuario.getGrr().getValue();
        this.senhaHash = usuario.getSenhaHash();
        this.senhaAlterada = usuario.isSenhaAlterada();
        this.lgpdAceiteEm = usuario.getLgpdAceiteEm();
        this.lgpdAceiteIp = usuario.getLgpdAceiteIp();
        this.lgpdAceiteUserAgent = usuario.getLgpdAceiteUserAgent();
        this.ativo = usuario.isAtivo();
        this.falhasConsecutivas = usuario.getFalhasConsecutivas();
        this.bloqueadoAte = usuario.getBloqueadoAte();
        this.authorities = new HashSet<>(usuario.getAuthorities());
    }

    public Usuario toDomain() {
        return new Usuario(
                getId(),
                Email.of(emailInstitucional),
                emailPessoal == null ? null : Email.of(emailPessoal),
                grr == null ? null : Grr.of(grr),
                senhaHash,
                senhaAlterada,
                lgpdAceiteEm,
                lgpdAceiteIp,
                lgpdAceiteUserAgent,
                ativo,
                falhasConsecutivas,
                bloqueadoAte,
                authorities.stream().sorted().toList(),
                getCreatedAt(),
                getUpdatedAt()
        );
    }
}
