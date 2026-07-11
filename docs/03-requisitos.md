# 03. Requisitos

## Requisitos Funcionais

| ID | Descrição | Status |
|---|---|---|
| RF01 | Usuário deve poder se registrar informando nome, e-mail e senha | ✅ |
| RF02 | Usuário deve poder autenticar-se e receber access token + refresh token | ✅ |
| RF03 | Usuário deve poder renovar o access token usando o refresh token | ✅ |
| RF04 | ADMIN deve poder listar, consultar, criar, atualizar e remover usuários | ✅ |
| RF05 | Usuário autenticado deve poder criar uma conta bancária | ✅ |
| RF06 | Usuário deve poder consultar, atualizar e remover **apenas as próprias contas** (ADMIN pode acessar todas) | ✅ |
| RF07 | Usuário deve poder depositar em uma conta de sua propriedade | ✅ |
| RF08 | Usuário deve poder sacar de uma conta de sua propriedade, respeitando o saldo disponível | ✅ |
| RF09 | Usuário deve poder transferir valores de uma conta própria para qualquer outra conta existente | ✅ |
| RF10 | Usuário deve poder consultar o extrato paginado de uma conta própria | ✅ |
| RF11 | Sistema deve criar automaticamente um ADMIN inicial na primeira subida, caso nenhum exista | ✅ |
| RF12 | Sistema deve simular transações PIX por chave | ⏳ Planejado |
| RF13 | Sistema deve emitir notificações em tempo real de transações | ⏳ Planejado |
| RF14 | Sistema deve oferecer interface web (SPA) para todas as operações acima | ⏳ Planejado |

## Requisitos Não Funcionais

| ID | Descrição | Como é atendido hoje |
|---|---|---|
| RNF01 | Senhas nunca devem ser armazenadas em texto plano | BCrypt (`PasswordEncoder`) |
| RNF02 | API deve ser stateless (sem sessão de servidor) | JWT + `SessionCreationPolicy.STATELESS` |
| RNF03 | Alterações de schema devem ser versionadas e reprodutíveis | Flyway migrations (`V1` a `V4`) |
| RNF04 | Código deve ter cobertura de testes automatizados, incluindo testes de integração com banco real | JUnit 5 + Mockito (unitários) + Testcontainers (integração) — 49 testes |
| RNF05 | Deploy deve ser containerizado e reprodutível entre ambientes | Docker multi-stage build + `docker-compose.yml` |
| RNF06 | Mudanças no repositório devem ser validadas automaticamente antes do merge | GitHub Actions (`backend-ci.yml`) rodando build + testes a cada push/PR |
| RNF07 | API deve ser autodocumentada | OpenAPI 3 / Swagger UI (`/api/swagger-ui.html`) |
| RNF08 | Erros da API devem seguir um formato consistente | `GlobalExceptionHandler` + `ApiError` padronizado |
| RNF09 | Um cliente nunca deve conseguir ler/alterar dados de outro cliente | Checagem de propriedade em `ContaService.validarPropriedade` |

## Restrições técnicas

- Backend: Java 21 + Spring Boot 3.
- Banco de dados: PostgreSQL 16.
- Frontend (planejado): Angular 20.
- Toda infraestrutura local deve subir via `docker-compose up`.
