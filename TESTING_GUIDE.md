# 🧪 Guia Completo de Testes - RPA WhatsApp API

## 📋 Resumo do Fluxo

O fluxo da API segue estas etapas:

1. **Listar colunas do CSV** → Preview (detecta cabeçalhos)
2. **Mapear variáveis** → Configurar quais colunas vão em quais campos
3. **Importar CSV** → Criar campanha + contatos + publicar na fila RabbitMQ
4. **Verificar status** → PATCH para atualizar status de envio (SUCESSO/ERRO)

---

## 🚀 Fluxo Passo-a-Passo

### ✅ PASSO 1: Preview do CSV (detectar colunas)

**Por que?** Para você saber quais colunas existem no arquivo CSV e fazer o mapeamento correto.

**URL:**
```
POST http://localhost:8080/api/campanhas/preview-csv
```

**Content-Type:** multipart/form-data

**Como fazer no Postman:**
1. Abra Postman
2. Clique em **+ New** → **Request**
3. Defina o método como **POST**
4. Cole a URL: `http://localhost:8080/api/campanhas/preview-csv`
5. Clique na aba **Body**
6. Selecione **form-data**
7. Na primeira linha, defina:
   - **KEY:** arquivo
   - **TYPE:** File (no dropdown à direita)
   - **VALUE:** clique em "Select Files" e escolha `contatos.csv` (arquivo na raiz do projeto)
8. Clique em **Send**

**Resposta esperada (200 OK):**
```json
{
  "colunas": ["Nome", "Telefone", "Email", "Projeto"],
  "amostras": [
    {
      "Nome": "Maria Silva",
      "Telefone": "5511999999999",
      "Email": "maria.silva@example.com",
      "Projeto": "Alpha"
    },
    {
      "Nome": "João Souza",
      "Telefone": "5511888888888",
      "Email": "joao.souza@example.com",
      "Projeto": "Beta"
    }
  ]
}
```

**O que significa:**
- `colunas`: nomes exatos das colunas no CSV
- `amostras`: as 5 primeiras linhas do arquivo

---

### ✅ PASSO 2: Importar CSV (criar campanha + contatos)

**Por que?** Envia o CSV + configuração de mapeamento para criar a campanha e todos os contatos com as variáveis prontas.

**URL:**
```
POST http://localhost:8080/api/campanhas/importar-csv
```

**Content-Type:** multipart/form-data

**Como fazer no Postman:**
1. Clique em **+ New** → **Request**
2. Defina método: **POST**
3. Cole URL: `http://localhost:8080/api/campanhas/importar-csv`
4. Clique em **Body** → **form-data**
5. **Primeira linha:**
   - KEY: `arquivo`
   - TYPE: File
   - VALUE: selecione `contatos.csv`
6. **Segunda linha:**
   - KEY: `config`
   - TYPE: Text (não File!)
   - VALUE: **copie e cole exatamente isto:**

```json
{
  "nome": "Campanha CSV de Teste",
  "mensagem": "Olá {{primeiro_nome}}, bem-vindo ao projeto {{projeto}}!",
  "colunaTelefone": "Telefone",
  "colunaNome": "Nome",
  "colunasVariaveis": {
    "primeiro_nome": "Nome",
    "projeto": "Projeto",
    "email": "Email"
  }
}
```

**Explicação do `config`:**
- `nome`: nome da campanha que será salva
- `mensagem`: template com variáveis entre `{{ }}`
- `colunaTelefone`: **exatamente** como aparece no Preview (caso sensível)
- `colunaNome`: nome da coluna que será usada
- `colunasVariaveis`: mapeamento: `"nome_da_variável": "coluna_CSV"`
  - Exemplo: `"primeiro_nome": "Nome"` → a coluna "Nome" será salva como variável "primeiro_nome"

7. Clique em **Send**

**Resposta esperada (201 Created):**
```json
{
  "id": "550e8400-e29b-41d4-a716-446655440000",
  "total": 2,
  "importados": 2,
  "telefonesInvalidos": []
}
```

**O que significa:**
- `id`: ID da campanha criada (guarde para depois)
- `total`: quantas linhas o CSV tinha
- `importados`: quantas foram salvas com sucesso
- `telefonesInvalidos`: array com números que não foram validados (se vazio, tudo ok)

---

### ✅ PASSO 3: Criar Campanha Manual (sem CSV)

**Por que?** Alternativa rápida para testar sem arquivo.

**URL:**
```
POST http://localhost:8080/api/campanhas
```

**Content-Type:** application/json

**Como fazer no Postman:**
1. Clique em **+ New** → **Request**
2. Defina método: **POST**
3. Cole URL: `http://localhost:8080/api/campanhas`
4. Clique em **Headers**
5. Adicione uma linha:
   - KEY: `Content-Type`
   - VALUE: `application/json`
6. Clique em **Body** → **raw** (não form-data!)
7. **Copie e cole exatamente isto:**

```json
{
  "nome": "Campanha Manual de Teste",
  "mensagem": "Olá {{primeiro_nome}}, você é do projeto {{projeto}}!",
  "contatos": [
    {
      "nome": "Maria Silva",
      "telefone": "5511999999999",
      "variaveis": {
        "primeiro_nome": "Maria",
        "projeto": "Alpha"
      }
    },
    {
      "nome": "João Souza",
      "telefone": "5511888888888",
      "variaveis": {
        "primeiro_nome": "João",
        "projeto": "Beta"
      }
    }
  ]
}
```

