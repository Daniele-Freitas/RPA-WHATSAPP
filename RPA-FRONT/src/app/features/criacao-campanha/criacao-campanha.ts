import { Component, signal } from '@angular/core';
import { FileUploadComponent } from '../../shared/components/file-upload/file-upload';
import { CampanhaService } from '../../core/services/campanhaService';
import { CampanhaCsvPreviewResponse } from '../../core/models/campanha.model';

@Component({
  selector: 'app-criacao-campanha',
  imports: [FileUploadComponent],
  templateUrl: './criacao-campanha.html',
  styleUrl: './criacao-campanha.scss',
  standalone: true
})
export class CriacaoCampanhaComponent {

  arquivoAtual = signal<File | null>(null);
  dadosPreview = signal<CampanhaCsvPreviewResponse | null>(null);
  carregando = signal<boolean>(false);

  constructor(private readonly campanhaService: CampanhaService){}

  processarArquivo(arquivo: File){
    this.arquivoAtual.set(arquivo);
    this.carregando.set(true);

    this.campanhaService.previewCsv(arquivo).subscribe({
      next: (response) => {
        this.dadosPreview.set(response);
        this.carregando.set(false);
      },
      error: (err) => {
        console.error("Erro ao processar CSV:", err);
        this.carregando.set(false);
        alert('Erro ao processar o CSV. Verifique o console para mais detalhes.');
      }
    })
  }
}
