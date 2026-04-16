import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

export interface PrimaDiaResponse {
  dia: string;
  fecha: string;
  horas: string;
  servicios: number;
  facturacion: number;
  prima: number;
}

export interface PrimaHistorialSemanaResponse {
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
  semana: string;
  rangoFechas: string;
  weekOffset: number;
  primaEstimada: number;
  primaBase: number;
  extraHoras: number;
  facturacionSemanal: number;
  horasTrabajadas: string;
  serviciosRealizados: number;
  diasTrabajados: number;
  porcentajeAplicado: number;
  recordGlobalFacturacion: number;
  recordPersonalFacturacion: number;
  actividadDiaria: PrimaDiaResponse[];
  historico: PrimaHistorialSemanaResponse[];
}

@Injectable({
  providedIn: 'root'
})
export class PrimasService {
  private apiUrl = 'http://localhost:8080/api/primas';

  constructor(private http: HttpClient) {}

  getMisPrimas(discordId: string, weekOffset: number): Observable<MisPrimasResponse> {
    return this.http.get<MisPrimasResponse>(
      `${this.apiUrl}/${discordId}?weekOffset=${weekOffset}`
    );
  }
}