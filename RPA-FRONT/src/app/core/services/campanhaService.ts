import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { 
  CreateCampanhaRequest, 
  CampanhaCsvImportResponse 
} from '../models/campanha.model';

@Injectable({
  providedIn: 'root'
})
export class CampanhaService {
  
  private readonly RESOURCE_URL = `${environment.apiUrl}/campanhas`;

  public constructor(private http: HttpClient) { }

  criarCampanha(request: CreateCampanhaRequest): Observable<CampanhaCsvImportResponse> {
    return this.http.post<CampanhaCsvImportResponse>(this.RESOURCE_URL, request);
  }
}