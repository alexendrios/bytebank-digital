# 💻 ByteBank Digital — Frontend

SPA em **Angular 20** (standalone components + Signals) que consome a API do
`backend/`, implementada conforme a especificação em [`../docs/07-frontend.md`](../docs/07-frontend.md).

## Rodando localmente

Pré-requisitos: Node.js 20+ e o backend rodando em `http://localhost:8080`
(veja o `README.md` na raiz do projeto — `docker-compose up -d postgres backend`
é suficiente para desenvolver só o frontend).

```bash
cd frontend
npm install
npm start
# App disponível em http://localhost:4200
```

## Build de produção

```bash
npm run build:prod
# Saída em dist/bytebank-digital-frontend/browser
```

## Rodando com Docker

```bash
# a partir da raiz do projeto
docker-compose up -d --build
# Frontend: http://localhost:4200 (Nginx, com /api em proxy reverso para o backend)
```

## Estrutura

```text
src/app/
├── core/         # SessionService, guards de rota, interceptors HTTP
├── shared/       # pipes, componentes visuais reaproveitáveis, utilitários
├── layouts/      # PublicLayout (login/cadastro) e PrivateLayout (app autenticado)
├── components/   # Navbar e Sidebar usados pelo PrivateLayout
├── services/     # clientes HTTP (Auth, Conta, Usuario, Transferencia)
├── models/       # interfaces TypeScript espelhando os DTOs do backend
└── pages/        # telas roteadas (login, dashboard, extrato, admin, ...)
```

## Decisões de implementação

- **Autenticação**: `authInterceptor` anexa o `Bearer` token; `errorInterceptor`
  captura `401`, renova via `POST /auth/refresh-token` e reenvia a requisição
  original (com deduplicação de refreshes concorrentes).
- **Perfil do usuário**: o JWT não carrega `perfil` e não há endpoint `/me`.
  O `AuthService` infere o perfil logo após login/cadastro tentando
  `GET /usuarios` (200 → ADMIN, 403 → CLIENTE) — ver comentários em
  `core/services/session.service.ts`.
- **Edição do próprio perfil (Cliente)**: a API só expõe `PUT /usuarios/{id}`
  para ADMIN, então a tela `/app/perfil` mostra os dados em modo leitura para
  clientes, com uma nota explicando a limitação (ver `docs/07-frontend.md`).
- **Transferências**: o campo de conta destino pede o *ID* (UUID) da conta, não
  o número — é o que `POST /transferencias` espera; não existe endpoint de
  busca de conta por número para uma "busca por chave" mais amigável.
- **Erros da API**: `shared/utils/api-error.util.ts` mapeia o formato `ApiError`
  estruturado do backend (`message` + `details[]`) para mensagens legíveis.
