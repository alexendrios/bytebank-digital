package com.bytebank.integration;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.util.StringUtils;
import org.testcontainers.containers.PostgreSQLContainer;
/**
 * Classe base para testes de integração ponta a ponta.
 *
 * IMPORTANTE: o container Postgres é iniciado uma ÚNICA VEZ, em um bloco
 * estático, e NUNCA é anotado com {@code @Container}/{@code @Testcontainers}.
 * Isso é proposital (padrão "Singleton Container"): se deixarmos o JUnit
 * gerenciar o ciclo de vida por classe de teste, cada subclasse reinicia o
 * container em uma porta nova, mas o Spring reaproveita o
 * {@code ApplicationContext} em cache entre classes com a mesma configuração
 * — resultando em testes tentando conectar numa porta de container já
 * encerrado. Subir uma vez só evita essa inconsistência e também acelera a
 * suíte, já que o container e o contexto Spring são compartilhados entre
 * todas as classes de teste de integração.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
public abstract class AbstractIntegrationTest {

    protected static final PostgreSQLContainer<?> POSTGRES;
    private static final String DATASOURCE_URL = System.getenv("SPRING_DATASOURCE_URL");
    private static final String DATASOURCE_USERNAME = System.getenv().getOrDefault("SPRING_DATASOURCE_USERNAME", "bytebank");
    private static final String DATASOURCE_PASSWORD = System.getenv().getOrDefault("SPRING_DATASOURCE_PASSWORD", "bytebank");

    static {
        if (StringUtils.hasText(DATASOURCE_URL)) {
            POSTGRES = null;
        } else {
            POSTGRES = new PostgreSQLContainer<>("postgres:16-alpine")
                    .withDatabaseName("bytebank_test")
                    .withUsername("bytebank")
                    .withPassword("bytebank")
                    .withReuse(false);
            POSTGRES.start();
            // Sem stop() explícito: o Ryuk (resource reaper do Testcontainers)
            // encerra o container automaticamente ao final da JVM de testes.
        }
    }

    @DynamicPropertySource
    static void datasourceProperties(DynamicPropertyRegistry registry) {
        if (StringUtils.hasText(DATASOURCE_URL)) {
            registry.add("spring.datasource.url", () -> DATASOURCE_URL);
            registry.add("spring.datasource.username", () -> DATASOURCE_USERNAME);
            registry.add("spring.datasource.password", () -> DATASOURCE_PASSWORD);
            return;
        }

        registry.add("spring.datasource.url", POSTGRES::getJdbcUrl);
        registry.add("spring.datasource.username", POSTGRES::getUsername);
        registry.add("spring.datasource.password", POSTGRES::getPassword);
    }

    @Autowired
    protected TestRestTemplate restTemplate;

    @LocalServerPort
    protected int port;
}
