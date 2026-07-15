package com.bytebank.karate;

import com.bytebank.integration.AbstractIntegrationTest;
import com.intuit.karate.junit5.Karate;

class KarateTest extends AbstractIntegrationTest {

    @Karate.Test
    Karate testAll() {
        return Karate.run("classpath:karate")
                .systemProperty("karate.port", String.valueOf(port));
    }
}