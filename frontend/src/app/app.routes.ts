import { Routes } from '@angular/router';
import { authGuard } from './core/guards/auth.guard';
import { adminGuard } from './core/guards/admin.guard';
import { guestGuard } from './core/guards/guest.guard';

export const routes: Routes = [
  {
    path: '',
    loadComponent: () => import('./layouts/public-layout/public-layout').then((m) => m.PublicLayout),
    canActivate: [guestGuard],
    children: [
      { path: '', pathMatch: 'full', redirectTo: 'login' },
      { path: 'login', loadComponent: () => import('./pages/login/login').then((m) => m.Login) },
      { path: 'cadastro', loadComponent: () => import('./pages/cadastro/cadastro').then((m) => m.Cadastro) }
    ]
  },
  {
    path: 'app',
    loadComponent: () => import('./layouts/private-layout/private-layout').then((m) => m.PrivateLayout),
    canActivate: [authGuard],
    children: [
      { path: '', pathMatch: 'full', redirectTo: 'dashboard' },
      { path: 'dashboard', loadComponent: () => import('./pages/dashboard/dashboard').then((m) => m.Dashboard) },
      {
        path: 'contas/:id',
        loadComponent: () => import('./pages/conta-detalhes/conta-detalhes').then((m) => m.ContaDetalhes)
      },
      {
        path: 'movimentar',
        loadComponent: () => import('./pages/movimentar/movimentar').then((m) => m.Movimentar)
      },
      {
        path: 'contas/:id/movimentar',
        loadComponent: () => import('./pages/movimentar/movimentar').then((m) => m.Movimentar)
      },
      {
        path: 'contas/:id/extrato',
        loadComponent: () => import('./pages/extrato/extrato').then((m) => m.Extrato)
      },
      {
        path: 'transferencia',
        loadComponent: () => import('./pages/transferencia/transferencia').then((m) => m.TransferenciaPage)
      },
      { path: 'perfil', loadComponent: () => import('./pages/perfil/perfil').then((m) => m.Perfil) },
      {
        path: 'admin/usuarios',
        canActivate: [adminGuard],
        loadComponent: () => import('./pages/admin-usuarios/admin-usuarios').then((m) => m.AdminUsuarios)
      },
      {
        path: 'admin/contas',
        canActivate: [adminGuard],
        loadComponent: () => import('./pages/admin-contas/admin-contas').then((m) => m.AdminContas)
      }
    ]
  },
  { path: '**', redirectTo: '' }
];
