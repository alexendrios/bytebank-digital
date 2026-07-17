import { Given, When, Then } from '@badeball/cypress-cucumber-preprocessor';
import { parseMoeda } from '../../support/moeda';
import { ClienteComConta } from '../../support/commands';

Given('que sou um cliente autenticado com uma conta de origem com saldo de {string}', (saldoStr: string) => {
  const saldoInicial = parseMoeda(saldoStr);
  cy.criarClienteComConta({ saldoInicial }).as('clienteOrigem');
  cy.get<ClienteComConta>('@clienteOrigem').then((cliente) => {
    cy.login(cliente.email, cliente.senha);
  });
});

Given('que existe uma conta de destino de outro cliente', () => {
  cy.criarClienteComConta({ saldoInicial: 0 }).as('clienteDestino');
});

When('eu transfiro o valor de {string} para a conta de destino', (valorStr: string) => {
  const valor = parseMoeda(valorStr);
  cy.get<ClienteComConta>('@clienteDestino').then((destino) => {
    // A conta de origem já vem pré-selecionada pelo componente quando o
    // cliente tem uma única conta (ver TransferenciaPage.ngOnInit).
    cy.get('[data-cy=input-conta-destino]').clear().type(destino.conta.id);
    cy.get('[data-cy=input-valor-transferencia]').clear().type(String(valor));
    cy.get('[data-cy=botao-confirmar-transferencia]').click();
  });
});

Then('devo ver uma mensagem de erro na transferência', () => {
  cy.get('[data-cy=erro-transferencia]').should('be.visible').and('not.have.text', '');
});

/**
 * Verificado diretamente na API (via token de ADMIN) em vez de na UI: uma
 * transferência bem-sucedida redireciona para o dashboard (lista de contas),
 * enquanto uma rejeitada permanece na tela de transferência — checar o saldo
 * pela API evita acoplar este step a qual das duas telas está ativa.
 */
Then('o saldo da conta de origem deve ser/permanecer {string}', (valorStr: string) => {
  cy.get<ClienteComConta>('@clienteOrigem').then((origem) => {
    cy.apiLoginAdmin().then((tokenAdmin) => {
      cy.request({
        method: 'GET',
        url: `${Cypress.env('apiUrl')}/contas/${origem.conta.id}`,
        headers: { Authorization: `Bearer ${tokenAdmin}` }
      }).then((res) => {
        expect(res.body.saldo).to.be.closeTo(parseMoeda(valorStr), 0.001);
      });
    });
  });
});
