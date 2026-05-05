import { CommonModule } from '@angular/common';
import { Component } from '@angular/core';
import { Router } from '@angular/router';

import { environment } from '../../../environments/environment';
import { SessionService } from '../../core/services/session.service';
import { ToastService } from '../../core/services/toast.service';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './login.component.html',
  styleUrls: ['./login.component.css']
})
export class LoginComponent {
  loading = false;

  constructor(
    private router: Router,
    private sessionService: SessionService,
    private toastService: ToastService
  ) {}

  loginWithDiscord(): void {
    this.loading = true;

    const width = 500;
    const height = 700;
    const left = window.screenX + (window.outerWidth - width) / 2;
    const top = window.screenY + (window.outerHeight - height) / 2;

    const popup = window.open(
      `${environment.backendUrl}/oauth2/authorization/discord`,
      'Discord Login',
      `width=${width},height=${height},left=${left},top=${top}`
    );

    if (!popup) {
      this.loading = false;
      this.toastService.error('El navegador ha bloqueado la ventana emergente.');
      return;
    }

    const backendOrigin = new URL(environment.backendUrl).origin;

    const messageListener = (event: MessageEvent) => {
      if (event.origin !== backendOrigin) {
        return;
      }

      const user = event.data;

      if (!user) {
        this.loading = false;
        this.toastService.error('No se pudo iniciar sesión.');
        return;
      }

      if (user.activo === false) {
        this.loading = false;
        this.sessionService.logout();
        this.toastService.error('Tu acceso está desactivado.');
        window.removeEventListener('message', messageListener);
        return;
      }

      this.sessionService.setEmpleado(user);

      window.removeEventListener('message', messageListener);
      this.loading = false;

      this.toastService.success('Sesión iniciada correctamente.');
      this.router.navigate(['/dashboard']);
    };

    window.addEventListener('message', messageListener);
  }
}