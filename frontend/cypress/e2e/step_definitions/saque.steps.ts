import { When } from '@badeball/cypress-cucumber-preprocessor';
import { parseMoeda } from '../../support/moeda';

function abrirAbaSaque(): void {
  cy.contains('[role=tab]', 'Saque').click();
}

When('eu saco o valor de {string}', (valorStr: string) => {
  const valor = parseMoeda(valorStr);
  abrirAbaSaque();
  cy.get('[data-cy=input-valor-saque]').clear().type(String(valor));
  cy.get('[data-cy=botao-confirmar-saque]').click();
});
