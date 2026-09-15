import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

import { environment } from '../../../environments/environment';

export interface DashboardHoyResponse {
  horaEntrada: string | null;
  fichajeActivo: boolean;
  serviciosRealizadosHoy: number;
  tipoServicio: 'MECANICA' | 'SEGURIDAD' | null;
  ultimoTurnoFecha: string | null;
  ultimoTurnoHoraEntrada: string | null;
  ultimoTurnoHoraSalida: string | null;
  ultimoTurnoMinutos: number | null;
}

export interface DashboardSemanaResponse {
  horasRegistradas: string;
  diasTrabajados: number;
  serviciosCompletados: number;
  primaEstimada: string;
}

export interface DashboardMesResponse {
  horasTotales: string;
  jornadasCompletadas: number;
  serviciosRealizados: number;
  rendimiento: string;
}

@Injectable({
  providedIn: 'root'
})
export class DashboardService {

  private readonly baseUrl =
    `${environment.backendUrl}/api/dashboard`;

  constructor(
    private http: HttpClient
  ) {}

  getResumenHoy(): Observable<DashboardHoyResponse> {
    return this.http.get<DashboardHoyResponse>(
      `${this.baseUrl}/hoy`
    );
  }

  getResumenSemana(): Observable<DashboardSemanaResponse> {
    return this.http.get<DashboardSemanaResponse>(
      `${this.baseUrl}/semana`
    );
  }

  getResumenMes(): Observable<DashboardMesResponse> {
    return this.http.get<DashboardMesResponse>(
      `${this.baseUrl}/mes`
    );
  }
}