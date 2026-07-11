package com.bytebank;

import com.bytebank.integration.AbstractIntegrationTest;
import org.junit.jupiter.api.Test;

/**
 * Smoke test que garante que o contexto Spring sobe corretamente com um
 * PostgreSQL real provisionado via Testcontainers.
 */
class ByteBankApplicationTests extends AbstractIntegrationTest {

    @Test
    void contextLoads() {
        // Verifica que o contexto Spring sobe corretamente com todas as configurações.
    }
}
