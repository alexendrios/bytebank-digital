# 10. API

> Referência completa dos endpoints. Para testar interativamente, use o Swagger UI em `/api/swagger-ui.html` (a aplicação já vem com suporte a Bearer JWT configurado na interface).

Todos os paths abaixo são relativos ao context-path `/api` (ex.: o endpoint de login é, na prática, `POST /api/auth/login`).

## Autenticação (`/auth`) — pública

### `POST /auth/register`

```json
// Request
{
  "nome": "Maria Silva",
  "email": "maria@bytebank.com",
  "senha": "senha1234"
}
```
```json
// Response 201
{
  "accessToken": "eyJ...",
  "refreshToken": "eyJ...",
  "tokenType": "Bearer"
}
```
Cria o usuário sempre com perfil `CLIENTE`. `409 Conflict` se o e-mail já existir.

### `POST /auth/login`

```json
// Request
{ "email": "maria@bytebank.com", "senha": "senha1234" }
```
`200 OK` com o mesmo formato de `AuthResponse` acima. `401 Unauthorized` para credenciais inválidas.

### `POST /auth/refresh-token`

```json
// Request
{ "refreshToken": "eyJ..." }
```
`200 OK` com novo par de tokens. `401 Unauthorized` se o token não for um refresh token válido (expirado, malformado, ou é na verdade um access token).

## Usuários (`/usuarios`) — restrito a ADMIN

| Método | Endpoint | Body | Resposta |
|---|---|---|---|
| `GET` | `/usuarios` | — | `200` lista de `UsuarioResponse` |
| `GET` | `/usuarios/{id}` | — | `200` `UsuarioResponse` / `404` |
| `POST` | `/usuarios` | `UsuarioRequest` (nome, email, senha, perfil) | `201` `UsuarioResponse` / `409` e-mail duplicado |
| `PUT` | `/usuarios/{id}` | `UsuarioRequest` (senha opcional — se omitida, mantém a atual) | `200` `UsuarioResponse` |
| `DELETE` | `/usuarios/{id}` | — | `204` |

## Contas (`/contas`) — autenticado; CLIENTE só acessa as próprias

| Método | Endpoint | Body | Resposta |
|---|---|---|---|
| `GET` | `/contas` | — | `200` — ADMIN vê todas, CLIENTE vê só as suas |
| `GET` | `/contas/{id}` | — | `200` / `403` se não for o dono / `404` |
| `POST` | `/contas` | `{ "agencia": "0001", "usuarioId": "..." }` | `201` `ContaResponse` (número gerado automaticamente) |
| `PUT` | `/contas/{id}` | `ContaRequest` | `200` / `403` |
| `DELETE` | `/contas/{id}` | — | `204` / `403` |

### `POST /contas/{id}/deposito`

```json
{ "valor": 500.00 }
```
`200 OK` com `ContaResponse` (saldo atualizado). `403` se a conta não for do usuário autenticado.

### `POST /contas/{id}/saque`

```json
{ "valor": 100.00 }
```
`200 OK` / `422 Unprocessable Entity` se saldo insuficiente / `403` se não for o dono.

### `GET /contas/{id}/extrato?page=0&size=20`

`200 OK` com uma página (`Page<MovimentacaoResponse>`) ordenada da movimentação mais recente para a mais antiga.

## Transferências (`/transferencias`)

### `POST /transferencias`

```json
{
  "contaOrigemId": "...",
  "contaDestinoId": "...",
  "valor": 150.00
}
```
`201 Created` com `TransferenciaResponse`. A conta de origem **deve** pertencer ao usuário autenticado (`403` caso contrário); a conta de destino pode ser de qualquer titular — é assim que uma transferência para terceiros funciona. `422` se saldo insuficiente ou se origem == destino.

## Formato padrão de erro

Toda resposta de erro (4xx/5xx) segue este formato, produzido pelo `GlobalExceptionHandler`:

```json
{
  "timestamp": "2026-07-11T10:00:00Z",
  "status": 422,
  "error": "Unprocessable Entity",
  "message": "Saldo insuficiente para realizar o saque",
  "path": "/contas/3fa85f64-.../saque",
  "details": []
}
```

Para erros de validação de campo (`400`), `details` é preenchido com uma entrada por campo inválido, no formato `"campo: mensagem"`.
