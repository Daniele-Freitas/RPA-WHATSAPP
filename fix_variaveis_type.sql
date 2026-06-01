-- Remover dados inválidos e recriar coluna como JSONB
DELETE FROM contatos;

-- Dropar coluna variaveis
ALTER TABLE contatos DROP COLUMN IF EXISTS variaveis;

-- Recriar como JSONB
ALTER TABLE contatos ADD COLUMN variaveis JSONB DEFAULT '{}'::jsonb;
