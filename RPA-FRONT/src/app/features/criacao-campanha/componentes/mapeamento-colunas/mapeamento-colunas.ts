import { Component, input, output, signal, effect } from '@angular/core';
import { CampanhaCsvPreviewResponse } from '../../../../core/models/campanha.model';

@Component({
  selector: 'app-mapeamento-colunas',
  standalone: true,
  templateUrl: './mapeamento-colunas.html'
})
export class MapeamentoColunasComponent {
  
  dadosPreview = input.required<CampanhaCsvPreviewResponse>();
  configMapeamento = output<{ colunaTelefone: string, colunaNome: string }>();

  colunaTelefoneSelecionada = signal<string>('');
  colunaNomeSelecionada = signal<string>('');

  constructor() {
    effect(() => {
      const dados = this.dadosPreview();
      if (dados) {
        this.colunaTelefoneSelecionada.set(dados.colunaTelefoneSugerida || '');
        this.colunaNomeSelecionada.set(dados.colunaNomeSugerida || '');
      }
    });
  }

  confirmarMapeamento() {
    if (!this.colunaTelefoneSelecionada()) {
      alert('Selecione a coluna que contém os números de telefone.');
      return;
    }

    this.configMapeamento.emit({
      colunaTelefone: this.colunaTelefoneSelecionada(),
      colunaNome: this.colunaNomeSelecionada()
    });
  }

  atualizarSelecao(tipo: 'telefone' | 'nome', event: Event) {
    const valor = (event.target as HTMLSelectElement).value;
    if (tipo === 'telefone') this.colunaTelefoneSelecionada.set(valor);
    if (tipo === 'nome') this.colunaNomeSelecionada.set(valor);
  }
}