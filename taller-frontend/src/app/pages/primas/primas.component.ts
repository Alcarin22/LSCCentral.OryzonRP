import {
  ChangeDetectorRef,
  Component,
  NgZone,
  OnInit
} from '@angular/core';

import { CommonModule } from '@angular/common';
import { firstValueFrom } from 'rxjs';

import {
  SessionEmpleado,
  SessionService
} from '../../core/services/session.service';

import {
  MisPrimasResponse,
  PrimasService
} from '../../core/services/primas.service';

@Component({
  selector: 'app-primas',
  standalone: true,
  imports: [
    CommonModule
  ],
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

  data: MisPrimasResponse =
    this.getEmptyData();

  progresoRecordGlobal = 0;
  progresoRecordPersonal = 0;

  restanteRecordGlobal = 0;
  restanteRecordPersonal = 0;

  constructor(
    private sessionService: SessionService,
    private primasService: PrimasService,
    private zone: NgZone,
    private cdr: ChangeDetectorRef
  ) {}

  ngOnInit(): void {

    this.empleado =
      this.obtenerEmpleado();

    if (!this.empleado) {

      this.error =
        'No hay sesión activa.';

      this.loading = false;

      this.cdr.detectChanges();

      return;
    }

    this.nombreVisible =
      this.empleado.nickServidor ||
      this.empleado.nombre ||
      'Empleado';

    this.rangoVisible =
      this.empleado.rango?.nombre ||
      'Sin rango';

    this.weekOffset = 0;

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

  private obtenerEmpleado():
    SessionEmpleado | null {

    const empleadoServicio =
      this.sessionService.getEmpleado();

    if (empleadoServicio) {
      return empleadoServicio;
    }

    try {

      const raw =
        localStorage.getItem(
          'empleado'
        );

      return raw
        ? JSON.parse(raw) as SessionEmpleado
        : null;

    } catch {

      return null;
    }
  }

  private async refrescarVista():
    Promise<void> {

    if (!this.empleado) {

      this.zone.run(() => {

        this.error =
          'No hay sesión activa.';

        this.loading = false;

        this.cdr.detectChanges();
      });

      return;
    }

    this.zone.run(() => {

      this.loading = true;

      this.error = '';

      this.cdr.detectChanges();
    });

    try {

      const response =
        await firstValueFrom(
          this.primasService
            .getMisPrimas(
              this.weekOffset
            )
        );

      this.zone.run(() => {

        this.data = {
          ...this.getEmptyData(),
          ...response,

          actividadDiaria:
            response.actividadDiaria
            ?? [],

          historico:
            response.historico
            ?? []
        };

        this.weekOffset =
          this.data.weekOffset
          ?? this.weekOffset;

        this.nombreVisible =
          this.data.nombreEmpleado ||
          this.nombreVisible;

        this.rangoVisible =
          this.data.rango ||
          this.rangoVisible;

        this.recalcularMetricas();

        this.loading = false;

        this.error = '';

        this.cdr.detectChanges();
      });

    } catch (error) {

      console.error(
        'Error cargando primas:',
        error
      );

      this.zone.run(() => {

        this.error =
          'No se pudieron cargar las primas.';

        this.loading = false;

        this.cdr.detectChanges();
      });
    }
  }

  private recalcularMetricas(): void {

    this.progresoRecordGlobal =
      this.calcularPorcentaje(
        this.data.facturacionSemanal,
        this.data.recordGlobalFacturacion
      );

    this.progresoRecordPersonal =
      this.calcularPorcentaje(
        this.data.facturacionSemanal,
        this.data.recordPersonalFacturacion
      );

    this.restanteRecordGlobal =
      Math.max(
        (
          this.data.recordGlobalFacturacion
          || 0
        )
        -
        (
          this.data.facturacionSemanal
          || 0
        ),
        0
      );

    this.restanteRecordPersonal =
      Math.max(
        (
          this.data.recordPersonalFacturacion
          || 0
        )
        -
        (
          this.data.facturacionSemanal
          || 0
        ),
        0
      );
  }

  get maxHistoricoFacturacion():
    number {

    return Math.max(
      ...this.data.historico.map(
        historico =>
          historico.facturacion
      ),
      this.data.facturacionSemanal,
      1
    );
  }

  get mejorSemanaHistorico():
    string {

    if (
      !this.data.historico.length
    ) {
      return '-';
    }

    const mejor =
      this.data.historico.reduce(
        (a, b) =>
          b.facturacion >
          a.facturacion
            ? b
            : a
      );

    return (
      `${mejor.semana} · ` +
      `${this.formatearDinero(
        mejor.facturacion
      )}`
    );
  }

  getPorcentajeBarra(
    valor: number
  ): number {

    return Math.min(
      Math.round(
        (
          valor /
          this.maxHistoricoFacturacion
        ) * 100
      ),
      100
    );
  }

  private calcularPorcentaje(
    actual: number,
    objetivo: number
  ): number {

    if (
      !objetivo ||
      objetivo <= 0
    ) {
      return 0;
    }

    return Math.min(
      Math.round(
        (
          actual /
          objetivo
        ) * 100
      ),
      100
    );
  }

  private formatearDinero(
    valor: number
  ): string {

    return new Intl.NumberFormat(
      'en-US',
      {
        style: 'currency',
        currency: 'USD',
        maximumFractionDigits: 0
      }
    ).format(
      valor || 0
    );
  }

  private getEmptyData():
    MisPrimasResponse {

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