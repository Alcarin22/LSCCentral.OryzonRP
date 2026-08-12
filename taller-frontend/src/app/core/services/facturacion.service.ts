import { Injectable } from '@angular/core';

import {
  HttpClient,
  HttpParams
} from '@angular/common/http';

import { Observable } from 'rxjs';

export interface FacturaItemDetalle {
  id: number;
  item: string;
  cantidad: number;
  precioUnitario: number;
  subtotal: number;
  lspd: boolean;
}

export interface FacturaListado {
  id: number;
  idEmpleado: number;
  nombreEmpleado: string;
  fecha: string;
  tipo: string;
  total: number;
  convenio: boolean;
  matricula: string;
  modelo: string;
  estado: string;
  estadoTasacion?: string;
  cantidad: number;
  item: string;
  categoria: string;
  gravedad: string;
  tuneoPlate: string;
  tuneoSeleccionados: string;
  grua: boolean;
  items?: FacturaItemDetalle[];
}

export interface FacturacionFiltros {
  fechaInicio?: string;
  fechaFin?: string;
  tipo?: string;
  idEmpleado?: number | null;
}

export interface FacturaResponse {
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

  private apiUrl =
    'https://lsccentraloryzonrp-production.up.railway.app/api/facturas';

  constructor(
    private http: HttpClient
  ) {}

  listarFacturas(
    filtros: FacturacionFiltros,
    pagina: number,
    size: number
  ): Observable<FacturaResponse> {

    let params = new HttpParams()
      .set('page', pagina)
      .set('size', size);

    if (filtros.fechaInicio) {
      params = params.set('fechaInicio', filtros.fechaInicio);
    }

    if (filtros.fechaFin) {
      params = params.set('fechaFin', filtros.fechaFin);
    }

    if (filtros.tipo) {
      params = params.set('tipo', filtros.tipo);
    }

    if (
      filtros.idEmpleado !== null &&
      filtros.idEmpleado !== undefined
    ) {
      params = params.set('idEmpleado', filtros.idEmpleado);
    }

    return this.http.get<FacturaResponse>(
      this.apiUrl,
      { params }
    );
  }

  marcarTasacionEnviada(id: number): Observable<FacturaListado> {
    return this.http.patch<FacturaListado>(
      `${this.apiUrl}/${id}/tasacion/enviada`,
      {}
    );
  }

  eliminarFactura(
    id: number
  ): Observable<void> {
    return this.http.delete<void>(
      `${this.apiUrl}/${id}`
    );
  }
}