**Explicação:**
- `nome`: nome da campanha
- `mensagem`: template com variáveis `{{chave}}`
- `contatos`: array de contatos:
  - `nome`: nome do contato
  - `telefone`: número com país+DDD (ex: 55 + 11 + número)
  - `variaveis`: objeto com as variáveis da mensagem (as chaves devem bater com as `{{chaves}}` da mensagem)

8. Clique em **Send**

**Resposta esperada (201 Created):**
```json
{
  "id": "550e8400-e29b-41d4-a716-446655440001",
  "contatos": 2
}
```

---

### ✅ PASSO 4: Verificar Status de um Contato

**Por que?** Para ver se a mensagem foi enviada com sucesso.

**URL:**
```
PATCH http://localhost:8080/api/contatos/{contatoId}/status
```

**Content-Type:** application/json

**Como fazer no Postman:**
1. Clique em **+ New** → **Request**
2. Defina método: **PATCH**
3. Cole URL (substitua `{contatoId}` pelo ID real):
   ```
   http://localhost:8080/api/contatos/550e8400-e29b-41d4-a716-446655440000/status
   ```
   
   > Exemplo: se o contato tem ID `"abc123"`, fica:
   > `http://localhost:8080/api/contatos/abc123/status`

4. Clique em **Headers** e adicione:
   - KEY: `Content-Type`
   - VALUE: `application/json`

5. Clique em **Body** → **raw**
6. **Copie e cole:**

```json
{
  "statusEnvio": "SUCESSO"
}
```

Ou para erro:
```json
{
  "statusEnvio": "ERRO"
}
```

7. Clique em **Send**

**Resposta esperada (204 No Content):**
- Sem body (apenas status 204)

---

## 📊 Resumo Visual do Fluxo

```
┌─────────────────────────────────────────────────┐
│ PASSO 1: Preview CSV                            │
│ POST /api/campanhas/preview-csv                 │
│ Body: multipart com arquivo                     │
│ ↓ Retorna: colunas e amostras                  │
└─────────────────────────────────────────────────┘
                      ↓
┌─────────────────────────────────────────────────┐
│ PASSO 2: Importar CSV                           │
│ POST /api/campanhas/importar-csv                │
│ Body: multipart com arquivo + config JSON       │
│ ↓ Retorna: ID da campanha e contadores         │
└─────────────────────────────────────────────────┘
                      ↓
┌─────────────────────────────────────────────────┐
│ Backend envia mensagens à fila RabbitMQ         │
│ (Consumer fica escutando)                       │
│ ↓ Mensagem renderizada com variáveis            │
└─────────────────────────────────────────────────┘
                      ↓
┌─────────────────────────────────────────────────┐
│ PASSO 3: Verificar Status (via Consumer)        │
│ PATCH /api/contatos/{id}/status                 │
│ Body: { "statusEnvio": "SUCESSO" }              │
│ ↓ Consumer executa e reporta via PATCH         │
└─────────────────────────────────────────────────┘
```

---

## 💡 Dicas Importantes

### Para não errar nos requests:

1. **Content-Type:**
   - JSON simples → `application/json` (no Headers)
   - Upload de arquivo → `multipart/form-data` (Postman detecta automaticamente)

2. **Variáveis no template:**
   - Use `{{nome_variavel}}`
   - As chaves devem bater exatamente com `colunasVariaveis` (no CSV) ou `variaveis` (no JSON)
   - Exemplo: se você usa `{{primeiro_nome}}` na mensagem, deve ter `"primeiro_nome": "Nome"` no mapeamento

3. **Telefones:**
   - Sempre com país: `55` + DDD + número
   - Exemplo: São Paulo (11) 99999999 → `5511999999999`

4. **IDs:**
   - São UUIDs (strings grandes)
   - Use exatamente como vem na resposta anterior

---

## 🧬 Exemplo Completo (do Início ao Fim)

**Arquivo CSV esperado (`contatos.csv`):**
```
Nome,Telefone,Email,Projeto
Maria Silva,5511999999999,maria@example.com,Alpha
João Souza,5511888888888,joao@example.com,Beta
```

**Passo 1 → Preview:**
```
POST http://localhost:8080/api/campanhas/preview-csv
Form-data: arquivo = contatos.csv
↓ Resposta: colunas + amostras
```

**Passo 2 → Importar:**
```
POST http://localhost:8080/api/campanhas/importar-csv
Form-data:
  arquivo = contatos.csv
  config = {
    "nome": "Campanha CSV",
    "mensagem": "Olá {{primeiro_nome}}, bem-vindo!",
    "colunaTelefone": "Telefone",
    "colunaNome": "Nome",
    "colunasVariaveis": {
      "primeiro_nome": "Nome"
    }
  }
↓ Resposta: ID da campanha
```

**Mensagens renderizadas (no RabbitMQ):**
```
Para Maria:   "Olá Maria, bem-vindo!"
Para João:    "Olá João, bem-vindo!"
```

**Passo 3 → Consumer executa → PATCH de status**
```
PATCH http://localhost:8080/api/contatos/{contatoId}/status
Body: { "statusEnvio": "SUCESSO" }
```

---

## ✨ Testando Agora

1. Certifique-se que o Docker está rodando: `docker-compose ps`
2. Abra Postman e comece pelo **PASSO 1 (Preview)**
3. Veja o resultado
4. Vá para **PASSO 2 (Importar)** com a mesma URL
5. Guarde o ID da campanha
6. Verifique se mensagens chegam na fila (logs do consumer)
7. Use **PASSO 4** para confirmar status

---

**Dúvidas?** Veja os exemplos acima ou me pergunte!
