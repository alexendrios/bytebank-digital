package com.bytebank.util;

import java.security.SecureRandom;

/**
 * Gera números de conta no formato NNNNNN-D (6 dígitos + dígito verificador),
 * suficiente para fins de simulação bancária deste projeto.
 */
public final class NumeroContaGenerator {

    private static final SecureRandom RANDOM = new SecureRandom();

    private NumeroContaGenerator() {
    }

    public static String gerar() {
        int base = 100000 + RANDOM.nextInt(900000); // 6 dígitos
        int digitoVerificador = base % 9;
        return base + "-" + digitoVerificador;
    }
}
