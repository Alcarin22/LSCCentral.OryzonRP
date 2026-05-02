import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';

export interface AdminPrima {
  id: number;
  empleadoId: number;
  nombreEmpleado: string;
  rango: string;
  semana: number;
  fechaInicio: string;
  fechaFin: string;
  facturado: number;
  horas: number;
  servicios: number;
  porcentajeAplicado: number;
  primaBase: number;
  extraHoras: number;
  total: number;
  pagada: boolean;
  fechaPago: string | null;
}

export interface AdminPrimaPagadaRequest {
  pagada: boolean;
}

@Injectable({
  providedIn: 'root'
})
export class AdminPrimasService {
  private readonly baseUrl = `${environment.backendUrl}/api/admin/primas`;

  constructor(private http: HttpClient) {}

  listarPrimas(semana?: number | null): Observable<AdminPrima[]> {
    let params = new HttpParams();

    if (semana !== null && semana !== undefined) {
      params = params.set('semana', semana);
    }

    return this.http.get<AdminPrima[]>(this.baseUrl, { params });
  }

  actualizarPagada(primaId: number, pagada: boolean): Observable<AdminPrima> {
    const payload: AdminPrimaPagadaRequest = { pagada };
    return this.http.patch<AdminPrima>(`${this.baseUrl}/${primaId}/pagada`, payload);
  }
}