import { Component, ElementRef, ViewChild, input, output, signal, computed } from '@angular/core';
import { FormsModule } from '@angular/forms';

@Component({
  selector: 'app-editor-mensagem',
  standalone: true,
  imports: [FormsModule],
  templateUrl: './editor-mensagem.html'
})
export class EditorMensagemComponent {
  
  colunasDisponiveis = input<string[]>([]);
  mensagemFinalizada = output<string>();

  @ViewChild('textAreaMsg') textAreaMsg!: ElementRef<HTMLTextAreaElement>;

  textoMensagem = signal<string>('');
  variaveisSistema = ['saudacao', 'primeiro_nome'];

  mensagemPreview = computed(() => {
    let msg = this.textoMensagem();
    if (!msg) return 'Sua mensagem aparecerá aqui...';

    msg = msg.replace(/{{saudacao}}/g, 'Boa tarde');
    msg = msg.replace(/{{primeiro_nome}}/g, 'João');
    
    this.colunasDisponiveis().forEach(coluna => {
      const regex = new RegExp(`{{${coluna}}}`, 'g');
      msg = msg.replace(regex, `[${coluna}]`);
    });

    return msg;
  });

  inserirVariavel(variavel: string) {
    const el = this.textAreaMsg.nativeElement;
    const inicio = el.selectionStart;
    const fim = el.selectionEnd;
    const textoAtual = this.textoMensagem();
    
    const tag = `{{${variavel}}}`;
    const novoTexto = textoAtual.substring(0, inicio) + tag + textoAtual.substring(fim);
    
    this.textoMensagem.set(novoTexto);
    
    setTimeout(() => {
      el.focus();
      el.setSelectionRange(inicio + tag.length, inicio + tag.length);
    }, 0);
  }

  confirmarMensagem() {
    if (!this.textoMensagem().trim()) {
      alert('A mensagem não pode estar vazia.');
      return;
    }
    this.mensagemFinalizada.emit(this.textoMensagem());
  }
}