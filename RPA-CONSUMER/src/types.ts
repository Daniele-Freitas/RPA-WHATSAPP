export interface ContatoMessage {
  contatoId: string;
  campanhaId: string;
  telefone: string;
  mensagem: string;
}

export type StatusEnvio = "SUCESSO" | "ERRO";
