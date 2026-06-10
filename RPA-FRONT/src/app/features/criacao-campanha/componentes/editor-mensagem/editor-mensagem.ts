import { Component, input, output, signal, ViewChild, ElementRef, effect } from '@angular/core';
import { DomSanitizer, SafeHtml } from '@angular/platform-browser';

@Component({
  selector: 'app-editor-mensagem',
  standalone: true,
  templateUrl: './editor-mensagem.html'
})
export class EditorMensagemComponent {
  colunasDisponiveis = input.required<string[]>();
  
  // Recebemos a primeira linha da planilha real para o Preview!
  amostra = input.required<Record<string, string>>(); 
  
  mensagemFinalizada = output<string>();

  @ViewChild('editorRef') editorRef!: ElementRef<HTMLDivElement>;

  // A tela atualiza sozinha baseada no conteúdo HTML da caixa
  conteudoHtml = signal<string>('');

  variaveisSistema = [
    { chave: 'saudacao_tempo', label: 'Saudação (Bom dia/tarde)' },
    { chave: 'primeiro_nome', label: 'Primeiro Nome' }
  ];

  constructor(private sanitizer: DomSanitizer) {}

  // 1. FORMATAR TEXTO (O toggle nativo do navegador)
  formatarTexto(comando: string) {
    // comando pode ser 'bold', 'italic', 'strikeThrough'
    document.execCommand(comando, false);
    this.editorRef.nativeElement.focus();
    this.atualizarPreview();
  }

  // 2. INSERIR VARIÁVEL (Como um Pill/Badge visual)
  inserirVariavel(variavel: string) {
    this.editorRef.nativeElement.focus();
    
    // Criamos um HTML bonitinho que não pode ser editado por dentro (contenteditable="false")
    const pillHtml = `<span contenteditable="false" class="inline-flex items-center px-1.5 py-0.5 mx-0.5 rounded bg-blue-100 text-blue-800 text-[11px] font-bold uppercase select-none align-baseline border border-blue-200" data-var="${variavel}">${variavel}</span>&#8203;`;
    
    // O comando insertHTML coloca o elemento exatamente onde o cursor está piscando
    document.execCommand('insertHTML', false, pillHtml);
    this.atualizarPreview();
  }

  // Acionado toda vez que o usuário digita na caixa
  onInput() {
    this.atualizarPreview();
  }

  atualizarPreview() {
    if (this.editorRef) {
      this.conteudoHtml.set(this.editorRef.nativeElement.innerHTML);
    }
  }

  // 3. O TRADUTOR: Transforma os Pills Visuais de volta para o padrão do Backend {{ }}
  extrairTextoParaBackend(): string {
    const editor = this.editorRef.nativeElement;
    let textoFinal = this.parseNodeToWhatsApp(editor);
    
    // Remove quebras de linha em excesso no começo que o navegador pode adicionar
    return textoFinal.trim();
  }

  private parseNodeToWhatsApp(node: Node): string {
    if (node.nodeType === Node.TEXT_NODE) {
      return node.textContent || '';
    }

    if (node.nodeType === Node.ELEMENT_NODE) {
      const el = node as HTMLElement;
      
      if (el.tagName === 'BR') return '\n';
      
      // Se for um dos nossos Pills, devolve no formato do Backend
      if (el.hasAttribute('data-var')) {
        return `{{${el.getAttribute('data-var')}}}`;
      }

      let inner = '';
      for (const child of Array.from(el.childNodes)) {
        inner += this.parseNodeToWhatsApp(child);
      }

      // Converte as tags HTML geradas pelo navegador nas marcações do WhatsApp
      if (el.tagName === 'DIV' || el.tagName === 'P') return '\n' + inner;
      if (el.tagName === 'B' || el.tagName === 'STRONG') return `*${inner}*`;
      if (el.tagName === 'I' || el.tagName === 'EM') return `_${inner}_`;
      if (el.tagName === 'S' || el.tagName === 'STRIKE') return `~${inner}~`;

      return inner;
    }
    return '';
  }

  // 4. PREVIEW REAL: Mostra os dados reais da linha 1 da planilha do cliente
  get previewFormatado(): SafeHtml {
    const htmlAtual = this.conteudoHtml();
    
    if (!htmlAtual || htmlAtual.trim() === '<br>') {
      return this.sanitizer.bypassSecurityTrustHtml('<em class="text-slate-400 text-sm">Sua mensagem aparecerá aqui...</em>');
    }

    // Criamos um DOM virtual invisível para manipular os nós sem afetar o editor
    const divTemporaria = document.createElement('div');
    divTemporaria.innerHTML = htmlAtual;

    // Encontra todos os Pills visuais e substitui pelos dados reais da amostra!
    const pills = divTemporaria.querySelectorAll('span[data-var]');
    pills.forEach(pill => {
      const chave = pill.getAttribute('data-var') || '';
      
      let valorReal = this.amostra()[chave]; // Pega o dado real da linha 1!

      // Tratamento para as automáticas do sistema
      if (chave === 'saudacao_tempo') valorReal = this.obterSaudacao();
      else if (chave === 'primeiro_nome') valorReal = this.obterPrimeiroNome();

      // CORREÇÃO AQUI: Cria um nó de texto puro, sem classes, para herdar a formatação exata do texto ao redor
      const textoReal = document.createTextNode(valorReal || '[Vazio]');
      pill.replaceWith(textoReal);
    });

    return this.sanitizer.bypassSecurityTrustHtml(divTemporaria.innerHTML);
  }

  obterSaudacao(): string {
    const hora = new Date().getHours();
    if (hora < 12) return 'Bom dia';
    if (hora < 18) return 'Boa tarde';
    return 'Boa noite';
  }

  obterPrimeiroNome(): string {
    // Tenta achar na amostra uma coluna que pareça ser de nome para extrair só o primeiro
    const chaves = Object.keys(this.amostra());
    const chaveNome = chaves.find(c => c.toLowerCase().includes('nome'));
    
    if (chaveNome && this.amostra()[chaveNome]) {
      return this.amostra()[chaveNome].split(' ')[0];
    }
    return 'Cliente';
  }

  currentHour(): string {
    const agora = new Date();
    return agora.toLocaleTimeString('pt-BR', { hour: '2-digit', minute: '2-digit' });
  }

  confirmar() {
    const textoFinal = this.extrairTextoParaBackend();
    
    if (!textoFinal) {
      alert('Por favor, digite uma mensagem antes de continuar.');
      return;
    }
    
    // Dispara a mensagem perfeitamente limpa e traduzida para o backend!
    this.mensagemFinalizada.emit(textoFinal);
  }
}