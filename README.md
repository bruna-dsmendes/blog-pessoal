# 📝 Blog Pessoal - Backend API

![Status do Projeto](https://img.shields.io/badge/Status-Em%20Desenvolvimento-orange?style=for-the-badge)
![Java](https://img.shields.io/badge/Java-17-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white)
![Spring Boot](https://img.shields.io/badge/Spring_Boot-3.x-6DB33F?style=for-the-badge&logo=spring-boot&logoColor=white)
![Spring Security](https://img.shields.io/badge/Spring_Security-6DB33F?style=for-the-badge&logo=springsecurity&logoColor=white)

API RESTful completa desenvolvida para funcionar como o ecossistema backend de uma rede social/blog pessoal. O projeto está sendo estruturado seguindo as melhores práticas da arquitetura **MVC (Model-View-Controller)** e conta com uma camada robusta de **Segurança e Autenticação**.

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
    }

    Estrutura de Endpoints (API Routes)
[!NOTE]
Com exceção das rotas de cadastro e login de usuário, todas as outras rotas exigem autenticação HTTP Basic/JWT no cabeçalho da requisição.

👤 Usuários (/usuarios)
POST /usuarios/cadastrar - Cria uma nova conta (Senha criptografada automaticamente).

POST /usuarios/logar - Autentica o usuário e retorna os dados de sessão.

GET /usuarios/all - Lista todos os usuários cadastrados.

PUT /usuarios/atualizar - Atualiza as informações do usuário logado.

🗂️ Temas (/temas)
GET /temas - Lista todos os temas disponíveis.

GET /temas/{id} - Busca um tema específico pelo ID.

GET /temas/descricao/{descricao} - Filtra temas por termos contidos na descrição.

POST /temas - Cadastra um novo tema.

PUT /temas - Atualiza um tema existente.

DELETE /temas/{id} - Remove um tema do sistema.

📝 Postagens (/postagens)
GET /postagens - Lista todas as postagens publicadas.

GET /postagens/{id} - Busca uma postagem específica.

GET /postagens/titulo/{titulo} - Busca postagens por palavras-chave no título.

POST /postagens - Cria uma nova publicação (Exige associação com um ID de Tema e Usuário válido).

PUT /postagens - Edita uma publicação.

DELETE /postagens/{id} - Deleta uma postagem permanentemente.