import { Component, input } from '@angular/core';

@Component({
  selector: 'app-logo',
  standalone: true,
  template: `
    <div class="flex items-center gap-2.5 cursor-pointer group">
      
      <div 
        class="flex items-center justify-center transition-transform duration-300 group-hover:scale-105 group-hover:-rotate-3"
        [class]="tamanho() === 'pequeno' ? 'w-7 h-7' : 'w-10 h-10'">
        
        <svg 
          [class]="tema() === 'dark' ? 'text-brand-400' : 'text-brand-600'" 
          viewBox="0 0 24 24" 
          fill="none" 
          stroke="currentColor" 
          stroke-width="2" 
          stroke-linecap="round" 
          stroke-linejoin="round" 
          class="w-full h-full drop-shadow-md">
          
          <line x1="12" y1="2" x2="12" y2="5"></line>
          <circle cx="12" cy="2" r="1" fill="currentColor"></circle>
          
          <path d="M21 15.5C21 16.8807 19.8807 18 18.5 18H7.5L3 21V6.5C3 5.11929 4.11929 4 5.5 4H18.5C19.8807 4 21 5.11929 21 6.5V15.5Z"></path>
          
          <circle cx="9" cy="11" r="1.5" fill="currentColor" class="animate-pulse"></circle>
          <circle cx="15" cy="11" r="1.5" fill="currentColor" class="animate-pulse"></circle>
        </svg>

      </div>

      <div class="flex flex-col justify-center">
        <span 
          class="font-extrabold tracking-tight leading-none"
          [class]="tamanho() === 'pequeno' ? 'text-lg' : 'text-2xl'"
          [class.text-white]="tema() === 'dark'"
          [class.text-slate-900]="tema() === 'light'">
          WhaBot<span class="text-brand-500">.</span>
        </span>
        
        @if (tamanho() === 'padrao' && tema() === 'dark') {
          <span class="text-[10px] text-slate-400 font-medium tracking-widest uppercase mt-0.5">
            RPA System
          </span>
        }
      </div>

    </div>
  `
})
export class LogoComponent {
  tamanho = input<'padrao' | 'pequeno'>('padrao');
  tema = input<'light' | 'dark'>('dark'); 
}