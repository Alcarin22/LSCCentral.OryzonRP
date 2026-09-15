import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { BehaviorSubject, Subscription, interval } from 'rxjs';
import { Router } from '@angular/router';

import { environment } from '../../../environments/environment';
import { ToastService } from './toast.service';

export interface SessionEmpleado {
  id: number;
  discordId: string;
  nombre: string;
  activo: boolean;
  avatarUrl: string;
  nickServidor: string;
  puedeTrabajarComoSeguridad: boolean;
  token?: string;

  rango: {
    id: number;
    nombre: string;
    nivel: number;
  };
}

@Injectable({
  providedIn: 'root'
})
export class SessionService {

  private static readonly EMPLEADO_STORAGE_KEY =
    'empleado';

  private static readonly TOKEN_STORAGE_KEY =
    'auth_token';

  private empleadoSubject =
    new BehaviorSubject<SessionEmpleado | null>(
      this.getEmpleadoFromStorage()
    );

  empleado$ =
    this.empleadoSubject.asObservable();

  private monitorSub?: Subscription;

  private validandoSesion =
    false;

  constructor(
    private http: HttpClient,
    private router: Router,
    private toastService: ToastService
  ) {}

  setEmpleado(
    empleado: SessionEmpleado
  ): void {

    if (empleado.token) {

      sessionStorage.setItem(
        SessionService.TOKEN_STORAGE_KEY,
        empleado.token
      );
    }

    const empleadoSinToken: SessionEmpleado = {
      ...empleado,
      token: undefined
    };

    localStorage.setItem(
      SessionService.EMPLEADO_STORAGE_KEY,
      JSON.stringify(
        empleadoSinToken
      )
    );

    this.empleadoSubject.next(
      empleadoSinToken
    );

    this.iniciarMonitorSesion();
  }

  getEmpleado(): SessionEmpleado | null {

    return this.empleadoSubject.value;
  }

  getToken(): string | null {

    return sessionStorage.getItem(
      SessionService.TOKEN_STORAGE_KEY
    );
  }

  isLogged(): boolean {

    const empleado =
      this.getEmpleado();

    return !!empleado
      && empleado.activo !== false
      && !!this.getToken();
  }

  logout(): void {

    localStorage.removeItem(
      SessionService.EMPLEADO_STORAGE_KEY
    );

    sessionStorage.removeItem(
      SessionService.TOKEN_STORAGE_KEY
    );

    this.empleadoSubject.next(
      null
    );

    this.detenerMonitorSesion();
  }

  iniciarMonitorSesion(): void {

    if (
      this.monitorSub ||
      !this.getEmpleado()
    ) {
      return;
    }

    this.validarSesionActual();

    this.monitorSub =
      interval(1000).subscribe(() => {

        this.validarSesionActual();

      });
  }

  detenerMonitorSesion(): void {

    this.monitorSub?.unsubscribe();

    this.monitorSub =
      undefined;

    this.validandoSesion =
      false;
  }

  validarSesionActual(): void {

    if (
      !this.getEmpleado() ||
      this.validandoSesion
    ) {
      return;
    }

    this.validandoSesion =
      true;

    this.http
      .get<SessionEmpleado>(
        `${environment.backendUrl}/api/session/me`
      )
      .subscribe({

        next: (
          empleadoActualizado
        ) => {

          this.validandoSesion =
            false;

          if (
            !empleadoActualizado ||
            empleadoActualizado.activo === false
          ) {

            this.cerrarSesionPorInactividad();

            return;
          }

          /*
           * El JWT permanece exclusivamente en sessionStorage.
           * La información del empleado se actualiza
           * independientemente del token.
           */
          const empleadoSinToken: SessionEmpleado = {
            ...empleadoActualizado,
            token: undefined
          };

          localStorage.setItem(
            SessionService.EMPLEADO_STORAGE_KEY,
            JSON.stringify(
              empleadoSinToken
            )
          );

          this.empleadoSubject.next(
            empleadoSinToken
          );
        },

        error: (
          error
        ) => {

          this.validandoSesion =
            false;

          if (
            error.status === 401 ||
            error.status === 403
          ) {

            this.cerrarSesionPorExpiracion();

            return;
          }

          console.error(
            'Error validando sesión:',
            error
          );
        }
      });
  }

  cerrarSesionPorInactividad(): void {

    this.logout();

    this.toastService.error(
      'Tu acceso ha sido desactivado.'
    );

    this.router.navigate([
      '/login'
    ]);
  }

  private cerrarSesionPorExpiracion(): void {

    this.logout();

    this.toastService.error(
      'Tu sesión ha caducado. Inicia sesión de nuevo.'
    );

    this.router.navigate([
      '/login'
    ]);
  }

  private getEmpleadoFromStorage():
    SessionEmpleado | null {

    const data =
      localStorage.getItem(
        SessionService.EMPLEADO_STORAGE_KEY
      );

    if (!data) {
      return null;
    }

    try {

      const empleado =
        JSON.parse(
          data
        ) as SessionEmpleado;

      if (
        empleado.activo === false
      ) {

        localStorage.removeItem(
          SessionService.EMPLEADO_STORAGE_KEY
        );

        return null;
      }

      return empleado;

    } catch {

      localStorage.removeItem(
        SessionService.EMPLEADO_STORAGE_KEY
      );

      return null;
    }
  }
}