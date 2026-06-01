-- Adicionar coluna mensagem_template à tabela campanhas
ALTER TABLE campanhas ADD COLUMN mensagem_template TEXT;

-- Garantir que a coluna variaveis existe em contatos (já deveria existir)
-- Se precisar adicionar: ALTER TABLE contatos ADD COLUMN variaveis JSONB;
