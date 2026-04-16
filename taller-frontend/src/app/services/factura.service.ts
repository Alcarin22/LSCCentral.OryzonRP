import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

export interface CreateFacturaRequest {
  discordId: string;
  matricula: string;
  tipo: string;
  total: number;
  convenio: boolean;
}

@Injectable({
  providedIn: 'root'
})
export class FacturaService {
  private apiUrl = 'http://localhost:8080/api/facturas';

  constructor(private http: HttpClient) {}

  crearFactura(factura: CreateFacturaRequest): Observable<any> {
    return this.http.post(this.apiUrl, factura);
  }
}