# 📝 Blog Pessoal - Backend API

![Status do Projeto](https://img.shields.io/badge/Status-Em%20Desenvolvimento-orange?style=for-the-badge)
![Java](https://img.shields.io/badge/Java-17-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white)
![Spring Boot](https://img.shields.io/badge/Spring_Boot-3.x-6DB33F?style=for-the-badge&logo=spring-boot&logoColor=white)
![Spring Security](https://img.shields.io/badge/Spring_Security-6DB33F?style=for-the-badge&logo=springsecurity&logoColor=white)

API RESTful completa desenvolvida para funcionar como o ecossistema backend de uma rede social/blog pessoal. O projeto foi estruturado seguindo as melhores práticas da arquitetura **MVC (Model-View-Controller)** e conta com uma camada robusta de **Segurança e Autenticação**.

---

## 🚀 Funcionalidades Principais

- **CRUD Completo:** Gerenciamento total de Postagens, Temas e Usuários.
- **Relacionamentos SQL:** Implementação prática de chaves estrangeiras (`One-to-Many` e `Many-to-One`).
- **Validação de Dados:** Uso de regras com `Spring Validation` para consistência das informações antes de salvar no banco.
- **Segurança Avançada (Branch Security):** Autenticação de usuários, criptografia de senhas com `BCrypt` e geração de tokens de acesso para proteção de rotas.

---

## 🛠️ Tecnologias Utilizadas

- **Linguagem:** Java 17
- **Framework:** Spring Boot 3
- **Persistência de Dados:** Spring Data JPA / Hibernate
- **Banco de Dados:** MySQL
- **Segurança:** Spring Security
- **Testes de Requisição:** Insomnia / Postman

---

## 🗃️ Modelagem do Banco de Dados (DER)

A estrutura relacional mapeia usuários que escrevem postagens, e postagens que pertencem a categorias específicas (temas):

```mermaid
erDiagram
    TEMA ||--o{ POSTAGEM : "possui"
    USUARIO ||--o{ POSTAGEM : "escreve"

    POSTAGEM {
        bigint id PK
        string titulo
        string texto
        datetime data
    }
    TEMA {
        bigint id PK
        string descricao
    }
    USUARIO {
        bigint id PK
        string nome
        string usuario
        string senha
        string foto
    }# blog-pessoal

## 🚀 Desenvolvimento
Bruna Mendes - [GitHub]https://github.com/bruna-dsmendes
