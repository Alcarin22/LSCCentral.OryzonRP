import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';

import { SessionEmpleado, SessionService } from '../../core/services/session.service';

interface PrimaHistoricoItem {
  semana: string;
  rangoFechas: string;
  facturacion: number;
  horas: number;
  servicios: number;
  primaBase: number;
  extraHoras: number;
  prima: number;
}

interface PrimaData {
  semana: string;
  rangoFechas: string;
  facturacionSemanal: number;
  recordGlobalFacturacion: number;
  recordPersonalFacturacion: number;
  primaEstimada: number;
  primaBase: number;
  extraHoras: number;
  horasTrabajadas: number;
  serviciosRealizados: number;
  diasTrabajados: number;
  porcentajeAplicado: number;
  historico: PrimaHistoricoItem[];
}

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

  data: PrimaData = this.generarDataMock(0);

  progresoRecordGlobal = 0;
  progresoRecordPersonal = 0;
  restanteRecordGlobal = 0;
  restanteRecordPersonal = 0;

  constructor(private sessionService: SessionService) {}

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
    this.loading = true;
    this.error = '';

    try {
      this.data = this.generarDataMock(this.weekOffset);

      this.progresoRecordGlobal = this.calcularPorcentaje(
        this.data.facturacionSemanal,
        this.data.recordGlobalFacturacion
      );

      this.progresoRecordPersonal = this.calcularPorcentaje(
        this.data.facturacionSemanal,
        this.data.recordPersonalFacturacion
      );

      this.restanteRecordGlobal = Math.max(
        this.data.recordGlobalFacturacion - this.data.facturacionSemanal,
        0
      );

      this.restanteRecordPersonal = Math.max(
        this.data.recordPersonalFacturacion - this.data.facturacionSemanal,
        0
      );
    } catch {
      this.error = 'No se pudieron cargar las primas.';
    } finally {
      this.loading = false;
    }
  }

  private calcularPorcentaje(actual: number, objetivo: number): number {
    if (!objetivo || objetivo <= 0) return 0;
    return Math.min(Math.round((actual / objetivo) * 100), 100);
  }

  private generarDataMock(offset: number): PrimaData {
    const inicio = new Date();
    inicio.setDate(inicio.getDate() - inicio.getDay() + 1 - offset * 7);

    const fin = new Date(inicio);
    fin.setDate(fin.getDate() + 6);

    const semana = `Semana ${this.getWeekNumber(inicio)}`;
    const rangoFechas = `${this.formatearFecha(inicio)} - ${this.formatearFecha(fin)}`;

    const facturacionSemanal = Math.max(12000 - offset * 1200, 3500);
    const horasTrabajadas = Math.max(38 - offset * 2, 12);
    const serviciosRealizados = Math.max(22 - offset, 6);
    const primaBase = Math.round(facturacionSemanal * 0.08);
    const extraHoras = horasTrabajadas > 30 ? Math.round((horasTrabajadas - 30) * 50) : 0;
    const primaEstimada = primaBase + extraHoras;

    const historico: PrimaHistoricoItem[] = Array.from({ length: 6 }).map((_, i) => {
      const d1 = new Date(inicio);
      d1.setDate(d1.getDate() - i * 7);
      const d2 = new Date(d1);
      d2.setDate(d2.getDate() + 6);

      const fact = Math.max(12000 - i * 900, 3000);
      const horas = Math.max(38 - i * 2, 10);
      const servicios = Math.max(20 - i, 5);
      const base = Math.round(fact * 0.08);
      const extra = Math.max(horas - 30, 0) * 50;
      const prima = base + extra;

      return {
        semana: `Semana ${this.getWeekNumber(d1)}`,
        rangoFechas: `${this.formatearFecha(d1)} - ${this.formatearFecha(d2)}`,
        facturacion: fact,
        horas,
        servicios,
        primaBase: base,
        extraHoras: extra,
        prima
      };
    });

    return {
      semana,
      rangoFechas,
      facturacionSemanal,
      recordGlobalFacturacion: 18000,
      recordPersonalFacturacion: 15000,
      primaEstimada,
      primaBase,
      extraHoras,
      horasTrabajadas,
      serviciosRealizados,
      diasTrabajados: Math.max(5 - Math.floor(offset / 2), 2),
      porcentajeAplicado: 8,
      historico
    };
  }

  private formatearFecha(fecha: Date): string {
    return fecha.toLocaleDateString('es-ES', {
      day: '2-digit',
      month: '2-digit',
      year: 'numeric'
    });
  }

  private getWeekNumber(date: Date): number {
    const d = new Date(Date.UTC(date.getFullYear(), date.getMonth(), date.getDate()));
    const dayNum = d.getUTCDay() || 7;
    d.setUTCDate(d.getUTCDate() + 4 - dayNum);
    const yearStart = new Date(Date.UTC(d.getUTCFullYear(), 0, 1));
    return Math.ceil((((d.getTime() - yearStart.getTime()) / 86400000) + 1) / 7);
  }
}