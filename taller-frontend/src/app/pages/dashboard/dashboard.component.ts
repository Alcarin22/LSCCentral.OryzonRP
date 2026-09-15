import { Component, OnDestroy, OnInit, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { HttpClient } from '@angular/common/http';
import { Subscription, forkJoin, interval } from 'rxjs';
import { finalize } from 'rxjs/operators';

import { SessionEmpleado, SessionService } from '../../core/services/session.service';
import { environment } from '../../../environments/environment';
import { FichajeService, FichajeResponse } from '../../core/services/fichaje.service';
import { ToastService } from '../../core/services/toast.service';

interface DashboardHoyResponse {
  horaEntrada: string | null;
  fichajeActivo: boolean;
  serviciosRealizadosHoy: number;
  tipoServicio: 'MECANICA' | 'SEGURIDAD' | null;
  ultimoTurnoFecha: string | null;
  ultimoTurnoHoraEntrada: string | null;
  ultimoTurnoHoraSalida: string | null;
  ultimoTurnoMinutos: number | null;
}

interface DashboardSemanaResponse {
  horasRegistradas: string;
  diasTrabajados: number;
  serviciosCompletados: number;
  primaEstimada: string;
}

interface DashboardMesResponse {
  horasTotales: string;
  jornadasCompletadas: number;
  serviciosRealizados: number;
  rendimiento: string;
}

@Component({
  selector: 'app-dashboard',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './dashboard.component.html',
  styleUrls: ['./dashboard.component.css']
})
export class DashboardComponent implements OnInit, OnDestroy {
  empleado: SessionEmpleado | null = null;

  nombreVisible = 'Empleado';
  rangoVisible = 'Sin rango';

  procesandoToggle = false;
  fichado = false;

  textoBotonFichaje = 'Iniciar fichaje';
  horaEntradaFormateada = '--:--';
  tiempoTrabajadoActual = '00:00:00';
  estadoActualTexto = 'Fuera de servicio';
  fichajeSeguridadSeleccionado = false;
  tipoServicioActivo: 'MECANICA' | 'SEGURIDAD' | null = null;

  resumenHoy = {
    serviciosRealizadosHoy: 0
  };

  ultimoTurno = {
    fecha: null as string | null,
    horaEntrada: null as string | null,
    horaSalida: null as string | null,
    minutosTrabajados: null as number | null
  };

  resumenSemana = {
    horasRegistradas: '0h 0m',
    diasTrabajados: 0,
    serviciosCompletados: 0,
    primaEstimada: '$0'
  };

  resumenMes = {
    horasTotales: '0h 0m',
    jornadasCompletadas: 0,
    serviciosRealizados: 0,
    rendimiento: 'Bajo'
  };

  private sub!: Subscription;
  private timerSub!: Subscription;
  private fechaEntrada: Date | null = null;
  private requestVersion = 0;

  constructor(
    private sessionService: SessionService,
    private http: HttpClient,
    private fichajeService: FichajeService,
    private cdr: ChangeDetectorRef,
    private toastService: ToastService
  ) {}

  ngOnInit(): void {
    this.sub = this.sessionService.empleado$.subscribe((empleadoSesion: SessionEmpleado | null) => {
      this.empleado = empleadoSesion;
      this.nombreVisible = empleadoSesion?.nickServidor || empleadoSesion?.nombre || 'Empleado';
      this.rangoVisible = empleadoSesion?.rango?.nombre || 'Sin rango';

      if (empleadoSesion?.discordId) {
        this.cargarDashboardCompleto(empleadoSesion.discordId);
      }
    });
  }

  ngOnDestroy(): void {
    this.sub?.unsubscribe();
    this.timerSub?.unsubscribe();
  }

  get esRangoSeguridad(): boolean {
    const rango = this.empleado?.rango?.nombre?.trim().toLowerCase() ?? '';
    return rango === 'seguridad' || rango === 'jefe de seguridad';
  }

  get puedeSeleccionarFichajeSeguridad(): boolean {
    return !this.esRangoSeguridad && !!this.empleado?.puedeTrabajarComoSeguridad;
  }

  get servicioActualTexto(): string {
    if (!this.fichado || !this.tipoServicioActivo) {
      return '-';
    }
    return this.tipoServicioActivo === 'SEGURIDAD' ? 'Seguridad' : 'Mecánica';
  }


  get plantillaUltimoTurno(): string {
    return [
      `FECHA: ${this.formatearFechaUltimoTurno(this.ultimoTurno.fecha)}`,
      `HORA DE ENTRADA: ${this.formatearHoraBackend(this.ultimoTurno.horaEntrada)}`,
      `HORA DE SALIDA: ${this.formatearHoraBackend(this.ultimoTurno.horaSalida)}`,
      `HORAS TOTALES: ${this.formatearMinutosUltimoTurno(this.ultimoTurno.minutosTrabajados)}`
    ].join('\n');
  }

  copiarUltimoTurno(): void {
    if (!this.ultimoTurno.fecha) {
      this.toastService.info('Todavía no hay un turno para copiar.');
      return;
    }

    navigator.clipboard.writeText(this.plantillaUltimoTurno)
      .then(() => {
        this.toastService.success('Turno copiado al portapapeles.');
      })
      .catch((error) => {
        console.error('Error copiando el último turno:', error);
        this.toastService.error('No se pudo copiar el último turno.');
      });
  }

  toggleFichaje(): void {
    if (!this.empleado || this.procesandoToggle) {
      return;
    }

    this.procesandoToggle = true;
    this.cdr.detectChanges();

    this.fichajeService.toggleFichaje(
  this.fichajeSeguridadSeleccionado
).pipe(
      finalize(() => {
        this.procesandoToggle = false;
        this.cdr.detectChanges();
      })
    ).subscribe({
      next: (response: FichajeResponse) => {
        this.aplicarEstadoDesdeToggle(response);

        if (response.fichajeActivo) {
          this.toastService.success('Has entrado en servicio.');
        } else {
          this.toastService.info('Has salido de servicio.');
        }

        this.cdr.detectChanges();

        if (this.empleado?.discordId) {
          setTimeout(() => {
            this.cargarDashboardCompleto(this.empleado!.discordId);
          }, 700);
        }
      },
      error: (error) => {
        console.error('Error al hacer toggle de fichaje:', error);
        this.toastService.error('No se pudo actualizar el fichaje.');
      }
    });
  }

  private cargarDashboardCompleto(discordId: string): void {
    const baseUrl = environment.backendUrl;
    const currentVersion = ++this.requestVersion;
    const ts = Date.now();

    forkJoin({
      hoy: this.http.get<DashboardHoyResponse>(`${baseUrl}/api/dashboard/hoy/${discordId}?ts=${ts}`),
      semana: this.http.get<DashboardSemanaResponse>(`${baseUrl}/api/dashboard/semana/${discordId}?ts=${ts}`),
      mes: this.http.get<DashboardMesResponse>(`${baseUrl}/api/dashboard/mes/${discordId}?ts=${ts}`)
    }).subscribe({
      next: ({ hoy, semana, mes }) => {
        if (currentVersion !== this.requestVersion) {
          return;
        }

        this.aplicarEstadoDesdeDashboardHoy(hoy);

        this.resumenSemana = {
          horasRegistradas: semana.horasRegistradas ?? '0h 0m',
          diasTrabajados: semana.diasTrabajados ?? 0,
          serviciosCompletados: semana.serviciosCompletados ?? 0,
          primaEstimada: semana.primaEstimada ?? '$0'
        };

        this.resumenMes = {
          horasTotales: mes.horasTotales ?? '0h 0m',
          jornadasCompletadas: mes.jornadasCompletadas ?? 0,
          serviciosRealizados: mes.serviciosRealizados ?? 0,
          rendimiento: mes.rendimiento ?? 'Bajo'
        };

        this.cdr.detectChanges();
      },
      error: (error) => {
        console.error('Error cargando dashboard:', error);
        this.toastService.error('No se pudo cargar el resumen del panel.');
      }
    });
  }

  private aplicarEstadoDesdeToggle(response: FichajeResponse): void {
    this.fichado = !!response.fichajeActivo;

    if (this.fichado) {
      this.estadoActualTexto = 'En servicio';
      this.textoBotonFichaje = 'Finalizar fichaje';
      this.tipoServicioActivo = response.tipoServicio ?? (this.esRangoSeguridad || this.fichajeSeguridadSeleccionado ? 'SEGURIDAD' : 'MECANICA');

      if (response.fechaHoraEntrada) {
        this.ultimoTurno = {
          fecha: this.extraerFechaBackend(response.fechaHoraEntrada),
          horaEntrada: response.fechaHoraEntrada,
          horaSalida: null,
          minutosTrabajados: null
        };

        this.fechaEntrada = this.parseBackendLocalDateTime(response.fechaHoraEntrada);

        if (this.fechaEntrada) {
          this.horaEntradaFormateada = this.formatearHora(this.fechaEntrada);
          this.iniciarTemporizador();
        }
      }
    } else {
      if (response.fechaHoraEntrada) {
        this.ultimoTurno = {
          fecha: this.extraerFechaBackend(response.fechaHoraEntrada),
          horaEntrada: response.fechaHoraEntrada,
          horaSalida: response.fechaHoraSalida ?? null,
          minutosTrabajados: response.minutosTrabajados ?? null
        };
      }

      this.estadoActualTexto = 'Fuera de servicio';
      this.textoBotonFichaje = 'Iniciar fichaje';
      this.resetEstadoFichajeVisual();
      this.fichajeSeguridadSeleccionado = false;
    }
  }

  private aplicarEstadoDesdeDashboardHoy(hoy: DashboardHoyResponse): void {
    if (hoy.fichajeActivo === undefined) {
      console.warn('Respuesta inválida de dashboard hoy:', hoy);
      return;
    }

    this.resumenHoy = {
      serviciosRealizadosHoy: hoy.serviciosRealizadosHoy ?? 0
    };

    this.ultimoTurno = {
      fecha: hoy.ultimoTurnoFecha ?? null,
      horaEntrada: hoy.ultimoTurnoHoraEntrada ?? null,
      horaSalida: hoy.ultimoTurnoHoraSalida ?? null,
      minutosTrabajados: hoy.ultimoTurnoMinutos ?? null
    };

    this.fichado = !!hoy.fichajeActivo;

    if (this.fichado && hoy.horaEntrada) {
      this.estadoActualTexto = 'En servicio';
      this.textoBotonFichaje = 'Finalizar fichaje';
      this.tipoServicioActivo = hoy.tipoServicio ?? (this.esRangoSeguridad ? 'SEGURIDAD' : 'MECANICA');
      this.fichajeSeguridadSeleccionado = this.tipoServicioActivo === 'SEGURIDAD';

      this.fechaEntrada = this.parseBackendLocalDateTime(hoy.horaEntrada);

      if (this.fechaEntrada) {
        this.horaEntradaFormateada = this.formatearHora(this.fechaEntrada);
        this.iniciarTemporizador();
      }
    } else {
      this.estadoActualTexto = 'Fuera de servicio';
      this.textoBotonFichaje = 'Iniciar fichaje';
      this.resetEstadoFichajeVisual();
    }
  }

  private resetEstadoFichajeVisual(): void {
    this.fichado = false;
    this.fechaEntrada = null;
    this.horaEntradaFormateada = '--:--';
    this.tiempoTrabajadoActual = '00:00:00';
    this.tipoServicioActivo = null;
    this.timerSub?.unsubscribe();
  }

  private iniciarTemporizador(): void {
    this.timerSub?.unsubscribe();

    if (!this.fechaEntrada) {
      this.tiempoTrabajadoActual = '00:00:00';
      return;
    }

    this.actualizarTiempoTrabajado();

    this.timerSub = interval(1000).subscribe(() => {
      this.actualizarTiempoTrabajado();
    });
  }

  private actualizarTiempoTrabajado(): void {
    if (!this.fechaEntrada) {
      this.tiempoTrabajadoActual = '00:00:00';
      return;
    }

    const segundos = Math.max(
      0,
      Math.floor((Date.now() - this.fechaEntrada.getTime()) / 1000)
    );

    this.tiempoTrabajadoActual = this.formatearDuracion(segundos);
  }


  formatearFechaUltimoTurno(value: string | null | undefined): string {
    if (!value) {
      return '';
    }

    const [year, month, day] = value.split('-');

    if (!year || !month || !day) {
      return value;
    }

    return `${day}/${month}/${year}`;
  }

  formatearHoraBackend(value: string | null | undefined): string {
    if (!value) {
      return '';
    }

    const limpio = value.trim().replace(' ', 'T');
    const partes = limpio.split('T');

    if (partes.length < 2) {
      return '';
    }

    const [hora, minuto] = partes[1].split(':');

    if (!hora || !minuto) {
      return '';
    }

    return `${hora}:${minuto}`;
  }

  formatearMinutosUltimoTurno(minutos: number | null | undefined): string {
    if (minutos === null || minutos === undefined) {
      return '';
    }

    const totalMinutos = Math.max(0, minutos);
    const horas = Math.floor(totalMinutos / 60);
    const mins = totalMinutos % 60;

    return `${horas}h ${String(mins).padStart(2, '0')}m`;
  }

  private extraerFechaBackend(value: string | null | undefined): string | null {
    if (!value) {
      return null;
    }

    const limpio = value.trim().replace(' ', 'T');
    const [fecha] = limpio.split('T');

    return fecha || null;
  }

  private parseBackendLocalDateTime(value: string | null | undefined): Date | null {
    if (!value) {
      return null;
    }

    const limpio = value
      .trim()
      .replace(' ', 'T')
      .replace(/Z$/, '')
      .replace(/[+-]\d{2}:\d{2}$/, '');

    const [datePart, timePartRaw] = limpio.split('T');

    if (!datePart || !timePartRaw) {
      return null;
    }

    const [year, month, day] = datePart.split('-').map(Number);
    const [hourRaw, minuteRaw, secondRaw] = timePartRaw.split(':');

    const hour = Number(hourRaw);
    const minute = Number(minuteRaw);
    const second = Number((secondRaw ?? '0').split('.')[0]);

    if (
      Number.isNaN(year) ||
      Number.isNaN(month) ||
      Number.isNaN(day) ||
      Number.isNaN(hour) ||
      Number.isNaN(minute) ||
      Number.isNaN(second)
    ) {
      return null;
    }

    return new Date(
      year,
      month - 1,
      day,
      hour,
      minute,
      second
    );
  }

  private formatearHora(fecha: Date): string {
    return fecha.toLocaleTimeString('es-ES', {
      hour: '2-digit',
      minute: '2-digit'
    });
  }

  private formatearDuracion(totalSegundos: number): string {
    const horas = Math.floor(totalSegundos / 3600);
    const minutos = Math.floor((totalSegundos % 3600) / 60);
    const segundos = totalSegundos % 60;

    return [horas, minutos, segundos]
      .map(v => String(v).padStart(2, '0'))
      .join(':');
  }
}