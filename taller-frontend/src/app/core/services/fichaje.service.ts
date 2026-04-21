import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';

export interface FichajeResponse {
  fichajeActivo: boolean;
  mensaje: string;
  fechaHoraEntrada?: string | null;
  fechaHoraSalida?: string | null;
  minutosTrabajados?: number | null;
}

@Injectable({
  providedIn: 'root'
})
export class FichajeService {
  private readonly baseUrl = `${environment.backendUrl}/api/fichajes`;

  constructor(private http: HttpClient) {}

  toggleFichaje(discordId: string): Observable<FichajeResponse> {
    return this.http.post<FichajeResponse>(`${this.baseUrl}/toggle/${discordId}`, {});
  }

  obtenerEstado(discordId: string): Observable<FichajeResponse> {
    return this.http.get<FichajeResponse>(`${this.baseUrl}/estado/${discordId}`);
  }
}