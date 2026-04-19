import { Component, OnDestroy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { environment } from '../../../environments/environment';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './login.component.html',
  styleUrl: './login.component.css'
})
export class LoginComponent implements OnDestroy {
  loading = false;
  private messageListener?: (event: MessageEvent) => void;

  loginWithDiscord(): void {
    if (this.loading) {
      return;
    }

    this.loading = true;

    const width = 520;
    const height = 720;
    const left = window.screenX + (window.outerWidth - width) / 2;
    const top = window.screenY + (window.outerHeight - height) / 2;

const popup = window.open(
  `${environment.backendUrl}/oauth2/authorization/discord`,
  'Discord Login',
  `width=${width},height=${height},left=${left},top=${top}`
);

    if (!popup) {
      this.loading = false;
      alert('No se pudo abrir la ventana de login');
      return;
    }

    this.messageListener = (event: MessageEvent) => {
      if (event.origin !== 'http://localhost:8080') {
        return;
      }

      if (event.data?.error) {
        console.error('Error de login:', event.data.error);
        this.loading = false;
        this.removeMessageListener();
        return;
      }

      localStorage.setItem('usuario', JSON.stringify(event.data));
      localStorage.setItem('empleado', JSON.stringify(event.data));

      this.loading = false;
      this.removeMessageListener();

      window.location.href = '/dashboard';
    };

    window.addEventListener('message', this.messageListener);
  }

  ngOnDestroy(): void {
    this.removeMessageListener();
  }

  private removeMessageListener(): void {
    if (this.messageListener) {
      window.removeEventListener('message', this.messageListener);
      this.messageListener = undefined;
    }
  }
}