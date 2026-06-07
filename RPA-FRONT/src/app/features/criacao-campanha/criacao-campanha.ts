import { Component, signal } from '@angular/core';
import { FileUploadComponent } from '../../shared/components/file-upload/file-upload';
import { CampanhaService } from '../../core/services/campanhaService';
import { CampanhaCsvPreviewResponse } from '../../core/models/campanha.model';
import { EditorMensagemComponent } from './componentes/editor-mensagem/editor-mensagem';
import { MapeamentoColunasComponent } from './componentes/mapeamento-colunas/mapeamento-colunas';

@Component({
  selector: 'app-criacao-campanha',
  imports: [FileUploadComponent, EditorMensagemComponent,MapeamentoColunasComponent],
  templateUrl: './criacao-campanha.html',
  styleUrl: './criacao-campanha.scss',
  standalone: true
})

export class CriacaoCampanhaComponent {

  arquivoAtual = signal<File | null>(null);
  dadosPreview = signal<CampanhaCsvPreviewResponse | null>(null);
  configMapeamento = signal<{ colunaTelefone: string, colunaNome: string } | null>(null);
  
  carregando = signal<boolean>(false);
  mensagemSucesso = signal<string | null>(null);

  constructor(private campanhaService: CampanhaService) {}

  processarArquivo(arquivo: File) {
    this.arquivoAtual.set(arquivo);
    this.carregando.set(true);

    this.campanhaService.previewCsv(arquivo).subscribe({
      next: (resposta) => {
        this.dadosPreview.set(resposta);
        this.carregando.set(false);
      },
      error: (erro) => {
        console.error('Erro ao ler CSV', erro);
        this.carregando.set(false);
        alert('Erro ao processar o arquivo. Verifique o console.');
      }
    });
  }

  avancarParaMensagem(config: { colunaTelefone: string, colunaNome: string }) {
    this.configMapeamento.set(config);
  }

  finalizarCampanha(textoMensagem: string) {
    const arquivo = this.arquivoAtual();
    const mapeamento = this.configMapeamento();

    if (!arquivo || !mapeamento) {
      alert('Dados incompletos. Por favor, refaça o processo.');
      return;
    }

    this.carregando.set(true);

    const requestPayload = {
      nome: `Campanha - ${arquivo.name} - ${new Date().toLocaleDateString()}`,
      mensagem: textoMensagem,
      colunaTelefones: [mapeamento.colunaTelefone],
      colunaNome: mapeamento.colunaNome
    };

    this.campanhaService.importarCsv(arquivo, requestPayload).subscribe({
      next: (resposta) => {
        this.carregando.set(false);
        this.mensagemSucesso.set(`Campanha criada com sucesso! ${resposta.importados} contatos na fila de envio.`);
        setTimeout(() => this.resetarFluxo(), 4000);
      },
      error: (erro) => {
        console.error('Erro ao importar campanha', erro);
        this.carregando.set(false);
        alert('Ocorreu um erro ao criar a campanha. Verifique o servidor.');
      }
    });
  }

  resetarFluxo() {
    this.arquivoAtual.set(null);
    this.dadosPreview.set(null);
    this.configMapeamento.set(null);
    this.mensagemSucesso.set(null);
  }
}