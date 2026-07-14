package com.bytebank.karate;

import com.bytebank.integration.AbstractIntegrationTest;
import com.intuit.karate.junit5.Karate;

class KarateRunner extends AbstractIntegrationTest {

    @Karate.Test
    Karate testAuth() {
        return Karate.run("classpath:karate/auth.feature")
                .systemProperty("karate.port", String.valueOf(port));
    }

    @Karate.Test
    Karate testOperacoes() {
        return Karate.run("classpath:karate/operacoes.feature")
                .systemProperty("karate.port", String.valueOf(port));
    }
}
