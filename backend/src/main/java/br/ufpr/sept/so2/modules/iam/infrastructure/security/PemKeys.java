package br.ufpr.sept.so2.modules.iam.infrastructure.security;

import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

final class PemKeys {

    private PemKeys() {
    }

    static KeyPair generateRsa2048() {
        try {
            KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
            generator.initialize(2048);
            return generator.generateKeyPair();
        } catch (Exception ex) {
            throw new IllegalStateException("Não foi possível gerar o par RSA", ex);
        }
    }

    static PrivateKey parsePrivate(String pem) {
        try {
            byte[] der = decode(pem, "PRIVATE KEY");
            return KeyFactory.getInstance("RSA").generatePrivate(new PKCS8EncodedKeySpec(der));
        } catch (Exception ex) {
            throw new IllegalStateException("JWT_RSA_PRIVATE_KEY inválida (esperado PKCS#8 PEM)", ex);
        }
    }

    static PublicKey parsePublic(String pem) {
        try {
            byte[] der = decode(pem, "PUBLIC KEY");
            return KeyFactory.getInstance("RSA").generatePublic(new X509EncodedKeySpec(der));
        } catch (Exception ex) {
            throw new IllegalStateException("JWT_RSA_PUBLIC_KEY inválida (esperado X.509 PEM)", ex);
        }
    }

    private static byte[] decode(String pem, String type) {
        String cleaned = pem
                .replace("-----BEGIN " + type + "-----", "")
                .replace("-----END " + type + "-----", "")
                .replaceAll("\\s", "");
        return Base64.getDecoder().decode(cleaned);
    }
}
