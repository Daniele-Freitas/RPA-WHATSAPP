import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { 
  CreateCampanhaRequest, 
  CampanhaCsvPreviewResponse,
  CampanhaCsvImportResponse
} from '../models/campanha.model';

@Injectable({
  providedIn: 'root'
})
export class CampanhaService {
  
  private readonly RESOURCE_URL = `${environment.apiUrl}/campanhas`;

  constructor(private http: HttpClient) { }

  // Endpoint manual sem import por Csv para integrações futuras/manuais
  criarCampanhaManual(request: CreateCampanhaRequest): Observable<any> {
    return this.http.post<any>(this.RESOURCE_URL, request);
  }

  // Envia o arquivo para o backend extrair as colunas
  previewCsv(arquivo: File): Observable<CampanhaCsvPreviewResponse> {
    const formData = new FormData();
    formData.append('arquivo', arquivo); // 'arquivo' o exato nome do @RequestParam no Spring Boot
    
    return this.http.post<CampanhaCsvPreviewResponse>(`${this.RESOURCE_URL}/preview-csv`, formData);
  }

  // NOVO: Envia o arquivo definitivo junto com as configurações de mapeamento
  importarCsv(arquivo: File, requestPayload: any) {
  const formData = new FormData();
  
  // 1. Anexa o arquivo normalmente
  formData.append('arquivo', arquivo);

  // 2. Empacota o JSON de configuração num Blob forçando o UTF-8
  const jsonBlob = new Blob(
    [JSON.stringify(requestPayload)], 
    { type: 'application/json; charset=utf-8' }
  );
  
  // 3. Anexa o Blob informando que se trata da 'request'
  formData.append('request', jsonBlob);

  // Envia o POST (o Angular HttpClient define o header multipart automaticamente)
  return this.http.post<CampanhaCsvImportResponse>(`${this.RESOURCE_URL}/importar-csv`, formData);
}
}