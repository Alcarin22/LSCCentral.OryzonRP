import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';

export interface PrimaHistoricoItem {
  semana: string;
  rangoFechas: string;
  horas: string;
  servicios: number;
  facturacion: number;
  prima: number;
  primaBase: number;
  extraHoras: number;
}

export interface MisPrimasResponse {
  nombreEmpleado: string;
  rango: string;
  weekOffset: number;
  semana: string;
  rangoFechas: string;
  primaEstimada: number;
  primaBase: number;
  extraHoras: number;
  facturacionSemanal: number;
  horasTrabajadas: string;
  serviciosRealizados: number;
  diasTrabajados: number;
  porcentajeAplicado: number;
  recordPersonalFacturacion: number;
  recordGlobalFacturacion: number;
  actividadDiaria: any[];
  historico: PrimaHistoricoItem[];
}

@Injectable({
  providedIn: 'root'
})
export class PrimasService {
  private readonly baseUrl = `${environment.backendUrl}/api/primas`;

  constructor(private http: HttpClient) {}

  getMisPrimas(discordId: string, weekOffset = 0): Observable<MisPrimasResponse> {
    return this.http.get<MisPrimasResponse>(
      `${this.baseUrl}/${discordId}?weekOffset=${weekOffset}`
    );
  }
}