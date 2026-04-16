import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { environment } from '../../../environments/environment';

export interface DashboardHoyResponse {
  horaEntrada: string | null;
  fichajeActivo: boolean;
  serviciosRealizadosHoy: number;
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

  constructor(private http: HttpClient) {}

  getResumenHoy(discordId: string) {
    return this.http.get<DashboardHoyResponse>(
      `${environment.backendUrl}/api/dashboard/hoy/${discordId}`
    );
  }

  getResumenSemana(discordId: string) {
    return this.http.get<DashboardSemanaResponse>(
      `${environment.backendUrl}/api/dashboard/semana/${discordId}`
    );
  }

  getResumenMes(discordId: string) {
    return this.http.get<DashboardMesResponse>(
      `${environment.backendUrl}/api/dashboard/mes/${discordId}`
    );
  }
}