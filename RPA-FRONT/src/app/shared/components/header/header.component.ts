import { Component, signal } from '@angular/core';
import { RouterLink, RouterLinkActive } from '@angular/router';
import { LogoComponent } from '../logo/logo.component';

@Component({
  selector: 'app-header',
  standalone: true,
  imports: [LogoComponent, RouterLink, RouterLinkActive],
  template: `
    <header class="bg-slate-900 border-b border-slate-800 sticky top-0 z-50 shadow-md">
      <div class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
        <div class="flex justify-between items-center h-16">
          
          <app-logo routerLink="/" [tema]="'dark'"></app-logo>

          <nav class="hidden md:flex space-x-2 h-full ml-10">
            <a routerLink="/" routerLinkActive="bg-slate-800 text-white border-brand-500" [routerLinkActiveOptions]="{exact: true}" class="text-slate-300 hover:text-white hover:bg-slate-800/50 font-medium px-4 py-2 mt-3 mb-3 rounded-lg border-b-2 border-transparent transition-all flex items-center">Campanhas</a>
            <a routerLink="/relatorios" routerLinkActive="bg-slate-800 text-white border-brand-500" class="text-slate-300 hover:text-white hover:bg-slate-800/50 font-medium px-4 py-2 mt-3 mb-3 rounded-lg border-b-2 border-transparent transition-all flex items-center">Relatórios</a>
          </nav>

          <div class="hidden md:flex items-center gap-5 ml-auto">
            <div class="flex items-center gap-2 text-sm bg-slate-800 px-3 py-1.5 rounded-full border border-slate-700">
              <span class="relative flex h-2.5 w-2.5">
                <span class="animate-ping absolute inline-flex h-full w-full rounded-full bg-brand-400 opacity-75"></span>
                <span class="relative inline-flex rounded-full h-2.5 w-2.5 bg-brand-500"></span>
              </span>
              <span class="text-slate-300 font-medium text-xs tracking-wide">Servidor Online</span>
            </div>
            <div class="h-9 w-9 rounded-full bg-gradient-to-tr from-brand-500 to-emerald-400 flex items-center justify-center text-white font-bold text-sm cursor-pointer shadow-lg hover:opacity-90 transition-opacity ring-2 ring-slate-800">
              DS
            </div>
          </div>

          <div class="md:hidden flex items-center">
            <button (click)="toggleMenu()" class="text-slate-300 hover:text-white focus:outline-none p-2 bg-slate-800 rounded-lg">
              <svg class="h-6 w-6" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                @if (!menuAberto()) {
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M4 6h16M4 12h16M4 18h16" />
                } @else {
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M6 18L18 6M6 6l12 12" />
                }
              </svg>
            </button>
          </div>
        </div>
      </div>

      @if (menuAberto()) {
        <div class="md:hidden border-t border-slate-800 bg-slate-900 absolute w-full shadow-2xl">
          <div class="px-4 pt-2 pb-4 space-y-1">
            <a routerLink="/" (click)="toggleMenu()" class="block px-3 py-2 rounded-md text-base font-medium text-white bg-slate-800">Campanhas</a>
            <a routerLink="/relatorios" (click)="toggleMenu()" class="block px-3 py-2 rounded-md text-base font-medium text-slate-300 hover:text-white hover:bg-slate-800">Relatórios</a>
          </div>
        </div>
      }
    </header>
  `
})
export class HeaderComponent {
  menuAberto = signal(false);

  toggleMenu() {
    this.menuAberto.update(estado => !estado);
  }
}