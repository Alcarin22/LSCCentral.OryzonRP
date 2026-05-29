import { Routes } from '@angular/router';

import { authGuard } from './core/guards/auth-guard';
import { noAuthGuard } from './core/guards/no-auth.guard';

import { LoginComponent } from './pages/login/login.component';
import { DashboardComponent } from './pages/dashboard/dashboard.component';
import { FacturaComponent } from './pages/factura/factura.component';
import { PrimasComponent } from './pages/primas/primas.component';
import { FacturacionComponent } from './pages/facturacion/facturacion.component';
import { FichajesComponent } from './pages/fichajes/fichajes.component';
import { AdministracionComponent } from './pages/administracion/administracion.component';

export const routes: Routes = [

  {
    path: 'login',
    component: LoginComponent,
    canActivate: [noAuthGuard]
  },

  {
    path: 'dashboard',
    component: DashboardComponent,
    canActivate: [authGuard]
  },

  {
    path: 'factura',
    component: FacturaComponent,
    canActivate: [authGuard]
  },

  {
    path: 'facturacion',
    component: FacturacionComponent,
    canActivate: [authGuard]
  },

  {
    path: 'primas',
    component: PrimasComponent,
    canActivate: [authGuard]
  },

  {
    path: 'precios',
    loadComponent: () =>
      import('./pages/precios/precios.component').then(m => m.PreciosComponent),
    canActivate: [authGuard]
  },

  {
    path: 'fichajes',
    component: FichajesComponent,
    canActivate: [authGuard]
  },

  {
    path: 'convenios',
    loadComponent: () =>
      import('./pages/convenios/convenios.component').then(m => m.ConveniosComponent),
    canActivate: [authGuard]
  },

  {
    path: 'normativas',
    loadComponent: () =>
      import('./pages/normativas/normativas.component').then(m => m.NormativasComponent),
    canActivate: [authGuard]
  },

  {
    path: 'manual-lsc',
    loadComponent: () =>
      import('./pages/manual-lsc/manual-lsc.component').then(m => m.ManualLscComponent),
    canActivate: [authGuard]
  },

  {
    path: 'administracion',
    component: AdministracionComponent,
    canActivate: [authGuard]
  },

  {
    path: '',
    redirectTo: 'dashboard',
    pathMatch: 'full'
  },

  {
    path: '**',
    redirectTo: 'dashboard'
  }
];