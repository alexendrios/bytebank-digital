Feature: Fluxos da API ByteBank Digital

  Background:
    * url 'http://localhost:' + karate.properties['karate.port'] + '/api'
    * header Content-Type = 'application/json'
    * def adminEmail = 'admin@bytebank.com'
    * def adminPassword = 'admin12345'

  Scenario: Registrar um novo usuário com dados válidos
    Given request {"nome":"Karate User","email":"karate.user@bytebank.com","senha":"senha1234"}
    When path '/auth/register'
    And method post
    Then status 201
    And match response.accessToken != null
    And match response.refreshToken != null

  Scenario: Tentar registrar um usuário com e-mail inválido
    Given request {"nome":"Karate User","email":"email-invalido","senha":"senha1234"}
    When path '/auth/register'
    And method post
    Then status 400

  Scenario: Fazer login com credenciais válidas
    Given request {"nome":"Karate Login","email":"karate.login@bytebank.com","senha":"senha1234"}
    When path '/auth/register'
    And method post
    Then status 201

    Given request {"email":"karate.login@bytebank.com","senha":"senha1234"}
    When path '/auth/login'
    And method post
    Then status 200
    And match response.accessToken != null
    And match response.refreshToken != null

  Scenario: Tentar fazer login com senha incorreta
    Given request {"nome":"Karate Login","email":"karate.login.errado@bytebank.com","senha":"senha1234"}
    When path '/auth/register'
    And method post
    Then status 201

    Given request {"email":"karate.login.errado@bytebank.com","senha":"senhaErrada"}
    When path '/auth/login'
    And method post
    Then status 401

  Scenario: Renovar token com refresh token válido
    Given request {"nome":"Karate Refresh","email":"karate.refresh@bytebank.com","senha":"senha1234"}
    When path '/auth/register'
    And method post
    Then status 201
    And def refreshToken = response.refreshToken

    Given request {"refreshToken":"#(refreshToken)"}
    When path '/auth/refresh-token'
    And method post
    Then status 200
    And match response.accessToken != null

  Scenario: Tentar renovar token com refresh token inválido
    Given request {"refreshToken":"token-invalido"}
    When path '/auth/refresh-token'
    And method post
    Then status 401

  Scenario: Listar contas sem autenticação
    When path '/contas'
    And method get
    Then status 401

  Scenario: Criar uma conta com dados válidos
    Given request {"email":"admin@bytebank.com","senha":"admin12345"}
    When path '/auth/login'
    And method post
    Then status 200
    And def adminToken = response.accessToken

    Given header Authorization = 'Bearer ' + adminToken
    When path '/usuarios'
    And method get
    Then status 200
    And def userId = response[0].id

    Given header Authorization = 'Bearer ' + adminToken
    And request {"agencia":"0001","usuarioId":"#(userId)"}
    When path '/contas'
    And method post
    Then status 201
    And match response.id != null

  Scenario: Tentar criar uma conta sem agência
    Given request {"email":"admin@bytebank.com","senha":"admin12345"}
    When path '/auth/login'
    And method post
    Then status 200
    And def adminToken = response.accessToken

    Given header Authorization = 'Bearer ' + adminToken
    When path '/usuarios'
    And method get
    Then status 200
    And def userId = response[0].id

    Given header Authorization = 'Bearer ' + adminToken
    And request {"usuarioId":"#(userId)"}
    When path '/contas'
    And method post
    Then status 400

  Scenario: Listar usuários sem autenticação
    When path '/usuarios'
    And method get
    Then status 401

  Scenario: Criar um usuário com dados válidos
    Given request {"nome":"Karate User Admin","email":"karate.admin@bytebank.com","senha":"senha1234"}
    When path '/auth/register'
    And method post
    Then status 201
    And def accessToken = response.accessToken

    Given header Authorization = 'Bearer ' + accessToken
    And request {"nome":"Novo Usuário","email":"novo.usuario@bytebank.com","senha":"senha1234","perfil":"CLIENTE"}
    When path '/usuarios'
    And method post
    Then status 403
