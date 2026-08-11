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
  puedeTrabajarComoSeguridad: boolean;
}

export interface RangoAdmin {
  id: number;
  nombre: string;
  nivel: number;
}

export interface EmpleadoAdminUpdateRequest {
  rangoId: number | null;
  activo: boolean | null;
  puedeTrabajarComoSeguridad?: boolean | null;
}

export interface VehiculoAdmin {
  id: number;
  marca: string;
  modelo: string;
  categoria: string;
  precio: number;
  imagenUrl: string | null;
  activo: boolean;
  createdAt: string | null;
  updatedAt: string | null;
}

export interface VehiculoAdminRequest {
  marca: string;
  modelo: string;
  categoria: string;
  precio: number;
  imagenUrl: string | null;
  activo: boolean;
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
    return this.http.put<EmpleadoAdmin>(
      `${this.baseUrl}/empleados/${empleadoId}`,
      payload
    );
  }

  listarVehiculos(): Observable<VehiculoAdmin[]> {
    return this.http.get<VehiculoAdmin[]>(`${this.baseUrl}/vehiculos`);
  }

  crearVehiculo(payload: VehiculoAdminRequest): Observable<VehiculoAdmin> {
    return this.http.post<VehiculoAdmin>(
      `${this.baseUrl}/vehiculos`,
      payload
    );
  }

  actualizarVehiculo(
    vehiculoId: number,
    payload: VehiculoAdminRequest
  ): Observable<VehiculoAdmin> {
    return this.http.put<VehiculoAdmin>(
      `${this.baseUrl}/vehiculos/${vehiculoId}`,
      payload
    );
  }

  cambiarEstadoVehiculo(
    vehiculoId: number,
    activo: boolean
  ): Observable<VehiculoAdmin> {
    return this.http.patch<VehiculoAdmin>(
      `${this.baseUrl}/vehiculos/${vehiculoId}/activo`,
      { activo }
    );
  }
}