import { Injectable } from '@angular/core';
import { BehaviorSubject } from 'rxjs';

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

  setEmpleado(empleado: SessionEmpleado): void {
    localStorage.setItem('empleado', JSON.stringify(empleado));
    this.empleadoSubject.next(empleado);
  }

  getEmpleado(): SessionEmpleado | null {
    return this.empleadoSubject.value;
  }

  isLogged(): boolean {
    return !!this.getEmpleado();
  }

  logout(): void {
    localStorage.removeItem('empleado');
    this.empleadoSubject.next(null);
  }

  private getEmpleadoFromStorage(): SessionEmpleado | null {
    const data = localStorage.getItem('empleado');
    return data ? JSON.parse(data) : null;
  }
}