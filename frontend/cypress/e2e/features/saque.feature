# language: pt
Funcionalidade: Saque em conta
  Como cliente autenticado do ByteBank Digital
  Eu quero sacar valores da minha conta
  Para utilizar meu dinheiro fora do banco

  Contexto:
    Dado que sou um cliente autenticado com uma conta com saldo de "R$ 500,00"

  Cenário: Saque com valor válido e saldo suficiente
    Quando eu acesso a opção "Depositar/Sacar" pelo menu
    E eu saco o valor de "R$ 150,00"
    Então devo ver a mensagem de sucesso "Operação realizada com sucesso."
    E o saldo da conta deve ser "R$ 350,00"

  Cenário: Tentativa de saque com saldo insuficiente é rejeitada
    Quando eu acesso a opção "Depositar/Sacar" pelo menu
    E eu saco o valor de "R$ 999,00"
    Então devo ver uma mensagem de erro na operação
    E o saldo da conta deve permanecer "R$ 500,00"
