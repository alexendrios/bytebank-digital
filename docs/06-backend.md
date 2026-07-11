# 06. Backend

## Stack

| Camada | Tecnologia | Versão |
|---|---|---|
| Linguagem | Java | 21 |
| Framework | Spring Boot | 3.3.4 |
| Persistência | Spring Data JPA / Hibernate | 6.5.3 |
| Migrations | Flyway | (via Spring Boot BOM) |
| Segurança | Spring Security + JJWT | jjwt 0.12.6 |
| Mapeamento DTO ↔ Entity | MapStruct | 1.6.2 |
| Documentação | springdoc-openapi | 2.6.0 |
| Testes | JUnit 5, Mockito, Testcontainers | — |

## Organização de pacotes

```text
com.bytebank/
├── config/         # CorsConfig, OpenApiConfig, AdminBootstrapRunner/Properties
├── controller/     # AuthController, UsuarioController, ContaController, TransferenciaController
├── dto/
│   ├── request/    # RegisterRequest, LoginRequest, ContaRequest, TransferenciaRequest, ...
│   └── response/    # AuthResponse, UsuarioResponse, ContaResponse, MovimentacaoResponse, ...
├── entity/         # Usuario, Conta, Movimentacao, Transferencia, Perfil, TipoMovimentacao
├── exception/      # GlobalExceptionHandler, ApiError, BusinessException, ResourceNotFoundException, ConflictException
├── factory/        # OperacaoFactory
├── mapper/         # UsuarioMapper, ContaMapper, MovimentacaoMapper, TransferenciaMapper (MapStruct)
├── repository/     # UsuarioRepository, ContaRepository, MovimentacaoRepository, TransferenciaRepository
├── security/       # JwtService, JwtAuthenticationFilter, SecurityConfig, UserDetailsServiceImpl, SecurityUtils
├── service/        # AuthService, UsuarioService, ContaService, BancoFacade
├── strategy/       # OperacaoStrategy, DepositoStrategy, SaqueStrategy, TransferenciaStrategy
└── util/           # NumeroContaGenerator
```

## Ciclo de vida de uma operação bancária (exemplo: depósito)

1. `ContaController.depositar()` recebe `POST /contas/{id}/deposito` e delega para `BancoFacade.depositar(contaId, valor)`.
2. `BancoFacade` busca a conta via `ContaService.buscarEntidadePorId` e valida propriedade via `ContaService.validarPropriedade`.
3. `OperacaoFactory.resolver(TipoMovimentacao.DEPOSITO)` retorna a `DepositoStrategy`.
4. A strategy calcula o novo saldo e monta o objeto `Movimentacao` (ainda em memória).
5. `BancoFacade` persiste a conta atualizada e a movimentação, dentro de uma única transação (`@Transactional`).
6. O resultado é mapeado para `ContaResponse` via MapStruct e devolvido ao cliente.

## Tratamento de erros

Toda exceção lançada por qualquer camada é convertida pelo `GlobalExceptionHandler` em um `ApiError` consistente:

```json
{
  "timestamp": "2026-07-11T10:00:00Z",
  "status": 422,
  "error": "Unprocessable Entity",
  "message": "Saldo insuficiente para realizar o saque",
  "path": "/contas/.../saque",
  "details": []
}
```

| Exceção | Status HTTP |
|---|---|
| `ResourceNotFoundException` | 404 |
| `BusinessException` (ex.: saldo insuficiente) | 422 |
| `ConflictException` (ex.: e-mail duplicado) | 409 |
| `BadCredentialsException` | 401 |
| `io.jsonwebtoken.JwtException` | 401 |
| `AccessDeniedException` | 403 |
| `MethodArgumentNotValidException` (Bean Validation) | 400, com `details` listando campo a campo |
| Qualquer outra exceção não mapeada | 500 |

## Observabilidade básica

O Actuator expõe `/actuator/health` e `/actuator/prometheus` (públicos, fora da autenticação JWT), preparando terreno para uma futura integração com Prometheus/Grafana (ver `13-devops.md` e a seção de módulos avançados em `02-visao-geral.md`).

## Documentação interativa da API

Com a aplicação rodando, o Swagger UI fica disponível em `/api/swagger-ui.html`, já configurado com suporte a Bearer JWT (`OpenApiConfig`) — é possível autenticar e testar todos os endpoints diretamente pela interface.
