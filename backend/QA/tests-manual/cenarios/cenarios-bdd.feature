# language: pt
Funcionalidade: API ByteBank Digital
  Como usuário da API do ByteBank Digital
  Quero validar os principais fluxos de autenticação, contas, transferências e usuários
  Para garantir que o sistema responda corretamente em cenários positivos e negativos

  Cenário: Registrar um novo usuário com dados válidos
    Dado que eu tenho um payload de cadastro válido
    Quando eu envio uma requisição POST para /auth/register
    Então a API deve retornar status 201
    E deve retornar um token de acesso e um refresh token

  Cenário: Tentar registrar um usuário com e-mail inválido
    Dado que eu tenho um payload de cadastro com e-mail inválido
    Quando eu envio uma requisição POST para /auth/register
    Então a API deve retornar status 400
    E deve informar que o e-mail é inválido

  Cenário: Fazer login com credenciais válidas
    Dado que eu tenho um usuário cadastrado com e-mail admin@bytebank.com e senha admin12345
    Quando eu envio uma requisição POST para /auth/login
    Então a API deve retornar status 200
    E deve retornar um token de acesso e um refresh token

  Cenário: Tentar fazer login com senha incorreta
    Dado que eu tenho um usuário cadastrado com e-mail admin@bytebank.com e senha incorreta
    Quando eu envio uma requisição POST para /auth/login
    Então a API deve retornar status 401 ou 400
    E deve informar que as credenciais são inválidas

  Cenário: Renovar token com refresh token válido
    Dado que eu possuo um refresh token válido
    Quando eu envio uma requisição POST para /auth/refresh-token
    Então a API deve retornar status 200
    E deve retornar um novo access token

  Cenário: Tentar renovar token com refresh token inválido
    Dado que eu possuo um refresh token inválido
    Quando eu envio uma requisição POST para /auth/refresh-token
    Então a API deve retornar status 401 ou 400
    E deve informar que o refresh token é inválido

  Cenário: Listar contas com token válido
    Dado que eu estou autenticado com um token válido
    Quando eu envio uma requisição GET para /contas
    Então a API deve retornar status 200
    E deve retornar a lista de contas

  Cenário: Tentar listar contas sem autenticação
    Dado que eu não estou autenticado
    Quando eu envio uma requisição GET para /contas
    Então a API deve retornar status 401
    E deve exigir autenticação

  Cenário: Criar uma conta com dados válidos
    Dado que eu estou autenticado com um token válido
    E que eu tenho um payload de criação de conta válido
    Quando eu envio uma requisição POST para /contas
    Então a API deve retornar status 201
    E deve retornar os dados da conta criada

  Cenário: Tentar criar uma conta sem agência
    Dado que eu estou autenticado com um token válido
    E que eu tenho um payload de criação de conta sem agência
    Quando eu envio uma requisição POST para /contas
    Então a API deve retornar status 400
    E deve informar que a agência é obrigatória

  Cenário: Depositar valor válido em uma conta
    Dado que eu estou autenticado com um token válido
    E que eu possuo uma conta existente
    Quando eu envio uma requisição POST para /contas/{id}/deposito com valor 150.75
    Então a API deve retornar status 200
    E deve atualizar o saldo da conta

  Cenário: Tentar depositar valor inválido
    Dado que eu estou autenticado com um token válido
    E que eu possuo uma conta existente
    Quando eu envio uma requisição POST para /contas/{id}/deposito com valor 0
    Então a API deve retornar status 400
    E deve informar que o valor deve ser maior que zero

  Cenário: Sacar valor válido de uma conta
    Dado que eu estou autenticado com um token válido
    E que eu possuo uma conta existente
    Quando eu envio uma requisição POST para /contas/{id}/saque com valor 50.00
    Então a API deve retornar status 200
    E deve reduzir o saldo da conta

  Cenário: Tentar sacar valor maior que o saldo disponível
    Dado que eu estou autenticado com um token válido
    E que eu possuo uma conta existente com saldo insuficiente
    Quando eu envio uma requisição POST para /contas/{id}/saque com valor superior ao saldo
    Então a API deve retornar status 400 ou 409
    E deve informar que não há saldo suficiente

  Cenário: Consultar extrato de uma conta
    Dado que eu estou autenticado com um token válido
    E que eu possuo uma conta existente
    Quando eu envio uma requisição GET para /contas/{id}/extrato
    Então a API deve retornar status 200
    E deve retornar as movimentações da conta

  Cenário: Transferir valor entre contas válidas
    Dado que eu estou autenticado com um token válido
    E que eu possuo duas contas válidas
    Quando eu envio uma requisição POST para /transferencias com valor válido
    Então a API deve retornar status 201
    E deve registrar a transferência com sucesso

  Cenário: Tentar transferir com saldo insuficiente
    Dado que eu estou autenticado com um token válido
    E que eu possuo duas contas onde a origem não tem saldo suficiente
    Quando eu envio uma requisição POST para /transferencias com valor maior que o saldo
    Então a API deve retornar status 400 ou 409
    E deve informar que não há saldo suficiente para transferir

  Cenário: Listar usuários com token válido
    Dado que eu estou autenticado com um token válido
    Quando eu envio uma requisição GET para /usuarios
    Então a API deve retornar status 200
    E deve retornar a lista de usuários

  Cenário: Tentar listar usuários sem autenticação
    Dado que eu não estou autenticado
    Quando eu envio uma requisição GET para /usuarios
    Então a API deve retornar status 401
    E deve exigir autenticação

  Cenário: Criar um usuário com dados válidos
    Dado que eu estou autenticado com um token válido
    E que eu tenho um payload de criação de usuário válido
    Quando eu envio uma requisição POST para /usuarios
    Então a API deve retornar status 201
    E deve retornar os dados do usuário criado

  Cenário: Tentar criar um usuário sem perfil
    Dado que eu estou autenticado com um token válido
    E que eu tenho um payload de criação de usuário sem perfil
    Quando eu envio uma requisição POST para /usuarios
    Então a API deve retornar status 400
    E deve informar que o perfil é obrigatório

  Cenário: Atualizar um usuário existente
    Dado que eu estou autenticado com um token válido
    E que eu possuo um usuário existente
    Quando eu envio uma requisição PUT para /usuarios/{id}
    Então a API deve retornar status 200
    E deve retornar os dados atualizados do usuário

  Cenário: Remover um usuário existente
    Dado que eu estou autenticado com um token válido
    E que eu possuo um usuário existente
    Quando eu envio uma requisição DELETE para /usuarios/{id}
    Então a API deve retornar status 204
    E não deve retornar corpo na resposta
