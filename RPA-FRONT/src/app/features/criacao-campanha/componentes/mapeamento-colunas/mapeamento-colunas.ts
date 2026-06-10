import { Component, input, output, signal, effect, untracked } from '@angular/core';
import { CampanhaCsvPreviewResponse } from '../../../../core/models/campanha.model';

@Component({
  selector: 'app-mapeamento-colunas',
  standalone: true,
  templateUrl: './mapeamento-colunas.html'
})
export class MapeamentoColunasComponent {
  
  dadosPreview = input.required<CampanhaCsvPreviewResponse>();
  configMapeamento = output<{ colunasTelefone: string[], colunaNome: string }>();

  colunaTelefonePrincipal = signal<string>('');
  colunasTelefonesReserva = signal<string[]>([]);
  colunaNomeSelecionada = signal<string>('');
  
  // Agora é uma lista para suportar múltiplos erros simultâneos
  errosValidacao = signal<string[]>([]);

  constructor() {
    effect(() => {
      const dados = this.dadosPreview();
      
      if (dados && dados.colunas && dados.colunas.length > 0) {
        untracked(() => {
          this.autoDetectarColunas(dados.colunas, dados);
        });
      }
    });
  }

  private autoDetectarColunas(colunas: string[], dados: CampanhaCsvPreviewResponse) {
    let tel = dados.colunaTelefoneSugerida || '';
    let nome = dados.colunaNomeSugerida || '';

    if (!tel) {
      tel = colunas.find(c => {
        const lower = c.toLowerCase();
        return lower.includes('tele') || lower.includes('cel') || lower.includes('whats') || lower.includes('wpp');
      }) || '';
    }

    if (!nome) {
      nome = colunas.find(c => {
        const lower = c.toLowerCase();
        return lower.includes('nome') || lower.includes('cliente') || lower.includes('contato');
      }) || '';
    }

    const telsReservas = colunas.filter(c => {
      const lower = c.toLowerCase();
      return c !== tel && (lower.includes('tele') || lower.includes('cel') || lower.includes('whats') || lower.match(/[2-9]/));
    });

    this.colunaTelefonePrincipal.set(tel);
    this.colunasTelefonesReserva.set(telsReservas);
    this.colunaNomeSelecionada.set(nome);

    this.executarValidacao();
  }

  atualizarSelecao(campo: 'telPrincipal' | 'nome', event: Event) {
    const valor = (event.target as HTMLSelectElement).value;

    if (campo === 'telPrincipal') this.colunaTelefonePrincipal.set(valor);
    if (campo === 'nome') this.colunaNomeSelecionada.set(valor);

    this.executarValidacao();
  }

  toggleReserva(coluna: string, event: Event) {
    const isChecked = (event.target as HTMLInputElement).checked;
    const reservasAtuais = this.colunasTelefonesReserva();

    if (isChecked) {
      this.colunasTelefonesReserva.set([...reservasAtuais, coluna]);
    } else {
      this.colunasTelefonesReserva.set(reservasAtuais.filter(c => c !== coluna));
    }
    
    this.executarValidacao();
  }

  private executarValidacao(): boolean {
    const erros: string[] = [];
    const principal = this.colunaTelefonePrincipal();
    const reservas = this.colunasTelefonesReserva();
    const nome = this.colunaNomeSelecionada();

    if (!principal) {
      this.errosValidacao.set([]);
      return false;
    }

    // VALIDAÇÃO 1: Telefone Principal
    if (!this.verificarSePareceTelefone(principal)) {
      erros.push(`A coluna "${principal}" não parece conter telefones.`);
    }

    // VALIDAÇÃO 2: Telefones Reservas
    const reservasInvalidas: string[] = [];
    for (const reserva of reservas) {
      if (reserva === principal) {
        erros.push(`A coluna "${reserva}" não pode ser usada como principal e reserva ao mesmo tempo.`);
      } else if (!this.verificarSePareceTelefone(reserva)) {
        reservasInvalidas.push(`"${reserva}"`);
      }
    }

    if (reservasInvalidas.length > 0) {
      const verbo = reservasInvalidas.length === 1 ? 'não parece' : 'não parecem';
      const sujeito = reservasInvalidas.length === 1 ? 'A coluna reserva' : 'As colunas reservas';
      erros.push(`${sujeito} ${reservasInvalidas.join(' e ')} ${verbo} conter telefones.`);
    }

    // VALIDAÇÃO 3: Coluna de Nome (Garante que não selecionou um número pro nome)
    if (nome) {
      if (nome === principal || reservas.includes(nome)) {
        erros.push(`A coluna "${nome}" não pode ser usada para Nome e Telefone ao mesmo tempo.`);
      } else if (this.verificarSePareceTelefone(nome)) {
        erros.push(`A coluna "${nome}" selecionada para Nome parece conter números de telefone em vez de nomes.`);
      }
    }

    this.errosValidacao.set(erros);
    return erros.length === 0; 
  }

  private verificarSePareceTelefone(coluna: string): boolean {
    const amostras = this.dadosPreview().amostras;
    return amostras.some(linha => {
      const valorOriginal = linha[coluna];
      
      if (valorOriginal === undefined || valorOriginal === null || valorOriginal === '') {
        return false;
      }
      
      const valorString = String(valorOriginal);
      const apenasNumeros = valorString.replace(/\D/g, '');
      
      return apenasNumeros.length >= 8;
    });
  }

  confirmarMapeamento() {
    if (!this.colunaTelefonePrincipal()) {
      this.errosValidacao.set(['Por favor, selecione qual coluna contém o WhatsApp principal.']);
      return;
    }

    if (!this.executarValidacao()) {
      return; 
    }

    const telefonesSelecionados = [this.colunaTelefonePrincipal(), ...this.colunasTelefonesReserva()];

    this.configMapeamento.emit({
      colunasTelefone: telefonesSelecionados,
      colunaNome: this.colunaNomeSelecionada()
    });
  }
}