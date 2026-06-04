import { Component, output } from '@angular/core';

@Component({
  selector: 'app-file-upload',
  templateUrl: './file-upload.html',
  standalone: true
})
export class FileUploadComponent {
  
  // Angular 17.3+: A nova forma de declarar saídas de dados
  arquivoSelecionado = output<File>();

  aoSelecionarArquivo(event: Event) {
    const input = event.target as HTMLInputElement;
    
    if (input.files && input.files.length > 0) {
      const arquivo = input.files[0];
      
      if (arquivo.name.endsWith('.csv')) {
        this.arquivoSelecionado.emit(arquivo);
      } else {
        alert('Por favor, selecione um arquivo .csv válido.');
      }
    }
  }
}