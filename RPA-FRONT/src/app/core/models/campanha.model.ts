export interface ContatoRequest {
  nome: string;
  telefones: string[];
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
  colunaTelefoneSugerida?: string;
  colunaNomeSugerida?: string;
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