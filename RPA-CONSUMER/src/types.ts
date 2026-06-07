export interface ContatoMessage {
  contatoId: string;
  campanhaId: string;
  telefones: string[];
  mensagem: string;
}

export type StatusEnvio = "SUCESSO" | "ERRO";