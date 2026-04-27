import { Routes } from '@angular/router';
import { authGuard } from './core/guards/auth-guard';
import { noAuthGuard } from './core/guards/no-auth.guard';

import { LoginComponent } from './pages/login/login.component';
import { DashboardComponent } from './pages/dashboard/dashboard.component';
import { FacturaComponent } from './pages/factura/factura.component';
import { PrimasComponent } from './pages/primas/primas.component';

// 🔥 NUEVO IMPORT
import { FacturacionComponent } from './pages/facturacion/facturacion.component';

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

  // 🔥 NUEVA RUTA FACTURACIÓN
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
    path: '',
    redirectTo: 'dashboard',
    pathMatch: 'full'
  },
  {
    path: '**',
    redirectTo: 'dashboard'
  },
  {
  path: 'precios',
  loadComponent: () =>
    import('./pages/precios/precios.component').then(m => m.PreciosComponent)
}
];