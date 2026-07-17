import { Given, When, Then } from '@badeball/cypress-cucumber-preprocessor';
import { parseMoeda } from '../../support/moeda';
import { ClienteComConta } from '../../support/commands';

/** Mapeia o rótulo visível no menu para o slug usado no atributo data-cy (ver components/sidebar/sidebar.ts). */
const SLUG_POR_OPCAO: Record<string, string> = {
  Dashboard: 'dashboard',
  'Depositar/Sacar': 'movimentar',
  Transferir: 'transferencia',
  'Meu perfil': 'perfil'
};

Given('que sou um cliente autenticado com uma conta com saldo de {string}', (saldoStr: string) => {
  const saldoInicial = parseMoeda(saldoStr);
  cy.criarClienteComConta({ saldoInicial }).as('cliente');
  cy.get<ClienteComConta>('@cliente').then((cliente) => {
    cy.login(cliente.email, cliente.senha);
  });
});

When('eu acesso a opção {string} pelo menu', (opcao: string) => {
  cy.get(`[data-cy=nav-${SLUG_POR_OPCAO[opcao]}]`).click();
});

Then('o menu lateral deve exibir a opção {string}', (opcao: string) => {
  cy.get(`[data-cy=nav-${SLUG_POR_OPCAO[opcao]}]`).should('be.visible').and('contain.text', opcao);
});

Then('devo ver a mensagem de sucesso {string}', (mensagem: string) => {
  cy.contains('.bb-snackbar-success', mensagem).should('be.visible');
});
