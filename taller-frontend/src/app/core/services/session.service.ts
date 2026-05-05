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
  private empleadoSubject = new BehaviorSubject<SessionEmpleado | null>(this.getEmpleadoFromStorage());
  empleado$ = this.empleadoSubject.asObservable();

  private monitorSub?: Subscription;
  private validandoSesion = false;

  constructor(
    private http: HttpClient,
    private router: Router,
    private toastService: ToastService
  ) {}

  setEmpleado(empleado: SessionEmpleado): void {
    localStorage.setItem('empleado', JSON.stringify(empleado));
    this.empleadoSubject.next(empleado);
    this.iniciarMonitorSesion();
  }

  getEmpleado(): SessionEmpleado | null {
    return this.empleadoSubject.value;
  }

  isLogged(): boolean {
    const empleado = this.getEmpleado();
    return !!empleado && empleado.activo !== false;
  }

  logout(): void {
    localStorage.removeItem('empleado');
    this.empleadoSubject.next(null);
    this.detenerMonitorSesion();
  }

  iniciarMonitorSesion(): void {
    if (this.monitorSub || !this.getEmpleado()?.discordId) {
      return;
    }

    this.validarSesionActual();

    this.monitorSub = interval(1000).subscribe(() => {
      this.validarSesionActual();
    });
  }

  detenerMonitorSesion(): void {
    this.monitorSub?.unsubscribe();
    this.monitorSub = undefined;
    this.validandoSesion = false;
  }

  validarSesionActual(): void {
    const empleado = this.getEmpleado();

    if (!empleado?.discordId || this.validandoSesion) {
      return;
    }

    this.validandoSesion = true;

    this.http
      .get<SessionEmpleado>(`${environment.backendUrl}/api/session/empleado/${empleado.discordId}`)
      .subscribe({
        next: (empleadoActualizado) => {
          this.validandoSesion = false;

          if (!empleadoActualizado || empleadoActualizado.activo === false) {
            this.cerrarSesionPorInactividad();
            return;
          }

          localStorage.setItem('empleado', JSON.stringify(empleadoActualizado));
          this.empleadoSubject.next(empleadoActualizado);
        },
        error: (error) => {
          this.validandoSesion = false;
          console.error('Error validando sesión:', error);
        }
      });
  }

  cerrarSesionPorInactividad(): void {
    this.logout();
    this.toastService.error('Tu acceso ha sido desactivado.');
    this.router.navigate(['/login']);
  }

  private getEmpleadoFromStorage(): SessionEmpleado | null {
    const data = localStorage.getItem('empleado');

    if (!data) {
      return null;
    }

    try {
      const empleado = JSON.parse(data) as SessionEmpleado;

      if (empleado.activo === false) {
        localStorage.removeItem('empleado');
        return null;
      }

      return empleado;
    } catch {
      localStorage.removeItem('empleado');
      return null;
    }
  }
}