-- Adicionar coluna mensagem_template à tabela campanhas
ALTER TABLE campanhas ADD COLUMN IF NOT EXISTS mensagem_template TEXT;

-- Adicionar coluna variaveis à tabela contatos como JSONB
ALTER TABLE contatos ADD COLUMN IF NOT EXISTS variaveis JSONB;
