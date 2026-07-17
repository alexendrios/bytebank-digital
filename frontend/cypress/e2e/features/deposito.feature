# language: pt
Funcionalidade: Depósito em conta
  Como cliente autenticado do ByteBank Digital
  Eu quero depositar valores na minha conta
  Para aumentar meu saldo disponível

  Contexto:
    Dado que sou um cliente autenticado com uma conta com saldo de "R$ 500,00"

  Cenário: Depósito com valor válido acessado pelo menu lateral
    Quando eu acesso a opção "Depositar/Sacar" pelo menu
    E deposito o valor de "R$ 200,00"
    Então devo ver a mensagem de sucesso "Operação realizada com sucesso."
    E o saldo da conta deve ser "R$ 700,00"

  Cenário: Tentativa de depósito com valor zerado é bloqueada no formulário
    Quando eu acesso a opção "Depositar/Sacar" pelo menu
    E tento depositar o valor de "R$ 0,00"
    Então o botão de confirmar depósito deve estar desabilitado
