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
  importarCsv(arquivo: File, config: any): Observable<CampanhaCsvImportResponse> {
    const formData = new FormData();
    formData.append('arquivo', arquivo);
    // Transformamos o objeto de configuração em string para enviar junto com o arquivo
    formData.append('config', new Blob([JSON.stringify(config)], { type: 'application/json' }));

    return this.http.post<CampanhaCsvImportResponse>(`${this.RESOURCE_URL}/importar-csv`, formData);
  }
}