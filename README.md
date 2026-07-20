# Blog Pessoal — API

Back-end RESTful de um blog pessoal, desenvolvido em **Java 17** e **Spring Boot**, aplicando arquitetura MVC.

## Sobre o projeto

API para gerenciamento de postagens e temas de um blog, com autenticação de usuários. Projeto desenvolvido para consolidar conceitos de persistência de dados, relacionamento entre entidades e boas práticas de API REST.

## Funcionalidades

- CRUD completo de **postagens** (título, texto, data de criação, tema)
- CRUD completo de **temas**, com relacionamento um-para-muitos com postagens
- Cadastro e autenticação de **usuários**
- Busca de postagens por título

## Tecnologias

- Java 17
- Spring Boot
- Spring Data JPA
- Hibernate
- MySQL
- Maven

## Rodando localmente

```bash
# clonar o repositório
git clone https://github.com/bruna-dsmendes/blog-pessoal.git
cd blog-pessoal

# configurar o banco de dados MySQL em application.properties

# rodar com Maven
./mvnw spring-boot:run
```

A API sobe em `http://localhost:8080`.

## Documentação

Endpoints documentados via Swagger, disponível em `/swagger-ui.html` após subir a aplicação.

---

Projeto desenvolvido para fins de estudo, como parte do Bootcamp Full Stack Java da [Generation Brasil](https://www.generation.org/brasil/).
