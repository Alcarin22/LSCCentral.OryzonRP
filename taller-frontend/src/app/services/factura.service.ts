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
  grua?: boolean | null;
  otros?: string | null;
}

export interface ReparacionDto {
  id: number;
  tipo: string;
  precio: number;
}

export interface ItemDto {
  id: number;
  nombre: string;
  precio: number;
}

export interface TasacionPrecioDto {
  id: number;
  estado: string;
  precio: number;
}

export interface FullTuningDto {
  id: number;
  categoria: string;
  precio: number;
}

export interface TuneoDto {
  id: number;
  pieza: string;
  precio: number;
  rendimiento?: number;
}

@Injectable({
  providedIn: 'root'
})
export class FacturaService {
  private readonly baseUrl = `${environment.backendUrl}/api/facturas`;
  private readonly reparacionesUrl = `${environment.backendUrl}/api/reparaciones`;
  private readonly itemsUrl = `${environment.backendUrl}/api/items`;
  private readonly tasacionPreciosUrl = `${environment.backendUrl}/api/tasacion-precios`;
  private readonly fullTuningUrl = `${environment.backendUrl}/api/full-tuning`;
  private readonly tuneoUrl = `${environment.backendUrl}/api/tuneo`;

  constructor(private http: HttpClient) {}

  crearFactura(payload: CreateFacturaRequest): Observable<any> {
    return this.http.post<any>(this.baseUrl, payload);
  }

  getReparaciones(): Observable<ReparacionDto[]> {
    return this.http.get<ReparacionDto[]>(this.reparacionesUrl);
  }

  getItems(): Observable<ItemDto[]> {
    return this.http.get<ItemDto[]>(this.itemsUrl);
  }

  getTasacionPrecios(): Observable<TasacionPrecioDto[]> {
    return this.http.get<TasacionPrecioDto[]>(this.tasacionPreciosUrl);
  }

  getFullTuning(): Observable<FullTuningDto[]> {
    return this.http.get<FullTuningDto[]>(this.fullTuningUrl);
  }

  getTuneo(): Observable<TuneoDto[]> {
    return this.http.get<TuneoDto[]>(this.tuneoUrl);
  }
}