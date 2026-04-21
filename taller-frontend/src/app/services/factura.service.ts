import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';

export interface CreateFacturaRequest {
  discordId: string;
  matricula: string | null;
  tipo: string;
  total: number;
  convenio: boolean;
  modelo?: string | null;
  estado?: string | null;
  cantidad?: number | null;
  item?: string | null;
  categoria?: string | null;
  gravedad?: string | null;
  tuneoPlate?: string | null;
  tuneoSeleccionados?: string | null;
}

@Injectable({
  providedIn: 'root'
})
export class FacturaService {
  private readonly baseUrl = `${environment.backendUrl}/api/facturas`;

  constructor(private http: HttpClient) {}

  crearFactura(payload: CreateFacturaRequest): Observable<any> {
    return this.http.post<any>(this.baseUrl, payload);
  }
}