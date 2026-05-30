# 🤖 RPA WhatsApp - Automação de Campanhas

Sistema de automação de campanhas WhatsApp com processamento assíncrono via RabbitMQ.

## 📋 Visão Geral

A aplicação permite criar campanhas de envio de mensagens WhatsApp em massa, com:
- ✅ API REST para gerenciar campanhas
- ✅ Armazenamento persistente em PostgreSQL
- ✅ Fila assíncrona com RabbitMQ
- ✅ Consumer para processar envios
- ✅ Perfis de ambiente (dev/prod)

## 🏗️ Arquitetura

```
┌─────────────┐
│  Frontend   │
└──────┬──────┘
       │ HTTP POST
       ↓
┌──────────────────────────────┐
│  RPA-Backend (Spring Boot)   │
│  - API REST                  │
│  - Persistência (JPA/Hibernate)
│  - RabbitMQ Publisher       │
└──────────────┬───────────────┘
               │ AMQP
               ↓
      ┌────────────────┐
      │   RabbitMQ     │
      │  whatsapp_jobs │
      └────────┬───────┘
               │
               ↓
      ┌─────────────────┐
      │ RPA-Consumer    │
      │ (TypeScript)    │
      │ - Queue Listener│
      │ - RPA Logic     │
      │ - DB Updates    │
      └────────┬────────┘
               │ SQL
               ↓
      ┌─────────────────┐
      │   PostgreSQL    │
      │ campanhas       │
      │ contatos        │
      └─────────────────┘
```

## 📦 Estrutura do Projeto

```
RPA-WHATSAPP/
├── RPA-BACKEND/              # API Backend (Spring Boot)
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/
│   │   │   │   └── com/rpa/whatsapp/
│   │   │   │       ├── controller/       # Endpoints HTTP
│   │   │   │       ├── service/          # Lógica de negócios
│   │   │   │       ├── repository/       # Acesso a dados
│   │   │   │       ├── domain/           # Entidades JPA
│   │   │   │       ├── dto/              # Data Transfer Objects
│   │   │   │       └── config/           # Configurações
│   │   │   └── resources/
│   │   │       ├── application.properties
│   │   │       ├── application-dev.properties
│   │   │       └── application-prod.properties
│   │   └── test/
│   ├── pom.xml                # Dependências Maven
│   └── Dockerfile             # Build do Backend
│
├── RPA-CONSUMER/             # Consumidor RabbitMQ (TypeScript)
│   ├── src/
│   │   ├── consumer.ts        # Listener da fila
│   │   ├── whatsapp.ts        # Lógica RPA
│   │   └── database.ts        # Pool PostgreSQL
│   ├── package.json
│   └── Dockerfile             # Build do Consumer
│
├── RPA-FRONT/                # Frontend (React/Vue/Angular)
│   ├── src/
│   ├── package.json
│   └── Dockerfile
│
├── docker-compose.yml         # Orquestração de containers
├── .gitignore
├── README.md
└── POSTMAN_REQUESTS.json     # Coleção de testes
```

## 🚀 Quick Start

### Pré-requisitos
- Docker & Docker Compose
- Java 21+
- Maven 3.8+
- Node.js 18+ (para o consumer)

### 1. Clonar o repositório
```bash
git clone https://github.com/Daniele-Freitas/RPA-WHATSAPP.git
cd RPA-WHATSAPP
```

### 2. Subir infraestrutura (Postgres + RabbitMQ)
```bash
docker-compose up -d
```

Verifique se os containers estão rodando:
```bash
docker-compose ps
```

### 3. Rodar o Backend

#### Desenvolvimento
```bash
cd RPA-BACKEND
mvn spring-boot:run
```

A API estará disponível em: `http://localhost:8080`

#### Produção
```bash
mvn spring-boot:run -Dspring-boot.run.arguments="--spring.profiles.active=prod"
```

### 4. Testar a API

**Criar uma campanha:**
```bash
curl -X POST http://localhost:8080/api/campanhas \
  -H "Content-Type: application/json" \
  -d '{
    "nome": "Campanha Teste",
    "contatos": [
      {
        "telefone": "5511999999999",
        "mensagemFormatada": "Olá! Teste 1"
      },
      {
        "telefone": "5511888888888",
        "mensagemFormatada": "Olá! Teste 2"
      }
    ]
  }'
```

Ou use o arquivo [POSTMAN_REQUESTS.json](./POSTMAN_REQUESTS.json) no Postman.

## 🔧 Configuração

### Variáveis de Ambiente

**Arquivo: `.env` (criar na raiz)**
```env
# PostgreSQL
DB_HOST=localhost
DB_PORT=5432
DB_NAME=app_db
DB_USER=app_user
DB_PASSWORD=app_password

# RabbitMQ
RABBITMQ_HOST=localhost
RABBITMQ_PORT=5672
RABBITMQ_USER=guest
RABBITMQ_PASSWORD=guest

# Application
SPRING_PROFILES_ACTIVE=dev
SERVER_PORT=8080
```

### Perfis Spring Boot

- **dev**: Logs verbosos, DDL auto update, hot-reload
- **prod**: Logs minimais, DDL validate, environment variables

Para mudar de perfil, edite `application.properties`:
```properties
spring.profiles.active=prod  # ou dev
```

## 📊 Banco de Dados

### Tabelas

**campanhas**
```
id (UUID) - Primary Key
nome (VARCHAR)
status (ENUM: PENDENTE, PROCESSANDO, FINALIZADA)
data_criacao (TIMESTAMP)
```

