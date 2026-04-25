import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';

export interface FacturaListado {
  id: number;
  idEmpleado: number;
  nombreEmpleado: string;
  fecha: string;
  tipo: string;
  total: number;
  convenio: boolean;
  matricula?: string;
  modelo?: string;
  estado?: string;
  cantidad?: number;
  item?: string;
  categoria?: string;
  gravedad?: string;
  tuneoPlate?: string;
  tuneoSeleccionados?: string;
  grua?: boolean;
}

export interface FacturacionFiltros {
  fechaInicio?: string;
  fechaFin?: string;
  tipo?: string;
  idEmpleado?: number | null;
}

@Injectable({
  providedIn: 'root'
})
export class FacturacionService {
  private readonly baseUrl = `${environment.backendUrl}/api/facturas`;

  constructor(private http: HttpClient) {}

  listarFacturas(filtros: FacturacionFiltros = {}): Observable<FacturaListado[]> {
    let params = new HttpParams();

    if (filtros.fechaInicio) {
      params = params.set('fechaInicio', filtros.fechaInicio);
    }

    if (filtros.fechaFin) {
      params = params.set('fechaFin', filtros.fechaFin);
    }

    if (filtros.tipo) {
      params = params.set('tipo', filtros.tipo);
    }

    if (filtros.idEmpleado) {
      params = params.set('idEmpleado', filtros.idEmpleado);
    }

    // Anti-cache (clave)
    params = params.set('t', Date.now());

    return this.http.get<FacturaListado[]>(this.baseUrl, { params });
  }
}