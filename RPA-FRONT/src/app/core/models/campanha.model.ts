export interface ContatoRequest {
  nome: string;
  telefone: string;
  variaveis: Record<string, string>;
}

export interface CreateCampanhaRequest {
  nome: string;
  mensagem: string;
  contatos: ContatoRequest[];
}

export interface CampanhaCsvPreviewResponse {
  colunas: string[];
  amostras: Record<string, string>[]; 
  //(um mapa chave-valor genérico) um objeto com qualquer nome de propriedade, 
  // desde que o valor dessa propriedade seja uma string. 
  // Isso encaixa perfeitamente com os dados dinâmicos das colunas do CSV.
}

export interface CampanhaCsvImportResponse {
  id: string; // UUID
  total: number;
  importados: number;
  telefonesInvalidos: string[];
}

export enum StatusEnvio {
  PENDENTE = 'PENDENTE',
  SUCESSO = 'SUCESSO',
  ERRO = 'ERRO'
}

export interface ContatoStatusUpdateRequest {
  statusEnvio: StatusEnvio;
}