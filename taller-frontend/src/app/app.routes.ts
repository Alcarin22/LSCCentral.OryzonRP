import { Routes } from '@angular/router';

import { authGuard } from './core/guards/auth-guard';
import { noAuthGuard } from './core/guards/no-auth.guard';

import { LoginComponent } from './pages/login/login.component';
import { DashboardComponent } from './pages/dashboard/dashboard.component';
import { FacturaComponent } from './pages/factura/factura.component';
import { PrimasComponent } from './pages/primas/primas.component';
import { FacturacionComponent } from './pages/facturacion/facturacion.component';
import { FichajesComponent } from './pages/fichajes/fichajes.component';

// 🔥 ADMIN (puedes usar lazy si prefieres)
import { AdministracionComponent } from './pages/administracion/administracion.component';

export const routes: Routes = [

  // 🔐 LOGIN
  {
    path: 'login',
    component: LoginComponent,
    canActivate: [noAuthGuard]
  },

  // 🏠 DASHBOARD
  {
    path: 'dashboard',
    component: DashboardComponent,
    canActivate: [authGuard]
  },

  // 🧾 FACTURA
  {
    path: 'factura',
    component: FacturaComponent,
    canActivate: [authGuard]
  },

  // 💳 FACTURACIÓN
  {
    path: 'facturacion',
    component: FacturacionComponent,
    canActivate: [authGuard]
  },

  // 💎 PRIMAS
  {
    path: 'primas',
    component: PrimasComponent,
    canActivate: [authGuard]
  },

  // ⏱️ FICHAJES
  {
    path: 'fichajes',
    component: FichajesComponent,
    canActivate: [authGuard]
  },

  // 🏷️ PRECIOS (LAZY)
  {
    path: 'precios',
    loadComponent: () =>
      import('./pages/precios/precios.component').then(m => m.PreciosComponent),
    canActivate: [authGuard]
  },

  // 📜 CONVENIOS (si ya la tienes)
  {
    path: 'convenios',
    loadComponent: () =>
      import('./pages/convenios/convenios.component').then(m => m.ConveniosComponent),
    canActivate: [authGuard]
  },

  // ⚙️ ADMINISTRACIÓN
  {
    path: 'administracion',
    component: AdministracionComponent,
    canActivate: [authGuard]
  },

  // 🔁 REDIRECCIONES (SIEMPRE AL FINAL)
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