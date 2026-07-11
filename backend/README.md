# ByteBank Digital — Backend

API REST do sistema bancário digital, construída em **Java 21 + Spring Boot 3**.

## Pré-requisitos

- JDK 21
- Maven 3.9+ (ou use o wrapper, se adicionado)
- Docker + Docker Compose (para subir o PostgreSQL local)

## Subindo o banco de dados local

Na raiz do projeto (`bytebank-digital/`):

```bash
cp .env.example .env
docker-compose up -d postgres
```

## Rodando a aplicação

```bash
cd backend
./mvnw spring-boot:run
# ou, se preferir usar o Maven instalado localmente:
mvn spring-boot:run
```

A API sobe em `http://localhost:8080/api`, com Swagger UI disponível em:

```
http://localhost:8080/api/swagger-ui.html
```

## Rodando os testes

```bash
mvn clean test
```

> Os testes de integração usam **Testcontainers**, então o Docker precisa estar em execução mesmo para rodar `mvn test` localmente.

## Profiles disponíveis

| Profile | Uso |
|---|---|
| `dev` | Padrão local, aponta para o PostgreSQL do `docker-compose.yml` |
| `prod` | Usado em produção, todas as credenciais vêm de variáveis de ambiente obrigatórias |

## Estrutura de pacotes

Ver `docs/06-backend.md` e o `README.md` principal do repositório para o detalhamento completo da arquitetura em camadas e dos Design Patterns aplicados.

## Status da implementação

- [x] Estrutura base do projeto (Maven, camadas, configs de CORS/OpenAPI, exception handler global)
- [x] Modelo de domínio + migrations Flyway (Usuário, Conta, Transferência, Movimentação)
- [x] Autenticação JWT (login/register/refresh-token)
- [x] CRUD de usuários (ADMIN) e contas (com checagem de propriedade)
- [x] Operações bancárias (depósito, saque, transferência) com Strategy + Factory + Facade
- [x] Extrato / movimentações (paginado)
- [x] Testes unitários (Strategy, Factory, JwtService, Services com Mockito)
- [x] Testes de integração (Auth, Contas/Operações, RBAC) com Testcontainers
- [ ] Frontend Angular
- [ ] Módulos avançados: PIX, cartões, Kafka, Redis, WebSockets, Prometheus/Grafana

## Estrutura de testes

```text
src/test/java/com/bytebank/
├── strategy/              # DepositoStrategyTest, SaqueStrategyTest, TransferenciaStrategyTest
├── factory/               # OperacaoFactoryTest
├── security/              # JwtServiceTest
├── service/                # AuthServiceTest, UsuarioServiceTest, ContaServiceTest, BancoFacadeTest (Mockito)
└── integration/
    ├── AbstractIntegrationTest.java       # Base com Testcontainers (PostgreSQL real)
    ├── AuthIntegrationTest.java           # register/login/refresh-token ponta a ponta
    ├── ContaOperacoesIntegrationTest.java # depósito/saque/transferência/extrato + ownership
    └── UsuarioRbacIntegrationTest.java    # RBAC: /usuarios só ADMIN
```

## Testando a API manualmente

```bash
# 1) Registrar um usuário (perfil CLIENTE por padrão)
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{"nome":"Maria Silva","email":"maria@bytebank.com","senha":"senha1234"}'

# 2) Login (retorna accessToken + refreshToken)
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"maria@bytebank.com","senha":"senha1234"}'

# 3) Criar uma conta para o usuário (troque USUARIO_ID e TOKEN)
curl -X POST http://localhost:8080/api/contas \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer TOKEN" \
  -d '{"agencia":"0001","usuarioId":"USUARIO_ID"}'

# 4) Depositar (troque CONTA_ID)
curl -X POST http://localhost:8080/api/contas/CONTA_ID/deposito \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer TOKEN" \
  -d '{"valor":500.00}'

# 5) Ver extrato
curl http://localhost:8080/api/contas/CONTA_ID/extrato \
  -H "Authorization: Bearer TOKEN"
```

> ⚠️ Para criar o **primeiro usuário ADMIN**: não precisa fazer nada manual. A aplicação cria automaticamente um ADMIN inicial na primeira subida (veja a seção "Usuário ADMIN inicial" abaixo).

## Usuário ADMIN inicial (bootstrap automático)

Como o endpoint `/usuarios` só é acessível por ADMIN, criamos um `AdminBootstrapRunner` que sobe um ADMIN automaticamente na primeira inicialização da aplicação — **apenas se ainda não existir nenhum ADMIN cadastrado**. Depois disso, ele nunca mais altera nada.

Credenciais padrão (configuráveis via `.env` / variáveis de ambiente):

| Variável | Padrão |
|---|---|
| `ADMIN_BOOTSTRAP_ENABLED` | `true` |
| `ADMIN_BOOTSTRAP_EMAIL` | `admin@bytebank.com` |
| `ADMIN_BOOTSTRAP_SENHA` | `admin12345` |

```bash
# Login com o admin criado automaticamente
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"admin@bytebank.com","senha":"admin12345"}'

# Use o accessToken retornado para acessar /usuarios
curl http://localhost:8080/api/usuarios \
  -H "Authorization: Bearer TOKEN_DO_ADMIN"
```

> 🔒 **Altere a senha padrão em produção** (ou desative o bootstrap com `ADMIN_BOOTSTRAP_ENABLED=false` depois de criar seu próprio ADMIN manualmente no banco).
