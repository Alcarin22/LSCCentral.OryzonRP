import { Injectable } from '@angular/core';

@Injectable({
  providedIn: 'root'
})
export class SessionService {

  setEmpleado(empleado: any): void {
    localStorage.setItem('empleado', JSON.stringify(empleado));
  }

  getEmpleado(): any {
    const data = localStorage.getItem('empleado');
    return data ? JSON.parse(data) : null;
  }

  logout(): void {
    localStorage.removeItem('empleado');
  }
}