import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
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

  constructor(private http: HttpClient) {}

  toggleFichaje(discordId: string) {
    return this.http.post<FichajeResponse>(
      `${environment.backendUrl}/api/fichajes/toggle/${discordId}`,
      {}
    );
  }

  obtenerEstado(discordId: string) {
    return this.http.get<FichajeResponse>(
      `${environment.backendUrl}/api/fichajes/estado/${discordId}`
    );
  }
}