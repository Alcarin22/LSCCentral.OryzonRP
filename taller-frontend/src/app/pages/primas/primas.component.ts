import { CommonModule, CurrencyPipe } from '@angular/common';
import { ChangeDetectorRef, Component, OnDestroy, OnInit } from '@angular/core';
import { Subscription } from 'rxjs';
import { SessionEmpleado, SessionService } from '../../core/services/session.service';
import { MisPrimasResponse, PrimasService } from '../../core/services/primas.service';

@Component({
  selector: 'app-primas',
  standalone: true,
  imports: [CommonModule, CurrencyPipe],
  templateUrl: './primas.component.html',
  styleUrl: './primas.component.css'
})
export class PrimasComponent implements OnInit, OnDestroy {
  empleado: SessionEmpleado | null = null;
  data: MisPrimasResponse = this.getEmptyData();
  weekOffset = 0;
  loading = true;
  error = false;

  private dataSub?: Subscription;

  constructor(
    private sessionService: SessionService,
    private primasService: PrimasService,
    private cdr: ChangeDetectorRef
  ) {}

  ngOnInit(): void {
    const empleado = this.sessionService.getEmpleado();

    if (!empleado?.discordId) {
      this.loading = false;
      this.error = true;
      this.cdr.detectChanges();
      return;
    }

    this.empleado = empleado;
    this.cargarDatos();
  }

  ngOnDestroy(): void {
    this.dataSub?.unsubscribe();
  }

  cargarDatos(): void {
    if (!this.empleado?.discordId) {
      this.loading = false;
      this.error = true;
      this.cdr.detectChanges();
      return;
    }

    this.loading = true;
    this.error = false;
    this.cdr.detectChanges();

    this.dataSub?.unsubscribe();

    this.dataSub = this.primasService.getMisPrimas(this.empleado.discordId, this.weekOffset).subscribe({
      next: (response) => {
        console.log('Respuesta primas:', response);

        this.data = {
          ...this.getEmptyData(),
          ...response,
          actividadDiaria: response.actividadDiaria ?? [],
          historico: response.historico ?? []
        };

        this.weekOffset = response.weekOffset ?? this.weekOffset;
        this.loading = false;
        this.error = false;

        this.cdr.detectChanges();
      },
      error: (error) => {
        console.error('Error cargando primas:', error);
        this.data = this.getEmptyData();
        this.loading = false;
        this.error = true;

        this.cdr.detectChanges();
      }
    });
  }

  semanaAnterior(): void {
    this.weekOffset += 1;
    this.cargarDatos();
  }

  semanaMasReciente(): void {
    if (this.weekOffset > 0) {
      this.weekOffset -= 1;
      this.cargarDatos();
    }
  }

  get progresoRecordGlobal(): number {
    const objetivo = Number(this.data.recordGlobalFacturacion ?? 0);
    const actual = Number(this.data.facturacionSemanal ?? 0);

    if (!objetivo || objetivo <= 0) {
      return 0;
    }

    const porcentaje = (actual / objetivo) * 100;
    return Math.max(0, Math.min(100, Math.round(porcentaje)));
  }

  get progresoRecordPersonal(): number {
    const objetivo = Number(this.data.recordPersonalFacturacion ?? 0);
    const actual = Number(this.data.facturacionSemanal ?? 0);

    if (!objetivo || objetivo <= 0) {
      return 0;
    }

    const porcentaje = (actual / objetivo) * 100;
    return Math.max(0, Math.min(100, Math.round(porcentaje)));
  }

  get restanteRecordGlobal(): number {
    const objetivo = Number(this.data.recordGlobalFacturacion ?? 0);
    const actual = Number(this.data.facturacionSemanal ?? 0);

    if (!objetivo || objetivo <= 0) {
      return 0;
    }

    return Math.max(0, objetivo - actual);
  }

  get restanteRecordPersonal(): number {
    const objetivo = Number(this.data.recordPersonalFacturacion ?? 0);
    const actual = Number(this.data.facturacionSemanal ?? 0);

    if (!objetivo || objetivo <= 0) {
      return 0;
    }

    return Math.max(0, objetivo - actual);
  }

  get nombreVisible(): string {
    if (!this.empleado) {
      return '—';
    }

    return this.empleado.nickServidor || this.empleado.nombre || '—';
  }

  get rangoVisible(): string {
    if (!this.empleado?.rango) {
      return '—';
    }

    if (typeof this.empleado.rango === 'string') {
      return this.empleado.rango;
    }

    return this.empleado.rango.nombre || '—';
  }

  private getEmptyData(): MisPrimasResponse {
    return {
      nombreEmpleado: '—',
      rango: '—',
      semana: 'Semana 0',
      rangoFechas: '',
      weekOffset: 0,
      primaEstimada: 0,
      primaBase: 0,
      extraHoras: 0,
      facturacionSemanal: 0,
      horasTrabajadas: '0h 0m',
      serviciosRealizados: 0,
      diasTrabajados: 0,
      porcentajeAplicado: 0,
      recordGlobalFacturacion: 0,
      recordPersonalFacturacion: 0,
      actividadDiaria: [],
      historico: []
    };
  }
}