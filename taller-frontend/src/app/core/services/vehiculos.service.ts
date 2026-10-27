import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';

export interface VehiculoEdicion {
  marca: string;
  modelo: string;
  categoria: string;
  precio: number;
  imagenUrl: string | null;
  activo: boolean;
}

export interface Vehiculo {
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

@Injectable({
  providedIn: 'root'
})
export class VehiculosService {
  private readonly apiUrl = `${environment.backendUrl}/api/vehiculos`;

  constructor(private http: HttpClient) {}

  actualizarVehiculo(id: number, datos: VehiculoEdicion): Observable<Vehiculo> {
    return this.http.put<Vehiculo>(`${environment.backendUrl}/api/admin/vehiculos/${id}`, datos);
  }

  listarVehiculos(): Observable<Vehiculo[]> {
    return this.http.get<Vehiculo[]>(this.apiUrl);
  }
}