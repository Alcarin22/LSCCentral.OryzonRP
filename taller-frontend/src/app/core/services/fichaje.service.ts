import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';

import { environment } from '../../../environments/environment';

export interface FichajeResponse {
  fichajeActivo: boolean;
  mensaje: string;
  fechaHoraEntrada?: string | null;
  fechaHoraSalida?: string | null;
  minutosTrabajados?: number | null;
  tipoServicio?: 'MECANICA' | 'SEGURIDAD' | null;
}

export interface FichajeListado {
  id: number;
  idEmpleado: number;
  nombreEmpleado: string;
  fechaHoraEntrada: string;
  fechaHoraSalida: string | null;
  minutosTrabajados: number | null;
  activo: boolean;
  tipoServicio: 'MECANICA' | 'SEGURIDAD';
}

export interface FichajeFiltros {
  fechaInicio: string;
  fechaFin: string;
}

@Injectable({
  providedIn: 'root'
})
export class FichajeService {

  private readonly baseUrl =
    `${environment.backendUrl}/api/fichajes`;

  constructor(
    private http: HttpClient
  ) {}

  toggleFichaje(
    fichajeSeguridad = false
  ): Observable<FichajeResponse> {

    return this.http.post<FichajeResponse>(
      `${this.baseUrl}/toggle`,
      {
        fichajeSeguridad
      }
    );
  }

  obtenerEstadoFichaje():
    Observable<FichajeResponse> {

    return this.http.get<FichajeResponse>(
      `${this.baseUrl}/estado`
    );
  }

  listarFichajes(
    filtros: FichajeFiltros
  ): Observable<FichajeListado[]> {

    let params =
      new HttpParams();

    if (filtros.fechaInicio) {

      params = params.set(
        'fechaInicio',
        filtros.fechaInicio
      );
    }

    if (filtros.fechaFin) {

      params = params.set(
        'fechaFin',
        filtros.fechaFin
      );
    }

    return this.http.get<FichajeListado[]>(
      this.baseUrl,
      {
        params
      }
    );
  }
}