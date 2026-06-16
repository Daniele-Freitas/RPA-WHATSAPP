import { Component } from '@angular/core';
import { LogoComponent } from '../logo/logo.component';

@Component({
  selector: 'app-footer',
  standalone: true,
  imports: [LogoComponent],
  template: `
    <footer class="bg-slate-950 border-t border-slate-800 mt-auto shadow-[0_-4px_6px_-1px_rgba(0,0,0,0.02)]">
      <div class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8 md:py-10 flex flex-col md:flex-row items-center justify-between gap-8 md:gap-0">
        
        <div class="flex flex-col md:flex-row items-center gap-4 md:gap-6">
          <app-logo [tamanho]="'pequeno'" [tema]="'dark'"></app-logo>
          <span class="text-sm text-slate-400 font-medium hidden md:block">|</span>
          <span class="text-sm text-slate-500 font-medium">
            &copy; 2026 WhaBot Automações. Todos os direitos reservados.
          </span>
        </div>

        <nav class="flex flex-wrap justify-center gap-6 text-sm font-semibold text-slate-400">
          <a href="#" class="hover:text-brand-400 transition-colors">Relatórios</a>
          <a href="#" class="hover:text-brand-400 transition-colors">Documentação</a>
          <a href="#" class="hover:text-brand-400 transition-colors">Suporte</a>
          <a href="#" class="hover:text-brand-400 transition-colors">Termos</a>
        </nav>
        
      </div>
    </footer>
  `
})
export class FooterComponent {}