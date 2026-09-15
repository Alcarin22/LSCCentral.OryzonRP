import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

import { environment } from '../../../environments/environment';

export interface EmpleadoListado {
  id: number;
  nombre: string;
  activo: boolean;
}

@Injectable({
  providedIn: 'root'
})
export class EmpleadoService {

  private readonly apiUrl =
    `${environment.backendUrl}/api/empleados`;

  constructor(
    private http: HttpClient
  ) {}

  listarEmpleados(): Observable<EmpleadoListado[]> {
    return this.http.get<EmpleadoListado[]>(
      this.apiUrl
    );
  }
}