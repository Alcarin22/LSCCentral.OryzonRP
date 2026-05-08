import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable, map } from 'rxjs';

import { environment } from '../../../environments/environment';

export interface FacturaListado {
  id: number;
  idEmpleado: number;
  nombreEmpleado: string;
  fecha: string;
  tipo: string;
  total: number;
  convenio: boolean;
  matricula?: string | null;
  modelo?: string | null;
  estado?: string | null;
  cantidad?: number | null;
  item?: string | null;
  categoria?: string | null;
  gravedad?: string | null;
  tuneoPlate?: string | null;
  tuneoSeleccionados?: string | null;
  grua?: boolean | null;
}

export interface FacturacionFiltros {
  fechaInicio?: string;
  fechaFin?: string;
  tipo?: string;
  idEmpleado?: number | null;
}

export interface FacturasPageResponse {
  content: FacturaListado[];
  totalElements: number;
  totalPages: number;
  page: number;
  size: number;
  totalFacturado: number;
  promedioFactura: number;
}

@Injectable({
  providedIn: 'root'
})
export class FacturacionService {
  private readonly baseUrl = `${environment.backendUrl}/api/facturas`;

  constructor(private http: HttpClient) {}

  listarFacturas(
    filtros: FacturacionFiltros = {},
    page = 0,
    size = 10
  ): Observable<FacturasPageResponse> {
    let params = new HttpParams()
      .set('page', String(page))
      .set('size', String(size))
      .set('t', String(Date.now()));

    if (filtros.fechaInicio) {
      params = params.set('fechaInicio', filtros.fechaInicio);
    }

    if (filtros.fechaFin) {
      params = params.set('fechaFin', filtros.fechaFin);
    }

    if (filtros.tipo) {
      params = params.set('tipo', filtros.tipo);
    }

    if (filtros.idEmpleado !== null && filtros.idEmpleado !== undefined) {
      params = params.set('idEmpleado', String(filtros.idEmpleado));
    }

    return this.http
      .get<FacturasPageResponse | FacturaListado[]>(this.baseUrl, { params })
      .pipe(
        map((response) => {
          if (Array.isArray(response)) {
            const totalFacturado = response.reduce(
              (acc, factura) => acc + (factura.total || 0),
              0
            );

            return {
              content: response,
              totalElements: response.length,
              totalPages: 1,
              page: 0,
              size: response.length,
              totalFacturado,
              promedioFactura: response.length
                ? Math.round(totalFacturado / response.length)
                : 0
            };
          }

          return {
            content: response.content ?? [],
            totalElements: response.totalElements ?? 0,
            totalPages: response.totalPages ?? 1,
            page: response.page ?? 0,
            size: response.size ?? size,
            totalFacturado: response.totalFacturado ?? 0,
            promedioFactura: response.promedioFactura ?? 0
          };
        })
      );
  }
}