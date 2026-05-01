import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';

export type EstadoConvenio = 'Activo' | 'Inactivo';
export type CategoriaConvenio = 'Estado' | 'Talleres' | 'Ocio' | 'Alimentación';

export interface Convenio {
  id: number;
  nombre: string;
  categoria: CategoriaConvenio;
  estado: EstadoConvenio;
  descuento: string | null;
  contacto: string | null;
  descripcion: string | null;
  condiciones: string[];
  documentoUrl: string | null;
}

export interface ConvenioRequest {
  nombre: string;
  categoria: CategoriaConvenio;
  estado: EstadoConvenio;
  descuento: string | null;
  contacto: string | null;
  descripcion: string | null;
  condiciones: string[];
  documentoUrl: string | null;
}

@Injectable({
  providedIn: 'root'
})
export class ConvenioService {
  private readonly baseUrl = `${environment.backendUrl}/api/convenios`;

  constructor(private http: HttpClient) {}

  listar(): Observable<Convenio[]> {
    return this.http.get<Convenio[]>(this.baseUrl);
  }

  crear(payload: ConvenioRequest): Observable<Convenio> {
    return this.http.post<Convenio>(this.baseUrl, payload);
  }

  actualizar(id: number, payload: ConvenioRequest): Observable<Convenio> {
    return this.http.put<Convenio>(`${this.baseUrl}/${id}`, payload);
  }

  eliminar(id: number): Observable<void> {
    return this.http.delete<void>(`${this.baseUrl}/${id}`);
  }
}