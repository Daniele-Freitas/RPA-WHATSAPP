import { Routes } from '@angular/router';

export const routes: Routes = [
    {
        path: '',
        title: 'Criação de Campanha',
        pathMatch: 'full',
        loadComponent: () => import('./features/criacao-campanha/criacao-campanha').then((m) => m.CriacaoCampanha),
    },
    {
        path: 'dashboard',
        title: 'Dashboard',
        loadComponent: () => import('./features/dashboard/dashboard').then((m) => m.Dashboard),
    },
    { path: '**', redirectTo: '' },
];