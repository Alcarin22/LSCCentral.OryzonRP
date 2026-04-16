import { Injectable } from '@angular/core';
import { BehaviorSubject } from 'rxjs';

export interface SessionRango {
  id: number;
  nombre: string;
  nivel: number;
}

export interface SessionEmpleado {
  id: number;
  discordId: string;
  nombre: string;
  nickServidor?: string;
  avatarUrl?: string;
  rango: SessionRango | null;
  activo: boolean;
}

@Injectable({
  providedIn: 'root'
})
export class SessionService {
  private readonly STORAGE_KEYS = ['empleado', 'usuario'] as const;

  private readonly empleadoSubject = new BehaviorSubject<SessionEmpleado | null>(this.loadEmpleado());
  readonly empleado$ = this.empleadoSubject.asObservable();

  getEmpleado(): SessionEmpleado | null {
    return this.empleadoSubject.value;
  }

  setEmpleado(empleado: SessionEmpleado): void {
    localStorage.setItem('empleado', JSON.stringify(empleado));
    localStorage.setItem('usuario', JSON.stringify(empleado));
    this.empleadoSubject.next(empleado);
  }

  isLogged(): boolean {
    return !!this.getEmpleado();
  }

  logout(): void {
    for (const key of this.STORAGE_KEYS) {
      localStorage.removeItem(key);
    }
    this.empleadoSubject.next(null);
  }

  private loadEmpleado(): SessionEmpleado | null {
    for (const key of this.STORAGE_KEYS) {
      const raw = localStorage.getItem(key);

      if (!raw) {
        continue;
      }

      try {
        const parsed = JSON.parse(raw) as SessionEmpleado;
        return this.normalizarEmpleado(parsed);
      } catch (error) {
        console.error(`Error leyendo sesión desde localStorage (${key}):`, error);
      }
    }

    return null;
  }

  private normalizarEmpleado(data: SessionEmpleado): SessionEmpleado {
    return {
      id: data.id,
      discordId: data.discordId,
      nombre: data.nombre,
      nickServidor: data.nickServidor ?? data.nombre,
      avatarUrl: data.avatarUrl ?? '',
      rango: data.rango
        ? {
            id: data.rango.id,
            nombre: data.rango.nombre,
            nivel: data.rango.nivel
          }
        : null,
      activo: !!data.activo
    };
  }
}