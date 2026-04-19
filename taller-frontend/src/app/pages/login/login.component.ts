import { Component } from '@angular/core';
import { Router } from '@angular/router';
import { environment } from '../../../environments/environment';
import { SessionService } from '../../core/services/session.service';

@Component({
  selector: 'app-login',
  standalone: true,
  templateUrl: './login.component.html',
  styleUrl: './login.component.css'
})
export class LoginComponent {
  loading = false;

  constructor(
    private router: Router,
    private sessionService: SessionService
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
      alert('Popup bloqueado');
      return;
    }

    const backendOrigin = new URL(environment.backendUrl).origin;

    const listener = (event: MessageEvent) => {
      if (event.origin !== backendOrigin) return;

      const user = event.data;

      if (!user) {
        this.loading = false;
        return;
      }

      this.sessionService.setEmpleado(user);

      window.removeEventListener('message', listener);
      this.loading = false;

      this.router.navigate(['/dashboard']);
    };

    window.addEventListener('message', listener);
  }
}