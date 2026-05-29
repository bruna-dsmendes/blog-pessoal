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
```markdown
## 🗃️ Estrutura das Tabelas (Banco de Dados)

### 1. Tabela: `tb_temas`
Mapeia as categorias das postagens.
* `id` (BigInt - Chave Primária - Auto Incremento)
* `descricao` (VarChar - Não Nulo)

### 2. Tabela: `tb_usuarios`
Armazena os dados de acesso e perfil dos usuários do blog.
* `id` (BigInt - Chave Primária - Auto Incremento)
* `nome` (VarChar - Não Nulo)
* `usuario` (VarChar - Não Nulo - Login/E-mail)
* `senha` (VarChar - Não Nulo - Criptografada)
* `foto` (VarChar - Opcional)

### 3. Tabela: `tb_postagens`
Registra os textos publicados e faz o vínculo com quem escreveu e o tema escolhido.
* `id` (BigInt - Chave Primária - Auto Incremento)
* `titulo` (VarChar - Não Nulo)
* `texto` (VarChar - Não Nulo)
* `data` (DateTime - Gerada Automaticamente)
* `tema_id` (BigInt - Chave Estrangeira ligada à `tb_temas`)
* `usuario_id` (BigInt - Chave Estrangeira ligada à `tb_usuarios`)

---

## 🔗 Relacionamentos Mapeados (Regras de Negócio)
* **Tema 1 ➔ N Postagens:** Um tema pode ter várias postagens associadas a ele, mas uma postagem pertence a apenas um tema.
* **Usuário 1 ➔ N Postagens:** Um usuário pode escrever diversas postagens no blog, mas cada postagem possui apenas um autor específico.

## 🛣️ Estrutura de Endpoints (API Routes)

> [!NOTE]
> Com exceção das rotas de cadastro e login de usuário, **todas as outras rotas exigem autenticação HTTP Basic/JWT** no cabeçalho da requisição.

### 👤 Usuários (`/usuarios`)
- `POST /usuarios/cadastrar` - Cria uma nova conta com a senha criptografada.
- `POST /usuarios/logar` - Autentica o usuário e retorna o token de acesso (JWT).
- `GET /usuarios/all` - Lista todos os usuários cadastrados no sistema.
- `GET /usuarios/{id}` - Busca um usuário específico pelo ID.
- `PUT /usuarios/atualizar` - Atualiza as informações do usuário logado.

### 🗂️ Temas (`/temas`)
- `GET /temas` - Lista todos os temas disponíveis.
- `GET /temas/{id}` - Busca um tema específico pelo ID.
- `GET /temas/descricao/{descricao}` - Filtra temas por termos contidos na descrição.
- `POST /temas` - Cadastra um novo tema (Exige privilégios).
- `PUT /temas` - Atualiza a descrição de um tema existente.
- `DELETE /temas/{id}` - Remove um tema permanentemente do banco de dados.

### 📝 Postagens (`/postagens`)
- `GET /postagens` - Lista todas as postagens publicadas no blog.
- `GET /postagens/{id}` - Busca uma postagem específica.
- `GET /postagens/titulo/{titulo}` - Busca postagens por palavras-chave contidas no título.
- `POST /postagens` - Cria uma nova publicação (Exige o vínculo com um ID de Tema e Usuário válido).
- `PUT /postagens` - Edita o título ou texto de uma publicação existente.
- `DELETE /postagens/{id}` - Deleta uma postagem permanentemente.

## 🔧 Como Executar o Projeto Localmente

### 1. Clonar o Repositório
Abra o terminal na pasta onde deseja salvar o projeto e execute os comandos abaixo para baixar os arquivos e acessar a branch de segurança:
```bash
git clone [https://github.com/bruna-dsmendes/blog-pessoal.git](https://github.com/bruna-dsmendes/blog-pessoal.git)
cd blog-pessoal
git checkout security

### 2. Configurar o Banco de Dados (MySQL)
Abra o projeto na sua IDE (IntelliJ, Eclipse ou VS Code) e navegue até o arquivo de propriedades: src/main/resources/application.properties.

Altere as linhas abaixo com o usuário e a senha locais do seu MySQL:

Properties
spring.datasource.url=jdbc:mysql://localhost:3306/db_blogpessoal?createDatabaseIfNotExist=true&serverTimezone=UTC
spring.datasource.username=seu_usuario
spring.datasource.password=sua_senha

[!TIP]
A propriedade createDatabaseIfNotExist=true criará automaticamente o banco de dados db_blogpessoal no seu MySQL assim que o projeto for executado pela primeira vez.

### 3. Executar a Aplicação
Você pode rodar a aplicação diretamente pelo botão de Run da sua IDE (na classe BlogPessoalApplication.java) ou utilizando o terminal na pasta raiz do projeto:

 - No Windows:

Bash
mvnw.cmd spring-boot:run

 - No Linux/macOS:

Bash
./mvnw spring-boot:run
O servidor será iniciado por padrão na porta 8080 (http://localhost:8080).

### 4. Testar os Endpoints
Para testar o funcionamento das rotas, validações e a segurança:

Abra o seu Insomnia ou Postman.

Faça primeiro a requisição de Cadastro de Usuário (POST /usuarios/cadastrar).

Realize o Login (POST /usuarios/logar) para receber o Token JWT ou utilize o mecanismo de Basic Auth com suas credenciais para liberar o acesso às demais rotas de Postagens e Temas.

## 🚀 Desenvolvimento
Bruna Mendes - [https://github.com/bruna-dsmendes]
