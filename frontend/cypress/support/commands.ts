/// <reference types="cypress" />

/**
 * Comandos customizados usados pelos steps de BDD.
 *
 * Estratégia: sempre que possível, a MASSA DE DADOS (usuário cliente, conta,
 * saldo inicial) é criada por chamadas diretas à API (`cy.request`), e não
 * pela UI — só a ação que está de fato sendo testada (depósito, saque,
 * transferência) passa pela interface. Isso deixa os cenários rápidos e
 * deterministas, evitando que um teste de "saque" dependa da tela de
 * cadastro/login estar 100% funcional.
 */

export interface ContaApi {
  id: string;
  numero: string;
  agencia: string;
  saldo: number;
  usuarioId: string;
  usuarioNome: string;
}

export interface ClienteComConta {
  nome: string;
  email: string;
  senha: string;
  usuarioId: string;
  conta: ContaApi;
}

const apiUrl = (): string => Cypress.env('apiUrl');

function apiLogin(email: string, senha: string): Cypress.Chainable<string> {
  return cy
    .request('POST', `${apiUrl()}/auth/login`, { email, senha })
    .then((res) => res.body.accessToken as string);
}

Cypress.Commands.add('apiLoginAdmin', () => {
  return apiLogin(Cypress.env('adminEmail'), Cypress.env('adminSenha'));
});

Cypress.Commands.add('apiRegistrarCliente', (nome: string, email: string, senha: string) => {
  return cy
    .request('POST', `${apiUrl()}/auth/register`, { nome, email, senha })
    .then((res) => res.body.accessToken as string);
});

Cypress.Commands.add('apiBuscarUsuarioIdPorEmail', (email: string, tokenAdmin: string) => {
  return cy
    .request({
      method: 'GET',
      url: `${apiUrl()}/usuarios`,
      headers: { Authorization: `Bearer ${tokenAdmin}` }
    })
    .then((res) => {
      const usuario = (res.body as Array<{ id: string; email: string }>).find((u) => u.email === email);
      if (!usuario) {
        throw new Error(`Usuário com e-mail ${email} não encontrado via /usuarios`);
      }
      return usuario.id;
    });
});

Cypress.Commands.add('apiCriarConta', (usuarioId: string, tokenAdmin: string, agencia = '0001') => {
  return cy
    .request({
      method: 'POST',
      url: `${apiUrl()}/contas`,
      headers: { Authorization: `Bearer ${tokenAdmin}` },
      body: { agencia, usuarioId }
    })
    .then((res) => res.body as ContaApi);
});

Cypress.Commands.add('apiDepositar', (contaId: string, valor: number, token: string) => {
  return cy
    .request({
      method: 'POST',
      url: `${apiUrl()}/contas/${contaId}/deposito`,
      headers: { Authorization: `Bearer ${token}` },
      body: { valor }
    })
    .then((res) => res.body as ContaApi);
});

/**
 * Cria um cliente novo (e-mail único gerado automaticamente), com uma conta
 * já aberta e, opcionalmente, um saldo inicial já depositado. Tudo via API.
 */
Cypress.Commands.add(
  'criarClienteComConta',
  (opts: { nome?: string; saldoInicial?: number } = {}) => {
    const sufixo = `${Date.now()}-${Math.floor(Math.random() * 100000)}`;
    const nome = opts.nome ?? 'Cliente Teste';
    const email = `cliente.${sufixo}@bytebank.com`;
    const senha = 'senha1234';
    const saldoInicial = opts.saldoInicial ?? 0;

    let tokenAdmin: string;
    let tokenCliente: string;

    return cy
      .apiLoginAdmin()
      .then((token) => {
        tokenAdmin = token;
        return cy.apiRegistrarCliente(nome, email, senha);
      })
      .then((token) => {
        tokenCliente = token;
        return cy.apiBuscarUsuarioIdPorEmail(email, tokenAdmin);
      })
      .then((usuarioId) => cy.apiCriarConta(usuarioId, tokenAdmin).then((conta) => ({ usuarioId, conta })))
      .then(({ usuarioId, conta }) => {
        if (saldoInicial > 0) {
          return cy.apiDepositar(conta.id, saldoInicial, tokenCliente).then((contaAtualizada) => ({
            nome,
            email,
            senha,
            usuarioId,
            conta: contaAtualizada
          }));
        }
        return { nome, email, senha, usuarioId, conta };
      });
  }
);

/** Faz login pela UI (usado pela própria funcionalidade sob teste). */
Cypress.Commands.add('login', (email: string, senha: string) => {
  cy.visit('/login');
  cy.get('[data-cy=input-email]').clear().type(email);
  cy.get('[data-cy=input-senha]').clear().type(senha);
  cy.get('[data-cy=botao-entrar]').click();
  cy.url().should('include', '/app/dashboard');
});

declare global {
  // eslint-disable-next-line @typescript-eslint/no-namespace
  namespace Cypress {
    interface Chainable {
      apiLoginAdmin(): Chainable<string>;
      apiRegistrarCliente(nome: string, email: string, senha: string): Chainable<string>;
      apiBuscarUsuarioIdPorEmail(email: string, tokenAdmin: string): Chainable<string>;
      apiCriarConta(usuarioId: string, tokenAdmin: string, agencia?: string): Chainable<ContaApi>;
      apiDepositar(contaId: string, valor: number, token: string): Chainable<ContaApi>;
      criarClienteComConta(opts?: { nome?: string; saldoInicial?: number }): Chainable<ClienteComConta>;
      login(email: string, senha: string): Chainable<void>;
    }
  }
}
