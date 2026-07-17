# language: pt
Funcionalidade: Transferência entre contas
  Como cliente autenticado do ByteBank Digital
  Eu quero transferir valores para outras contas
  Para movimentar dinheiro entre titulares diferentes

  Contexto:
    Dado que sou um cliente autenticado com uma conta de origem com saldo de "R$ 500,00"
    E que existe uma conta de destino de outro cliente

  Cenário: Transferência com valor válido e saldo suficiente
    Quando eu acesso a opção "Transferir" pelo menu
    E eu transfiro o valor de "R$ 300,00" para a conta de destino
    Então devo ver a mensagem de sucesso "Transferência realizada com sucesso."
    E o saldo da conta de origem deve ser "R$ 200,00"

  Cenário: Tentativa de transferência com saldo insuficiente é rejeitada
    Quando eu acesso a opção "Transferir" pelo menu
    E eu transfiro o valor de "R$ 999,00" para a conta de destino
    Então devo ver uma mensagem de erro na transferência
    E o saldo da conta de origem deve permanecer "R$ 500,00"
