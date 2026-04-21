import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';

import { SessionEmpleado, SessionService } from '../../core/services/session.service';
import { MisPrimasResponse, PrimasService } from '../../core/services/primas.service';

@Component({
  selector: 'app-primas',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './primas.component.html',
  styleUrls: ['./primas.component.css']
})
export class PrimasComponent implements OnInit {
  empleado: SessionEmpleado | null = null;

  nombreVisible = 'Empleado';
  rangoVisible = 'Sin rango';

  weekOffset = 0;
  loading = false;
  error = '';

  data: MisPrimasResponse = this.getEmptyData();

  progresoRecordGlobal = 0;
  progresoRecordPersonal = 0;
  restanteRecordGlobal = 0;
  restanteRecordPersonal = 0;

  constructor(
    private sessionService: SessionService,
    private primasService: PrimasService
  ) {}

  ngOnInit(): void {
    this.empleado = this.sessionService.getEmpleado();
    this.nombreVisible = this.empleado?.nickServidor || this.empleado?.nombre || 'Empleado';
    this.rangoVisible = this.empleado?.rango?.nombre || 'Sin rango';
    this.refrescarVista();
  }

  semanaAnterior(): void {
    this.weekOffset += 1;
    this.refrescarVista();
  }

  semanaMasReciente(): void {
    this.weekOffset = 0;
    this.refrescarVista();
  }

  private refrescarVista(): void {
    if (!this.empleado?.discordId) {
      this.error = 'No hay sesión activa.';
      return;
    }

    this.loading = true;
    this.error = '';

    this.primasService.getMisPrimas(this.empleado.discordId, this.weekOffset).subscribe({
      next: (response) => {
        this.data = response;

        this.nombreVisible = response.nombreEmpleado || this.nombreVisible;
        this.rangoVisible = response.rango || this.rangoVisible;

        this.progresoRecordGlobal = this.calcularPorcentaje(
          response.facturacionSemanal,
          response.recordGlobalFacturacion
        );

        this.progresoRecordPersonal = this.calcularPorcentaje(
          response.facturacionSemanal,
          response.recordPersonalFacturacion
        );

        this.restanteRecordGlobal = Math.max(
          (response.recordGlobalFacturacion || 0) - (response.facturacionSemanal || 0),
          0
        );

        this.restanteRecordPersonal = Math.max(
          (response.recordPersonalFacturacion || 0) - (response.facturacionSemanal || 0),
          0
        );

        this.loading = false;
      },
      error: (error) => {
        console.error('Error cargando primas reales:', error);
        this.error = 'No se pudieron cargar las primas.';
        this.loading = false;
      }
    });
  }

  private calcularPorcentaje(actual: number, objetivo: number): number {
    if (!objetivo || objetivo <= 0) return 0;
    return Math.min(Math.round((actual / objetivo) * 100), 100);
  }

  private getEmptyData(): MisPrimasResponse {
    return {
      nombreEmpleado: 'Empleado',
      rango: 'Sin rango',
      weekOffset: 0,
      semana: 'Sin datos',
      rangoFechas: '-',
      primaEstimada: 0,
      primaBase: 0,
      extraHoras: 0,
      facturacionSemanal: 0,
      horasTrabajadas: '0h 0m',
      serviciosRealizados: 0,
      diasTrabajados: 0,
      porcentajeAplicado: 0,
      recordPersonalFacturacion: 0,
      recordGlobalFacturacion: 0,
      actividadDiaria: [],
      historico: []
    };
  }
}