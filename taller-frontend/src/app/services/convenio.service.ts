import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';

export type EstadoConvenio = 'Activo' | 'Inactivo';
export type CategoriaConvenio =
  | 'Estado'
  | 'Talleres'
  | 'Ocio'
  | 'Alimentación'
  | 'Otros';

export interface Convenio {
  id: number;
  local: string;
  categoria: CategoriaConvenio;
  estado: EstadoConvenio;
  condicionesLsc: string | null;
  condicionesLocal: string | null;
  tieneArchivo: boolean;
  archivoNombre: string | null;
  archivoTipoMime: string | null;
}

export interface ConvenioRequest {
  local: string;
  categoria: CategoriaConvenio;
  estado: EstadoConvenio;
  condicionesLsc: string | null;
  condicionesLocal: string | null;
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

  crear(
    payload: ConvenioRequest,
    archivo: File | null
  ): Observable<Convenio> {
    const formData = this.crearFormData(payload, archivo);

    return this.http.post<Convenio>(
      this.baseUrl,
      formData
    );
  }

  actualizar(
    id: number,
    payload: ConvenioRequest,
    archivo: File | null
  ): Observable<Convenio> {
    const formData = this.crearFormData(payload, archivo);

    return this.http.put<Convenio>(
      `${this.baseUrl}/${id}`,
      formData
    );
  }

  eliminar(id: number): Observable<void> {
    return this.http.delete<void>(
      `${this.baseUrl}/${id}`
    );
  }

  obtenerArchivo(id: number): Observable<Blob> {
    return this.http.get(
      `${this.baseUrl}/${id}/archivo`,
      { responseType: 'blob' }
    );
  }

  private crearFormData(
    payload: ConvenioRequest,
    archivo: File | null
  ): FormData {
    const formData = new FormData();

    formData.append(
      'datos',
      new Blob(
        [JSON.stringify(payload)],
        { type: 'application/json' }
      )
    );

    if (archivo) {
      formData.append(
        'archivo',
        archivo,
        archivo.name
      );
    }

    return formData;
  }
}
