# FWnet Time Tracking API

Backend do sistema FWnet Time Tracking para controle de jornada de trabalho dos analistas.

## Stack

- Java 21
- Spring Boot
- Spring Web
- Spring Data JPA
- Spring Security
- JWT
- PostgreSQL
- Flyway
- Maven
- JUnit 5
- Mockito

## Funcionalidades implementadas

- Autenticação com e-mail e senha
- Autenticação e autorização com JWT
- Perfis ADMIN e ANALYST
- Criação de usuários pelo ADMIN
- Registro de ponto
- CLOCK_IN
- LUNCH_OUT
- LUNCH_IN
- CLOCK_OUT
- Validação da sequência da jornada
- Bloqueio de marcações duplicadas
- Bloqueio de usuário inativo
- Testes automatizados do registro de ponto

## Banco de dados

PostgreSQL com versionamento de schema através do Flyway.

Migrations atuais:

- V1__create_users_table.sql
- V2__create_time_records_table.sql

## Status

Backend em desenvolvimento.
