# language: pt
Funcionalidade: Navegação do perfil cliente
  Como cliente autenticado do ByteBank Digital
  Eu quero enxergar as operações de depósito e saque no menu
  Para não depender apenas da transferência para movimentar minha conta

  Contexto:
    Dado que sou um cliente autenticado com uma conta com saldo de "R$ 100,00"

  Cenário: Menu lateral exibe depósito/saque, transferência e perfil
    Então o menu lateral deve exibir a opção "Depositar/Sacar"
    E o menu lateral deve exibir a opção "Transferir"
    E o menu lateral deve exibir a opção "Meu perfil"
