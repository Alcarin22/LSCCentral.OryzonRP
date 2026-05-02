import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';

export interface EmpleadoAdmin {
  id: number;
  discordId: string;
  nombre: string;
  activo: boolean;
  rangoId: number;
  rangoNombre: string;
  rangoNivel: number;
}

export interface RangoAdmin {
  id: number;
  nombre: string;
  nivel: number;
}

export interface EmpleadoAdminUpdateRequest {
  rangoId: number | null;
  activo: boolean | null;
}

@Injectable({
  providedIn: 'root'
})
export class AdminService {
  private readonly baseUrl = `${environment.backendUrl}/api/admin`;

  constructor(private http: HttpClient) {}

  listarEmpleados(): Observable<EmpleadoAdmin[]> {
    return this.http.get<EmpleadoAdmin[]>(`${this.baseUrl}/empleados`);
  }

  listarRangos(): Observable<RangoAdmin[]> {
    return this.http.get<RangoAdmin[]>(`${this.baseUrl}/rangos`);
  }

  actualizarEmpleado(
    empleadoId: number,
    payload: EmpleadoAdminUpdateRequest
  ): Observable<EmpleadoAdmin> {
    return this.http.put<EmpleadoAdmin>(`${this.baseUrl}/empleados/${empleadoId}`, payload);
  }
}