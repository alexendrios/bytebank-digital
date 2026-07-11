# 12. Estratégia de Testes

## Pirâmide de testes do projeto

- **35 testes unitários** (Strategy, Factory, JwtService, Services) — rápidos, sem I/O, rodam em milissegundos.
- **14 testes de integração** (fluxo de auth, operações bancárias, RBAC) — sobem o contexto Spring completo + um PostgreSQL real via Testcontainers.
- **Total: 49 testes**, todos passando em CI a cada push.

## Testes unitários (Mockito)

Isolam completamente a unidade sob teste, mockando todas as dependências.

| Classe testada | O que é validado |
|---|---|
| `DepositoStrategyTest`, `SaqueStrategyTest`, `TransferenciaStrategyTest` | Regras de cálculo de saldo, saldo insuficiente, saque exato do saldo disponível, contas iguais na transferência |
| `OperacaoFactoryTest` | Resolução correta por tipo; exceção para tipo sem strategy registrada |
| `JwtServiceTest` | Geração/validação de access e refresh token, token de outro usuário, expiração |
| `AuthServiceTest`, `UsuarioServiceTest`, `ContaServiceTest`, `BancoFacadeTest` | Regras de negócio de cada service, incluindo a checagem de propriedade de conta via `mockStatic(SecurityUtils.class)` |

## Testes de integração (Testcontainers)

Usam um PostgreSQL real (não H2, não mocks) para validar o comportamento de ponta a ponta: Controller → Security → Service → Repository → banco.

| Classe | Cenários cobertos |
|---|---|
| `AuthIntegrationTest` | Registro, login, refresh token, e-mail duplicado (409), senha errada (401), endpoint protegido sem token (401) |
| `ContaOperacoesIntegrationTest` | Depósito, saque sem saldo (422), transferência entre contas, cliente tentando mexer em conta alheia (403), extrato após depósito |
| `UsuarioRbacIntegrationTest` | `/usuarios` acessível só por ADMIN |

### Padrão "Singleton Container"

O container PostgreSQL é iniciado **uma única vez por execução da suíte**, em um bloco estático de `AbstractIntegrationTest`, deliberadamente **sem** as anotações `@Testcontainers`/`@Container`:

```java
static {
    POSTGRES = new PostgreSQLContainer<>("postgres:16-alpine")...;
    POSTGRES.start();
    // sem stop() explícito — o Ryuk encerra ao final da JVM
}
```

**Por que não usar `@Testcontainers` + `@Container` (a forma "padrão")?** Descobrimos, durante o desenvolvimento deste projeto, que deixar o JUnit gerenciar o ciclo de vida do container por classe de teste causa um problema sutil: cada classe reinicia o container em uma porta nova, mas o Spring Test Framework **reaproveita o `ApplicationContext` em cache** entre classes com a mesma configuração — resultando em testes tentando conectar a uma porta de container já encerrado. Subir o container uma única vez, fora do ciclo de vida do JUnit, elimina esse problema por completo e ainda acelera a suíte (o contexto Spring também é reaproveitado entre as classes).

### Outro problema real encontrado: duplo prefixo de URL

O `TestRestTemplate`, quando usado com `@SpringBootTest(webEnvironment = RANDOM_PORT)`, já inclui automaticamente o `server.servlet.context-path` (`/api`) na URL raiz. Escrever `restTemplate.postForEntity("/api/auth/login", ...)` gera, na prática, uma chamada para `/api/api/auth/login` — uma rota inexistente. O sintoma era enganoso: parecia falha de autenticação (401) ou erro de servidor (500), mas a causa era só a URL duplicada nos próprios testes. Lição: ao usar `TestRestTemplate` com `context-path` configurado, os paths devem ser escritos **sem** o prefixo do context-path.

### Um terceiro problema: bug do JDK com `HttpURLConnection`

Um teste de "senha incorreta" (POST com corpo esperando 401) lançava `HttpRetryException: cannot retry due to server authentication, in streaming mode` — um bug conhecido do `HttpURLConnection` padrão do JDK ao lidar com respostas 401 em requisições com corpo em modo streaming. Esse erro no cliente de teste, inclusive, deixava a conexão *keep-alive* subjacente em estado inconsistente, afetando testes subsequentes que reaproveitavam a mesma conexão TCP.

## Rodando os testes

```bash
cd backend
mvn clean test
```

> Requer Docker em execução (para os testes de integração via Testcontainers), mesmo ao rodar localmente fora do CI.

## CI

Ver `13-devops.md` — os mesmos 49 testes rodam automaticamente a cada push/PR via GitHub Actions, em um runner que já vem com Docker disponível.
