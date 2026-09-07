package br.ufpr.sept.so2.modules.presenca.application;

import java.security.SecureRandom;

public final class PinPresenca {

    private static final SecureRandom RANDOM = new SecureRandom();

    private PinPresenca() {
    }

    public static String gerar() {
        return String.format("%06d", RANDOM.nextInt(1_000_000));
    }
}
