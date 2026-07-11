# 13. DevOps

## Docker

### Dockerfile (multi-stage)

```dockerfile
FROM maven:3.9-eclipse-temurin-21 AS build
# ... build com Maven, cache de dependências separado do código-fonte

FROM eclipse-temurin:21-jre-alpine
# ... runtime enxuto, usuário não-root, healthcheck via /actuator/health
```

Duas etapas: a primeira compila com Maven (imagem pesada, mas descartada ao final), a segunda roda apenas o JRE + o JAR final — resultando em uma imagem final bem menor. O container roda com um usuário não-root (`bytebank`) por segurança.

### docker-compose.yml

Orquestra dois serviços hoje (um terceiro, `frontend`, está comentado no arquivo, pronto para quando o Angular existir):

| Serviço | Papel |
|---|---|
| `postgres` | Banco de dados, com `healthcheck` via `pg_isready` |
| `backend` | API Spring Boot, aguarda o Postgres estar saudável (`depends_on: condition: service_healthy`) antes de subir |

```bash
cp .env.example .env
docker-compose up -d --build
```

## CI/CD — GitHub Actions

Workflow: `.github/workflows/backend-ci.yml`, com dois jobs:

### Job `build-and-test`

Roda em todo push/PR na `main` que altere algo em `backend/**`:

1. Checkout do código.
2. Setup do JDK 21 (Temurin), com cache de dependências Maven.
3. `mvn clean compile`.
4. `mvn test` — inclui os testes de integração via Testcontainers; o runner `ubuntu-latest` já vem com Docker instalado, então isso funciona sem configuração adicional.
5. Publicação do relatório JUnit diretamente na aba de checks do commit/PR.
6. Upload dos `surefire-reports` como artefato, apenas em caso de falha (facilita debugar sem precisar reproduzir localmente).
7. `mvn package` (gera o JAR final) e upload como artefato.

### Job `docker-build-and-push`

Roda apenas em push na `main` (não em PRs) e apenas se os testes passarem (`needs: build-and-test`):

1. Login no GHCR (GitHub Container Registry) usando o `GITHUB_TOKEN` automático — não requer nenhum secret adicional.
2. Build da imagem a partir do `backend/Dockerfile`.
3. Push para `ghcr.io/<owner>/<repo>/backend`, com tags `latest` e o SHA curto do commit.

> Requer que o repositório tenha "Read and write permissions" habilitado para o `GITHUB_TOKEN` em Settings → Actions → General → Workflow permissions — repositórios novos costumam vir apenas com leitura por padrão.

## Variáveis de ambiente

Ver `.env.example` na raiz do repositório para a lista completa. As principais:

| Variável | Uso |
|---|---|
| `DB_NAME` / `DB_USER` / `DB_PASSWORD` | Credenciais do Postgres |
| `JWT_SECRET` | Chave de assinatura dos tokens — trocar em produção |
| `CORS_ALLOWED_ORIGINS` | Origem(ns) permitida(s) para o frontend |
| `ADMIN_BOOTSTRAP_*` | Credenciais do ADMIN criado automaticamente na primeira subida (ver 09-seguranca.md) |

## Próximos passos de DevOps (não implementados)

- Deploy automatizado (ex.: para um ambiente de staging) após o merge na main.
- Observabilidade via Prometheus + Grafana (o Actuator já expõe /actuator/prometheus; falta o scraping e os dashboards).
- Gestão de segredos via um vault dedicado, em vez de variáveis de ambiente simples.
