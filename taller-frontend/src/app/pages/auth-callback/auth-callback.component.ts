import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { AuthService } from '../../core/services/auth.service';
import { SessionService } from '../../core/services/session.service';

@Component({
  selector: 'app-auth-callback',
  standalone: true,
  templateUrl: './auth-callback.component.html'
})
export class AuthCallbackComponent implements OnInit {

  constructor(
    private route: ActivatedRoute,
    private authService: AuthService,
    private sessionService: SessionService,
    private router: Router
  ) {}

  ngOnInit(): void {
    const code = this.route.snapshot.queryParamMap.get('code');

    if (!code) {
      this.router.navigate(['/']);
      return;
    }

    this.authService.sendCodeToBackend(code).subscribe({
      next: (response: any) => {
        console.log('Respuesta del backend:', response);
        this.sessionService.setEmpleado(response);
        this.router.navigate(['/dashboard']);
      },
      error: (error) => {
        console.error('Error enviando code al backend:', error);
        this.router.navigate(['/']);
      }
    });
  }
}