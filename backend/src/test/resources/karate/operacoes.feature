Feature: Operações bancárias da API ByteBank Digital

  Background:
    * url 'http://localhost:' + karate.properties['karate.port'] + '/api'
    * header Content-Type = 'application/json'

  Scenario: Depositar valor válido em uma conta própria
    Given request {"nome":"Cliente Operações","email":"karate.operacoes@bytebank.com","senha":"senha1234"}
    When path '/auth/register'
    And method post
    Then status 201

    Given request {"email":"admin@bytebank.com","senha":"admin12345"}
    When path '/auth/login'
    And method post
    Then status 200
    And def adminToken = response.accessToken

    Given header Authorization = 'Bearer ' + adminToken
    When path '/usuarios'
    And method get
    Then status 200
    And def userId = response.find(x => x.email == 'karate.operacoes@bytebank.com').id

    Given header Authorization = 'Bearer ' + adminToken
    And request {"agencia":"0001","usuarioId":"#(userId)"}
    When path '/contas'
    And method post
    Then status 201
    And def contaId = response.id

    Given request {"email":"karate.operacoes@bytebank.com","senha":"senha1234"}
    When path '/auth/login'
    And method post
    Then status 200
    And def userToken = response.accessToken

    Given header Authorization = 'Bearer ' + userToken
    And request {"valor":100.50}
    When path '/contas/' + contaId + '/deposito'
    And method post
    Then status 200
    And match response.saldo == 100.50

  Scenario: Tentar depositar valor inválido
    Given request {"nome":"Cliente Depósito Inválido","email":"karate.deposito.invalido@bytebank.com","senha":"senha1234"}
    When path '/auth/register'
    And method post
    Then status 201

    Given request {"email":"admin@bytebank.com","senha":"admin12345"}
    When path '/auth/login'
    And method post
    Then status 200
    And def adminToken = response.accessToken

    Given header Authorization = 'Bearer ' + adminToken
    When path '/usuarios'
    And method get
    Then status 200
    And def userId = response.find(x => x.email == 'karate.deposito.invalido@bytebank.com').id

    Given header Authorization = 'Bearer ' + adminToken
    And request {"agencia":"0001","usuarioId":"#(userId)"}
    When path '/contas'
    And method post
    Then status 201
    And def contaId = response.id

    Given request {"email":"karate.deposito.invalido@bytebank.com","senha":"senha1234"}
    When path '/auth/login'
    And method post
    Then status 200
    And def userToken = response.accessToken

    Given header Authorization = 'Bearer ' + userToken
    And request {"valor":0}
    When path '/contas/' + contaId + '/deposito'
    And method post
    Then status 400
    And match response.message == 'Erro de validação nos campos enviados'

  Scenario: Sacar um valor dentro do saldo
    Given request {"nome":"Cliente Saque","email":"karate.saque@bytebank.com","senha":"senha1234"}
    When path '/auth/register'
    And method post
    Then status 201

    Given request {"email":"admin@bytebank.com","senha":"admin12345"}
    When path '/auth/login'
    And method post
    Then status 200
    And def adminToken = response.accessToken

    Given header Authorization = 'Bearer ' + adminToken
    When path '/usuarios'
    And method get
    Then status 200
    And def userId = response.find(x => x.email == 'karate.saque@bytebank.com').id

    Given header Authorization = 'Bearer ' + adminToken
    And request {"agencia":"0001","usuarioId":"#(userId)"}
    When path '/contas'
    And method post
    Then status 201
    And def contaId = response.id

    Given request {"email":"karate.saque@bytebank.com","senha":"senha1234"}
    When path '/auth/login'
    And method post
    Then status 200
    And def userToken = response.accessToken

    Given header Authorization = 'Bearer ' + userToken
    And request {"valor":100}
    When path '/contas/' + contaId + '/deposito'
    And method post
    Then status 200

    Given header Authorization = 'Bearer ' + userToken
    And request {"valor":40}
    When path '/contas/' + contaId + '/saque'
    And method post
    Then status 200
    And match response.saldo == 60.00

  Scenario: Tentar sacar um valor maior que o saldo
    Given request {"nome":"Cliente Saque Insuficiente","email":"karate.saque.insuficiente@bytebank.com","senha":"senha1234"}
    When path '/auth/register'
    And method post
    Then status 201

    Given request {"email":"admin@bytebank.com","senha":"admin12345"}
    When path '/auth/login'
    And method post
    Then status 200
    And def adminToken = response.accessToken

    Given header Authorization = 'Bearer ' + adminToken
    When path '/usuarios'
    And method get
    Then status 200
    And def userId = response.find(x => x.email == 'karate.saque.insuficiente@bytebank.com').id

    Given header Authorization = 'Bearer ' + adminToken
    And request {"agencia":"0001","usuarioId":"#(userId)"}
    When path '/contas'
    And method post
    Then status 201
    And def contaId = response.id

    Given request {"email":"karate.saque.insuficiente@bytebank.com","senha":"senha1234"}
    When path '/auth/login'
    And method post
    Then status 200
    And def userToken = response.accessToken

    Given header Authorization = 'Bearer ' + userToken
    And request {"valor":100}
    When path '/contas/' + contaId + '/saque'
    And method post
    Then status 422
    And match response.message == 'Saldo insuficiente para realizar o saque'

  Scenario: Transferir valor entre duas contas com sucesso
    Given request {"nome":"Cliente Origem","email":"karate.origem@bytebank.com","senha":"senha1234"}
    When path '/auth/register'
    And method post
    Then status 201

    Given request {"nome":"Cliente Destino","email":"karate.destino@bytebank.com","senha":"senha1234"}
    When path '/auth/register'
    And method post
    Then status 201

    Given request {"email":"admin@bytebank.com","senha":"admin12345"}
    When path '/auth/login'
    And method post
    Then status 200
    And def adminToken = response.accessToken

    Given header Authorization = 'Bearer ' + adminToken
    When path '/usuarios'
    And method get
    Then status 200
    And def origemUserId = response.find(x => x.email == 'karate.origem@bytebank.com').id
    And def destinoUserId = response.find(x => x.email == 'karate.destino@bytebank.com').id

    Given header Authorization = 'Bearer ' + adminToken
    And request {"agencia":"0001","usuarioId":"#(origemUserId)"}
    When path '/contas'
    And method post
    Then status 201
    And def contaOrigemId = response.id

    Given header Authorization = 'Bearer ' + adminToken
    And request {"agencia":"0001","usuarioId":"#(destinoUserId)"}
    When path '/contas'
    And method post
    Then status 201
    And def contaDestinoId = response.id

    Given request {"email":"karate.origem@bytebank.com","senha":"senha1234"}
    When path '/auth/login'
    And method post
    Then status 200
    And def userToken = response.accessToken

    Given header Authorization = 'Bearer ' + userToken
    And request {"valor":50}
    When path '/contas/' + contaOrigemId + '/deposito'
    And method post
    Then status 200

    Given header Authorization = 'Bearer ' + userToken
    And request {"contaOrigemId":"#(contaOrigemId)","contaDestinoId":"#(contaDestinoId)","valor":30}
    When path '/transferencias'
    And method post
    Then status 201
    And match response.valor == 30

  Scenario: Tentar transferir para a mesma conta
    Given request {"nome":"Cliente Transferência Inválida","email":"karate.transferencia.invalida@bytebank.com","senha":"senha1234"}
    When path '/auth/register'
    And method post
    Then status 201

    Given request {"email":"admin@bytebank.com","senha":"admin12345"}
    When path '/auth/login'
    And method post
    Then status 200
    And def adminToken = response.accessToken

    Given header Authorization = 'Bearer ' + adminToken
    When path '/usuarios'
    And method get
    Then status 200
    And def userId = response.find(x => x.email == 'karate.transferencia.invalida@bytebank.com').id

    Given header Authorization = 'Bearer ' + adminToken
    And request {"agencia":"0001","usuarioId":"#(userId)"}
    When path '/contas'
    And method post
    Then status 201
    And def contaId = response.id

    Given request {"email":"karate.transferencia.invalida@bytebank.com","senha":"senha1234"}
    When path '/auth/login'
    And method post
    Then status 200
    And def userToken = response.accessToken

    Given header Authorization = 'Bearer ' + userToken
    And request {"contaOrigemId":"#(contaId)","contaDestinoId":"#(contaId)","valor":10}
    When path '/transferencias'
    And method post
    Then status 422
    And match response.message == 'Conta de origem e destino não podem ser a mesma'

  Scenario: Consultar o extrato de uma conta própria
    Given request {"nome":"Cliente Extrato","email":"karate.extrato@bytebank.com","senha":"senha1234"}
    When path '/auth/register'
    And method post
    Then status 201

    Given request {"email":"admin@bytebank.com","senha":"admin12345"}
    When path '/auth/login'
    And method post
    Then status 200
    And def adminToken = response.accessToken

    Given header Authorization = 'Bearer ' + adminToken
    When path '/usuarios'
    And method get
    Then status 200
    And def userId = response.find(x => x.email == 'karate.extrato@bytebank.com').id

    Given header Authorization = 'Bearer ' + adminToken
    And request {"agencia":"0001","usuarioId":"#(userId)"}
    When path '/contas'
    And method post
    Then status 201
    And def contaId = response.id

    Given request {"email":"karate.extrato@bytebank.com","senha":"senha1234"}
    When path '/auth/login'
    And method post
    Then status 200
    And def userToken = response.accessToken

    Given header Authorization = 'Bearer ' + userToken
    And request {"valor":100}
    When path '/contas/' + contaId + '/deposito'
    And method post
    Then status 200

    Given header Authorization = 'Bearer ' + userToken
    And request {"valor":30}
    When path '/contas/' + contaId + '/saque'
    And method post
    Then status 200

    Given header Authorization = 'Bearer ' + userToken
    When path '/contas/' + contaId + '/extrato'
    And method get
    Then status 200
    And match response.content[0].tipo == 'SAQUE'
    And match response.content[1].tipo == 'DEPOSITO'

  Scenario: Tentar acessar a conta de outro usuário
    Given request {"nome":"Cliente Acesso Negado","email":"karate.acesso.negado@bytebank.com","senha":"senha1234"}
    When path '/auth/register'
    And method post
    Then status 201

    Given request {"nome":"Cliente Intruso","email":"karate.intruso@bytebank.com","senha":"senha1234"}
    When path '/auth/register'
    And method post
    Then status 201

    Given request {"email":"admin@bytebank.com","senha":"admin12345"}
    When path '/auth/login'
    And method post
    Then status 200
    And def adminToken = response.accessToken

    Given header Authorization = 'Bearer ' + adminToken
    When path '/usuarios'
    And method get
    Then status 200
    And def contaUsuarioId = response.find(x => x.email == 'karate.acesso.negado@bytebank.com').id

    Given header Authorization = 'Bearer ' + adminToken
    And request {"agencia":"0001","usuarioId":"#(contaUsuarioId)"}
    When path '/contas'
    And method post
    Then status 201
    And def contaId = response.id

    Given request {"email":"karate.intruso@bytebank.com","senha":"senha1234"}
    When path '/auth/login'
    And method post
    Then status 200
    And def intrusoToken = response.accessToken

    Given header Authorization = 'Bearer ' + intrusoToken
    When path '/contas/' + contaId
    And method get
    Then status 403
    And match response.message == 'Acesso negado para o recurso solicitado'

  Scenario: Tentar acessar uma conta inexistente
    Given request {"nome":"Cliente Conta Inexistente","email":"karate.conta.inexistente@bytebank.com","senha":"senha1234"}
    When path '/auth/register'
    And method post
    Then status 201

    Given request {"email":"karate.conta.inexistente@bytebank.com","senha":"senha1234"}
    When path '/auth/login'
    And method post
    Then status 200
    And def userToken = response.accessToken

    Given header Authorization = 'Bearer ' + userToken
    When path '/contas/00000000-0000-0000-0000-000000000000'
    And method get
    Then status 404
    And match response.message contains 'Conta não encontrada'

  Scenario: Tentar transferir sem saldo suficiente
    Given request {"nome":"Cliente Transferência Sem Saldo","email":"karate.transferencia.saldo@bytebank.com","senha":"senha1234"}
    When path '/auth/register'
    And method post
    Then status 201

    Given request {"nome":"Cliente Destino Saldo","email":"karate.destino.saldo@bytebank.com","senha":"senha1234"}
    When path '/auth/register'
    And method post
    Then status 201

    Given request {"email":"admin@bytebank.com","senha":"admin12345"}
    When path '/auth/login'
    And method post
    Then status 200
    And def adminToken = response.accessToken

    Given header Authorization = 'Bearer ' + adminToken
    When path '/usuarios'
    And method get
    Then status 200
    And def origemUserId = response.find(x => x.email == 'karate.transferencia.saldo@bytebank.com').id
    And def destinoUserId = response.find(x => x.email == 'karate.destino.saldo@bytebank.com').id

    Given header Authorization = 'Bearer ' + adminToken
    And request {"agencia":"0001","usuarioId":"#(origemUserId)"}
    When path '/contas'
    And method post
    Then status 201
    And def contaOrigemId = response.id

    Given header Authorization = 'Bearer ' + adminToken
    And request {"agencia":"0001","usuarioId":"#(destinoUserId)"}
    When path '/contas'
    And method post
    Then status 201
    And def contaDestinoId = response.id

    Given request {"email":"karate.transferencia.saldo@bytebank.com","senha":"senha1234"}
    When path '/auth/login'
    And method post
    Then status 200
    And def userToken = response.accessToken

    Given header Authorization = 'Bearer ' + userToken
    And request {"contaOrigemId":"#(contaOrigemId)","contaDestinoId":"#(contaDestinoId)","valor":50}
    When path '/transferencias'
    And method post
    Then status 422
    And match response.message == 'Saldo insuficiente para realizar a transferência'

  Scenario: Tentar criar uma operação com payload inválido
    Given request {"nome":"Cliente Payload Inválido","email":"karate.payload.invalido@bytebank.com","senha":"senha1234"}
    When path '/auth/register'
    And method post
    Then status 201

    Given request {"email":"karate.payload.invalido@bytebank.com","senha":"senha1234"}
    When path '/auth/login'
    And method post
    Then status 200
    And def userToken = response.accessToken

    Given header Authorization = 'Bearer ' + userToken
    And request {"valor":0}
    When path '/contas/00000000-0000-0000-0000-000000000000/deposito'
    And method post
    Then status 400
    And match response.details[0] contains 'valor'