**contatos**
```
id (UUID) - Primary Key
campanha_id (UUID) - Foreign Key
telefone (VARCHAR)
mensagem_formatada (VARCHAR)
status_envio (ENUM: PENDENTE, SUCESSO, ERRO)
```

Acesso ao banco:
```bash
docker-compose exec postgres psql -U app_user -d app_db

# Listar campanhas
SELECT * FROM campanhas;

# Listar contatos
SELECT * FROM contatos;
```

## 🐰 RabbitMQ

**Fila:** `whatsapp_jobs`

Acessar Management UI:
- **URL:** http://localhost:15672
- **User:** guest
- **Password:** guest

**Monitorar fila:**
```bash
docker-compose logs -f rabbitmq
```

## 🧪 Testes

### Testar conexão com Postgres
```bash
docker-compose exec postgres psql -U app_user -d app_db -c "SELECT 1"
```

### Testar RabbitMQ
```bash
docker-compose exec rabbitmq rabbitmqctl status
```

### Logs da Aplicação
```bash
# Backend
tail -f RPA-BACKEND/logs/app.log

# Docker
docker-compose logs -f backend
```

## 📈 Endpoints da API

### Campanhas

#### Criar Campanha
```http
POST /api/campanhas
Content-Type: application/json

{
  "nome": "Campanha Exemplo",
  "contatos": [
    {
      "telefone": "5511999999999",
      "mensagemFormatada": "Mensagem de teste"
    }
  ]
}

Response: 201 Created
{
  "id": "550e8400-e29b-41d4-a716-446655440000"
}
```

#### Endpoints em desenvolvimento
- `GET /api/campanhas` - Listar campanhas
- `GET /api/campanhas/{id}` - Consultar campanha
- `GET /api/campanhas/{id}/contatos` - Listar contatos

## 🛠️ Desenvolvimento

### Stack
- **Backend:** Spring Boot 3.3.2, Java 21, Spring Data JPA, RabbitMQ AMQP
- **Database:** PostgreSQL 18
- **Consumer:** TypeScript, Node.js, pg (PostgreSQL driver)
- **Mensageria:** RabbitMQ 3-management
- **Orquestração:** Docker Compose

### Padrões de Código
- **DRY:** Don't Repeat Yourself
- **KISS:** Keep It Simple, Stupid
- **YAGNI:** You Aren't Gonna Need It
- **Separation of Concerns:** Uma responsabilidade por arquivo
- **Lombok:** Reduzir boilerplate com `@Data`, `@Builder`, etc

### Como adicionar um novo endpoint

1. **DTO:** Criar classe em `dto/` com `@Data`
2. **Entity:** Criar classe em `domain/` com `@Entity`
3. **Repository:** Estender `JpaRepository` em `repository/`
4. **Service:** Criar lógica em `service/` (opcional)
5. **Controller:** Adicionar método no controller com comentário do endpoint

**Sempre adicionar comentário com exemplo de requisição:**
```java
/**
 * POST /api/endpoint
 * 
 * Body:
 * {
 *   "campo": "valor"
 * }
 */
@PostMapping
public ResponseEntity<?> endpoint(@RequestBody Request request) {
  // ...
}
```

## 📝 Logs

Estrutura de logs por perfil:

**Dev:**
```
2026-05-30 15:09:35 - [DEBUG] com.rpa.whatsapp - Iniciando aplicação
2026-05-30 15:09:35 - [DEBUG] org.springframework.web - Mapeando endpoint
```

**Prod:**
```
2026-05-30 15:09:35 - [WARN] com.rpa.whatsapp - Recurso não encontrado
2026-05-30 15:09:35 - [ERROR] com.rpa.whatsapp - Erro ao processar
```

## 🚢 Deployment

### Docker Build
```bash
# Backend
docker build -f RPA-BACKEND/Dockerfile -t rpa-backend:latest ./RPA-BACKEND

# Consumer
docker build -f RPA-CONSUMER/Dockerfile -t rpa-consumer:latest ./RPA-CONSUMER
```

### Production Checklist
- [ ] Alterar `spring.profiles.active=prod`
- [ ] Configurar variáveis de ambiente `.env`
- [ ] Verificar dados sensíveis não estão em version control
- [ ] Executar testes
- [ ] Validar logs e monitoramento

## 🐛 Troubleshooting

### Erro: "Connection refused"
```
Postgres não está rodando. Execute: docker-compose up -d
```

### Erro: "Queue not found"
```
RabbitMQ não inicializou. Aguarde 15 segundos e tente novamente.
```

### Erro: "Spring profile not found"
```
Verifique se application-{profile}.properties existe em src/main/resources/
```

## 📚 Referências

- [Spring Boot Documentation](https://spring.io/projects/spring-boot)
- [Spring Data JPA](https://spring.io/projects/spring-data-jpa)
- [RabbitMQ](https://www.rabbitmq.com/)
- [PostgreSQL](https://www.postgresql.org/)
- [Docker Compose](https://docs.docker.com/compose/)

## 👥 Contribuindo

1. Crie uma branch: `git checkout -b feature/sua-feature`
2. Commite suas mudanças: `git commit -m "feat: descrição"`
3. Faça push: `git push origin feature/sua-feature`
4. Abra um Pull Request

## 📄 Licença

MIT License - Veja [LICENSE](./LICENSE) para detalhes.

## 📞 Suporte

Para dúvidas ou problemas:
- Abra uma issue no GitHub
- Entre em contato via email

---

**Desenvolvido com ❤️ por [Sua Equipe]**
