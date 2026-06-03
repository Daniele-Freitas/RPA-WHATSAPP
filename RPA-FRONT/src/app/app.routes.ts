import { Routes } from '@angular/router';

export const routes: Routes = [
	{
		path: '',
		title: 'Dashboard',
		loadComponent: () => import('./features/dashboard/dashboard').then((m) => m.Dashboard),
	},
	{
		path: 'criacao',
		title: 'Criação de Campanha',
		loadComponent: () => import('./features/criacao-campanha/criacao-campanha').then((m) => m.CriacaoCampanha),
	},
	{ path: '**', redirectTo: '' },
];
