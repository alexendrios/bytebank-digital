import { When, Then } from '@badeball/cypress-cucumber-preprocessor';
import { parseMoeda } from '../../support/moeda';

function abrirAbaDeposito(): void {
  cy.contains('[role=tab]', 'Depósito').click();
}

When('deposito o valor de {string}', (valorStr: string) => {
  const valor = parseMoeda(valorStr);
  abrirAbaDeposito();
  cy.get('[data-cy=input-valor-deposito]').clear().type(String(valor));
  cy.get('[data-cy=botao-confirmar-deposito]').click();
});

When('tento depositar o valor de {string}', (valorStr: string) => {
  const valor = parseMoeda(valorStr);
  abrirAbaDeposito();
  cy.get('[data-cy=input-valor-deposito]').clear();
  if (valor > 0) {
    cy.get('[data-cy=input-valor-deposito]').type(String(valor));
  }
  cy.get('[data-cy=input-valor-deposito]').blur();
});

Then('o botão de confirmar depósito deve estar desabilitado', () => {
  cy.get('[data-cy=botao-confirmar-deposito]').should('be.disabled');
});

/**
 * Funciona tanto após uma operação bem-sucedida (redireciona para a tela de
 * detalhes da conta, que expõe [data-cy=saldo-conta]) quanto após uma
 * operação rejeitada (permanece em /movimentar, que expõe [data-cy=saldo-atual]).
 */
Then('o saldo da conta deve ser/permanecer {string}', (valorStr: string) => {
  cy.get('[data-cy=saldo-conta], [data-cy=saldo-atual]')
    .first()
    .invoke('text')
    .then((texto) => {
      expect(parseMoeda(texto)).to.be.closeTo(parseMoeda(valorStr), 0.001);
    });
});

Then('devo ver uma mensagem de erro na operação', () => {
  cy.get('[data-cy=erro-operacao]').should('be.visible').and('not.have.text', '');
});
