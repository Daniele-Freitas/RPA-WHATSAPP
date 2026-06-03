import { Component, EventEmitter, Output } from '@angular/core';
import * as Papa from 'papaparse';

@Component({
  selector: 'app-file-upload',
  templateUrl: './file-upload.html',
  standalone: true
})
export class FileUploadComponent {
  
  @Output() arquivoLido = new EventEmitter<any[]>();

  aoSelecionarArquivo(event: any) {
    const arquivo: File = event.target.files[0];
    
    if (arquivo) {
      Papa.parse(arquivo, {
        header: true, // Avisa que a primeira linha tem os nomes das colunas
        skipEmptyLines: true, // Ignora linhas em branco no final do CSV
        complete: (resultado) => {
          console.log('CSV Lido com sucesso:', resultado.data);
          // Emite os dados formatados em JSON para o CriacaoCampanhaComponent
          this.arquivoLido.emit(resultado.data);
        },
        error: (erro) => {
          console.error('Erro ao ler CSV:', erro);
          alert('Erro ao processar o arquivo CSV.');
        }
      });
    }
  }
}