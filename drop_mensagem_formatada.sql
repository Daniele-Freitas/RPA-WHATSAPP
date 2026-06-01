-- Remover coluna mensagem_formatada que não é mais usada
ALTER TABLE contatos DROP COLUMN IF EXISTS mensagem_formatada;
