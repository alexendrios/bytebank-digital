# 07. Frontend

> ⏳ **Status: planejado, ainda não implementado.** Este documento descreve o desenho pretendido para o frontend, servindo de especificação para quando a implementação começar.

## Stack planejada

| Item | Escolha |
|---|---|
| Framework | Angular 20 |
| UI Components | Angular Material |
| Estilização | SCSS modularizado |
| Gerenciamento de estado | Signals (nativo do Angular), com RxJS para fluxos assíncronos (HTTP, WebSockets futuros) |

## Estrutura de pastas planejada

```text
frontend/src/app/
├── core/         # Guards, Interceptors e serviços globais (Singleton)
├── shared/       # Componentes visuais, pipes e diretivas reaproveitáveis
├── pages/        # Views principais roteadas (Login, Dashboard, Perfil)
├── components/   # Componentes internos de apoio (Navbar, Sidebar)
├── services/     # Clientes HTTP para consumo da API do ByteBank
├── models/       # Interfaces e classes de modelos de dados TypeScript
└── layouts/      # Templates de estrutura de página (Autenticado vs Público)
```

## Telas planejadas

| Área | Tela | Endpoint(s) da API consumidos |
|---|---|---|
| Pública | Login | `POST /auth/login` |
| Pública | Cadastro | `POST /auth/register` |
| Cliente | Dashboard financeiro | `GET /contas` |
| Cliente | Detalhes da conta | `GET /contas/{id}` |
| Cliente | Depósito / Saque | `POST /contas/{id}/deposito`, `POST /contas/{id}/saque` |
| Cliente | Transferência | `POST /transferencias` |
| Cliente | Extrato | `GET /contas/{id}/extrato` |
| Cliente | Edição de perfil | *(endpoint de auto-atualização de usuário ainda não existe na API — hoje `PUT /usuarios/{id}` é restrito a ADMIN; será necessário decidir se o cliente edita o próprio perfil por um endpoint dedicado ou se isso fica só para ADMIN)* |
| Admin | Administração do sistema / lista de usuários | `GET /usuarios`, `POST /usuarios`, `PUT /usuarios/{id}`, `DELETE /usuarios/{id}` |
| Admin | Lista global de contas | `GET /contas` (ADMIN enxerga todas) |

## Pontos de atenção para quando a implementação começar

1. **Renovação de token**: o Angular precisará de um `HttpInterceptor` que capture 401s, tente renovar via `/auth/refresh-token` e reenvie a requisição original — o backend já expõe o endpoint, falta o consumo.
2. **Guards de rota**: rotas administrativas devem verificar o `perfil` decodificado do JWT (ou mantido em um serviço de sessão) antes mesmo de bater na API, para UX (a API já protege de qualquer forma no backend).
3. **Erros da API**: o formato `ApiError` (ver `06-backend.md`) já é estruturado — o frontend pode mapear `error.details` diretamente para mensagens de validação por campo em formulários.
4. **CORS**: já configurado no backend (`CorsConfig`) para aceitar `http://localhost:4200` por padrão via variável de ambiente `CORS_ALLOWED_ORIGINS`.
